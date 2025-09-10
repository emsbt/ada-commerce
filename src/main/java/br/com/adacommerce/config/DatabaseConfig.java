package br.com.adacommerce.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public final class DatabaseConfig {

    private static final String DB_DIR = "data";
    private static final String DB_FILE = "ada-commerce.db";
    private static final String URL = "jdbc:sqlite:" + DB_DIR + "/" + DB_FILE;

    private static volatile boolean initialized = false;
    private static final Object LOCK = new Object();

    private DatabaseConfig(){}

    public static void initialize() {
        if (initialized) return;
        synchronized (LOCK) {
            if (initialized) return;
            try {
                Path dir = Path.of(DB_DIR);
                if (!Files.exists(dir)) Files.createDirectories(dir);
                try (Connection c = DriverManager.getConnection(URL)) {
                    c.createStatement().execute("PRAGMA foreign_keys = ON");
                    migrarTabelaItensSeNecessario(c);  // antes de criar para evitar conflito
                    criarTabelas(c);
                    // garantir colunas novas caso o DB já exista
                    garantirColunasPedido(c);
                    seedOuMigrarAdmin(c);
                }
                initialized = true;
                System.out.println("[DB] Inicializado em: " + DB_DIR + "/" + DB_FILE);
            } catch (Exception e) {
                throw new RuntimeException("Falha ao inicializar banco: " + e.getMessage(), e);
            }
        }
    }

    private static boolean existeTabela(Connection c, String nome) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?")) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void migrarTabelaItensSeNecessario(Connection c) throws SQLException {
        boolean hasItemSingular = existeTabela(c, "item_pedido");
        boolean hasItensPlural  = existeTabela(c, "itens_pedido");
        if (hasItemSingular && !hasItensPlural) {
            System.out.println("[DB] Migrando tabela item_pedido -> itens_pedido");
            try (Statement st = c.createStatement()) {
                st.execute("ALTER TABLE item_pedido RENAME TO itens_pedido");
            }
        }
    }

    private static void criarTabelas(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS usuario(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  nome TEXT NOT NULL,
                  email TEXT UNIQUE NOT NULL,
                  senha TEXT NOT NULL,
                  usuario TEXT UNIQUE,
                  ativo INTEGER DEFAULT 1,
                  data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS categoria(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  nome TEXT NOT NULL,
                  descricao TEXT,
                  categoria_pai_id INTEGER,
                  ativo INTEGER DEFAULT 1,
                  data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                  data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                  FOREIGN KEY(categoria_pai_id) REFERENCES categoria(id)
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS cliente(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  nome TEXT NOT NULL,
                  documento TEXT UNIQUE NOT NULL,
                  email TEXT,
                  telefone TEXT,
                  endereco TEXT,
                  ativo INTEGER DEFAULT 1,
                  data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                  data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS produto(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  nome TEXT NOT NULL,
                  descricao TEXT,
                  categoria_id INTEGER,
                  preco REAL NOT NULL,
                  estoque_atual INTEGER DEFAULT 0,
                  ativo INTEGER DEFAULT 1,
                  data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                  data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                  FOREIGN KEY(categoria_id) REFERENCES categoria(id)
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS pedido(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  cliente_id INTEGER,
                  numero TEXT,
                  status_pedido TEXT NOT NULL,
                  status_pagamento TEXT NOT NULL,
                  desconto REAL NOT NULL DEFAULT 0,
                  total_bruto REAL DEFAULT 0,
                  total_liquido REAL DEFAULT 0,
                  data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                  data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                  FOREIGN KEY(cliente_id) REFERENCES cliente(id)
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS itens_pedido(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  pedido_id INTEGER NOT NULL,
                  produto_id INTEGER NOT NULL,
                  produto_nome_snapshot TEXT,
                  quantidade INTEGER NOT NULL,
                  preco_unitario REAL NOT NULL,
                  desconto REAL NOT NULL DEFAULT 0,
                  FOREIGN KEY(pedido_id) REFERENCES pedido(id),
                  FOREIGN KEY(produto_id) REFERENCES produto(id)
                )""");
        }
    }

    private static void garantirColunasPedido(Connection c) throws SQLException {
        // adiciona colunas que possam faltar em bancos antigos
        ensureColumnExists(c, "pedido", "numero", "TEXT");
        ensureColumnExists(c, "pedido", "total_bruto", "REAL DEFAULT 0");
        ensureColumnExists(c, "pedido", "total_liquido", "REAL DEFAULT 0");
        // desconto já existia no seu schema original; se quiser garantir:
        ensureColumnExists(c, "pedido", "desconto", "REAL NOT NULL DEFAULT 0");
    }

    private static boolean columnExists(Connection c, String table, String column) throws SQLException {
        // PRAGMA table_info retorna colunas: cid,name,type,...
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info('" + table + "')")) {
            while (rs.next()) {
                String name = rs.getString("name");
                if (column.equalsIgnoreCase(name)) return true;
            }
        }
        return false;
    }

    private static void ensureColumnExists(Connection c, String table, String columnName, String columnDefinition) throws SQLException {
        if (columnExists(c, table, columnName)) return;
        String sql = "ALTER TABLE " + table + " ADD COLUMN " + columnName + " " + columnDefinition;
        try (Statement st = c.createStatement()) {
            System.out.println("[DB] Adicionando coluna " + columnName + " em " + table);
            st.execute(sql);
        }
    }

    private static void seedOuMigrarAdmin(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id FROM usuario WHERE usuario='admin'")) {
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO usuario (nome,email,senha,usuario,ativo) VALUES (?,?,?,?,1)")) {
                    ins.setString(1, "Administrador");
                    ins.setString(2, "admin@local");
                    ins.setString(3, "admin123");
                    ins.setString(4, "admin");
                    ins.executeUpdate();
                }
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        initialize();
        return DriverManager.getConnection(URL);
    }
}