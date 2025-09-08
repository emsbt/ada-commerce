package br.com.adacommerce.config;

import org.mindrot.jbcrypt.BCrypt;

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
                if (!Files.exists(dir)) {
                    Files.createDirectories(dir);
                }
                // Cria tabelas e seed usando uma conexão temporária
                try (Connection c = DriverManager.getConnection(URL)) {
                    c.createStatement().execute("PRAGMA foreign_keys = ON");
                    criarTabelas(c);
                    seedOuMigrarAdmin(c);
                }
                initialized = true;
                System.out.println("[DB] Inicializado em: " + DB_DIR + "/" + DB_FILE);
            } catch (Exception e) {
                throw new RuntimeException("Falha ao inicializar banco: " + e.getMessage(), e);
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
                  data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP,
                  ativo INTEGER DEFAULT 1
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS produto(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  nome TEXT NOT NULL,
                  preco_base REAL NOT NULL,
                  ativo INTEGER DEFAULT 1,
                  data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS pedido(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  cliente_id INTEGER NOT NULL,
                  status_pedido TEXT NOT NULL,
                  status_pagamento TEXT NOT NULL,
                  data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                  data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                  FOREIGN KEY(cliente_id) REFERENCES cliente(id)
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS item_pedido(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  pedido_id INTEGER NOT NULL,
                  produto_id INTEGER NOT NULL,
                  quantidade INTEGER NOT NULL,
                  preco_venda REAL NOT NULL,
                  subtotal REAL NOT NULL,
                  FOREIGN KEY(pedido_id) REFERENCES pedido(id),
                  FOREIGN KEY(produto_id) REFERENCES produto(id)
                )""");
        }
    }

    private static void seedOuMigrarAdmin(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id, senha FROM usuario WHERE usuario='admin'")) {
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                String hash = BCrypt.hashpw("admin123", BCrypt.gensalt());
                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO usuario (nome,email,senha,usuario,ativo) VALUES (?,?,?,?,1)")) {
                    ins.setString(1, "Admin");
                    ins.setString(2, "admin@adacommerce.com");
                    ins.setString(3, hash);
                    ins.setString(4, "admin");
                    ins.executeUpdate();
                    System.out.println("[DB] Usuário admin criado (admin / admin123).");
                }
            } else {
                String senha = rs.getString("senha");
                if (!(senha.startsWith("$2a$") || senha.startsWith("$2b$") || senha.startsWith("$2y$"))) {
                    try (PreparedStatement up = c.prepareStatement(
                            "UPDATE usuario SET senha=? WHERE usuario='admin'")) {
                        up.setString(1, BCrypt.hashpw(senha, BCrypt.gensalt()));
                        up.executeUpdate();
                        System.out.println("[DB] Senha admin migrada para hash BCrypt.");
                    }
                }
            }
        }
    }

    // Retorna SEMPRE nova conexão (cada try-with-resources fecha apenas a sua)
    public static Connection getConnection() {
        if (!initialized) initialize();
        try {
            Connection c = DriverManager.getConnection(URL);
            c.createStatement().execute("PRAGMA foreign_keys = ON");
            return c;
        } catch (SQLException e) {
            throw new RuntimeException("Erro obtendo conexão: " + e.getMessage(), e);
        }
    }
}