package br.com.adacommerce.config;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public class DatabaseConfig {

    private static final String DB_DIR = "data";
    private static final String DB_FILE = "ada-commerce.db";
    private static final String URL = "jdbc:sqlite:" + DB_DIR + "/" + DB_FILE;

    private static Connection connection;
    private static volatile boolean initialized = false;

    // DDLs
    private static final String DDL_USUARIO =
            "CREATE TABLE IF NOT EXISTS usuario (" +
                    " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " nome TEXT NOT NULL," +
                    " email TEXT UNIQUE NOT NULL," +
                    " senha TEXT NOT NULL," +
                    " usuario TEXT UNIQUE," +
                    " ativo BOOLEAN DEFAULT 1," +
                    " data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";

    private static final String DDL_CATEGORIA =
            "CREATE TABLE IF NOT EXISTS categoria (" +
                    " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " nome TEXT NOT NULL," +
                    " descricao TEXT," +
                    " categoria_pai_id INTEGER," +
                    " ativo BOOLEAN DEFAULT 1," +
                    " data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    " data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    " FOREIGN KEY (categoria_pai_id) REFERENCES categoria(id)" +
                    ")";

    private static final String DDL_CLIENTE =
            "CREATE TABLE IF NOT EXISTS cliente (" +
                    " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " nome TEXT NOT NULL," +
                    " documento TEXT UNIQUE NOT NULL," +
                    " email TEXT," +
                    " telefone TEXT," +
                    " endereco TEXT," +
                    " data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    " ativo BOOLEAN DEFAULT 1" +
                    ")";

    private static final String DDL_PRODUTO =
            "CREATE TABLE IF NOT EXISTS produto (" +
                    " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " nome TEXT NOT NULL," +
                    " preco_base REAL NOT NULL," +
                    " ativo BOOLEAN DEFAULT 1," +
                    " data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";

    private static final String DDL_PEDIDO =
            "CREATE TABLE IF NOT EXISTS pedido (" +
                    " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " cliente_id INTEGER NOT NULL," +
                    " status_pedido TEXT NOT NULL," +
                    " status_pagamento TEXT NOT NULL," +
                    " data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    " data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    " FOREIGN KEY (cliente_id) REFERENCES cliente(id)" +
                    ")";

    private static final String DDL_ITEM_PEDIDO =
            "CREATE TABLE IF NOT EXISTS item_pedido (" +
                    " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " pedido_id INTEGER NOT NULL," +
                    " produto_id INTEGER NOT NULL," +
                    " quantidade INTEGER NOT NULL," +
                    " preco_venda REAL NOT NULL," +
                    " subtotal REAL NOT NULL," +
                    " FOREIGN KEY (pedido_id) REFERENCES pedido(id)," +
                    " FOREIGN KEY (produto_id) REFERENCES produto(id)" +
                    ")";

    private DatabaseConfig() {}

    public static synchronized void initialize() {
        if (initialized) return;
        try {
            criarDiretorio();
            connection = DriverManager.getConnection(URL);
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            criarTabelas();
            seedOrMigrateAdmin();
            initialized = true;
            System.out.println("[DB] Inicializado em: " + DB_DIR + "/" + DB_FILE);
        } catch (Exception e) {
            throw new RuntimeException("Erro inicializando banco: " + e.getMessage(), e);
        }
    }

    private static void criarDiretorio() throws Exception {
        Path dir = Path.of(DB_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            System.out.println("[DB] Diretório criado: " + dir.toAbsolutePath());
        }
    }

    private static void criarTabelas() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute(DDL_USUARIO);
            st.execute(DDL_CATEGORIA);
            st.execute(DDL_CLIENTE);
            st.execute(DDL_PRODUTO);
            st.execute(DDL_PEDIDO);
            st.execute(DDL_ITEM_PEDIDO);
        }
    }

    // Cria ou converte admin para hash se estiver em texto puro
    private static void seedOrMigrateAdmin() throws SQLException {
        // Verifica se já existe admin
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id, senha FROM usuario WHERE usuario = ?")) {
            ps.setString(1, "admin");
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                // Criar admin
                String hash = BCrypt.hashpw("admin123", BCrypt.gensalt());
                try (PreparedStatement ins = connection.prepareStatement(
                        "INSERT INTO usuario (nome,email,senha,usuario,ativo) VALUES (?,?,?,?,1)")) {
                    ins.setString(1, "Admin");
                    ins.setString(2, "admin@adacommerce.com");
                    ins.setString(3, hash);
                    ins.setString(4, "admin");
                    ins.executeUpdate();
                    System.out.println("[DB] Usuário admin criado (admin / admin123).");
                }
                return;
            } else {
                String senha = rs.getString("senha");
                if (!senha.startsWith("$2a$") && !senha.startsWith("$2b$") && !senha.startsWith("$2y$")) {
                    // Migrar senha texto -> hash
                    String novoHash = BCrypt.hashpw(senha, BCrypt.gensalt());
                    try (PreparedStatement up = connection.prepareStatement(
                            "UPDATE usuario SET senha=? WHERE usuario='admin'")) {
                        up.setString(1, novoHash);
                        up.executeUpdate();
                        System.out.println("[DB] Senha admin migrada para hash BCrypt.");
                    }
                }
            }
        }
    }

    public static Connection getConnection() {
        if (!initialized) initialize();
        return connection;
    }
}