package br.com.adacommerce.config;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public final class DatabaseConfig {

    private static final Path DB_DIR = Path.of("database");
    private static final Path DB_FILE = DB_DIR.resolve("ada_commerce.db");
    private static final String URL = "jdbc:sqlite:" + DB_FILE;

    private static Connection conn;

    static {
        init();
    }

    private DatabaseConfig() {}

    private static void init() {
        try {
            criarDiretorio();
            conn = DriverManager.getConnection(URL);
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            criarTabelas();
            seedAdmin();
            System.out.println("[DB] Inicializado em: " + DB_FILE.toAbsolutePath());
        } catch (SQLException e) {
            throw new RuntimeException("Erro inicializando banco: " + e.getMessage(), e);
        }
    }

    private static void criarDiretorio() {
        try {
            if (!Files.exists(DB_DIR)) {
                Files.createDirectories(DB_DIR);
                System.out.println("[DB] Diretório criado: " + DB_DIR.toAbsolutePath());
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha criando diretório: " + e.getMessage(), e);
        }
    }

    private static void criarTabelas() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS usuario(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  login TEXT UNIQUE NOT NULL,
                  senha TEXT NOT NULL,
                  nome TEXT
                );
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS categoria(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  nome TEXT NOT NULL UNIQUE,
                  descricao TEXT,
                  categoria_pai_id INTEGER,
                  ativo INTEGER DEFAULT 1,
                  FOREIGN KEY(categoria_pai_id) REFERENCES categoria(id)
                );
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS cliente(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  nome TEXT NOT NULL,
                  email TEXT,
                  documento TEXT
                );
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS produto(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  nome TEXT NOT NULL,
                  preco REAL NOT NULL,
                  estoque INTEGER DEFAULT 0
                );
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS pedido(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  cliente_id INTEGER,
                  data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  status_pedido TEXT,
                  status_pagamento TEXT,
                  FOREIGN KEY(cliente_id) REFERENCES cliente(id)
                );
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS itens_pedido(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  pedido_id INTEGER NOT NULL,
                  produto_id INTEGER NOT NULL,
                  produto_nome_snapshot TEXT,
                  quantidade INTEGER NOT NULL,
                  preco_unitario REAL NOT NULL,
                  FOREIGN KEY(pedido_id) REFERENCES pedido(id),
                  FOREIGN KEY(produto_id) REFERENCES produto(id)
                );
                """);
        }
    }

    private static void seedAdmin() {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM usuario");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            long total = rs.getLong(1);
            if (total == 0) {
                String hash = BCrypt.hashpw("admin", BCrypt.gensalt());
                try (PreparedStatement ins =
                             conn.prepareStatement("INSERT INTO usuario(login, senha, nome) VALUES (?,?,?)")) {
                    ins.setString(1, "admin");
                    ins.setString(2, hash);
                    ins.setString(3, "Administrador");
                    ins.executeUpdate();
                    System.out.println("[DB] Usuário admin criado (admin/admin).");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro seed admin: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() {
        if (conn == null) init();
        return conn;
    }

    // compatibilidade se em algum lugar ainda chamava initialize()
    public static void initialize() {
        getConnection();
    }
}