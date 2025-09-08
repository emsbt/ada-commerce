package br.com.adacommerce.config;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class DatabaseConfig {
    private static final String URL = "jdbc:sqlite:adacommerce.db";
    private static Connection connection;

    public static void initialize() {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            criarTabelaUsuario(conn);
            garantirColunaUsuarioEmUsuario(conn);

            executarDDL(conn, """
                CREATE TABLE IF NOT EXISTS categoria (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    descricao TEXT,
                    categoria_pai_id INTEGER,
                    ativo BOOLEAN DEFAULT 1,
                    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                    data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (categoria_pai_id) REFERENCES categoria(id)
                )
                """);

            executarDDL(conn, """
                CREATE TABLE IF NOT EXISTS cliente (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    documento TEXT UNIQUE NOT NULL,
                    email TEXT,
                    telefone TEXT,
                    endereco TEXT,
                    data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP,
                    ativo BOOLEAN DEFAULT 1
                )
                """);

            executarDDL(conn, """
                CREATE TABLE IF NOT EXISTS produto (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    preco_base REAL NOT NULL,
                    ativo BOOLEAN DEFAULT 1,
                    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """);

            executarDDL(conn, """
                CREATE TABLE IF NOT EXISTS pedido (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    cliente_id INTEGER NOT NULL,
                    status_pedido TEXT NOT NULL,
                    status_pagamento TEXT NOT NULL,
                    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                    data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (cliente_id) REFERENCES cliente(id)
                )
                """);

            executarDDL(conn, """
                CREATE TABLE IF NOT EXISTS item_pedido (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pedido_id INTEGER NOT NULL,
                    produto_id INTEGER NOT NULL,
                    quantidade INTEGER NOT NULL,
                    preco_venda REAL NOT NULL,
                    subtotal REAL NOT NULL,
                    FOREIGN KEY (pedido_id) REFERENCES pedido(id),
                    FOREIGN KEY (produto_id) REFERENCES produto(id)
                )
                """);

            executarDDL(conn, """
                CREATE TRIGGER IF NOT EXISTS trg_pedido_update_timestamp
                AFTER UPDATE ON pedido
                FOR EACH ROW
                BEGIN
                    UPDATE pedido SET data_atualizacao = CURRENT_TIMESTAMP WHERE id = NEW.id;
                END;
                """);

            executarDDL(conn, """
                INSERT OR IGNORE INTO usuario (nome, email, senha, usuario)
                VALUES ('Admin', 'admin@adacommerce.com', 'admin123', 'admin')
                """);

            conn.commit();
            System.out.println("Estrutura de banco atualizada com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro na inicialização do banco: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void criarTabelaUsuario(Connection conn) throws SQLException {
        executarDDL(conn, """
            CREATE TABLE IF NOT EXISTS usuario (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                senha TEXT NOT NULL,
                ativo BOOLEAN DEFAULT 1,
                data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """);
    }

    private static void garantirColunaUsuarioEmUsuario(Connection conn) throws SQLException {
        Set<String> colunas = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement("PRAGMA table_info(usuario)");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                colunas.add(rs.getString("name").toLowerCase());
            }
        }
        if (!colunas.contains("usuario")) {
            System.out.println("Adicionando coluna 'usuario' em 'usuario'...");
            executarDDL(conn, "ALTER TABLE usuario ADD COLUMN usuario TEXT");
            executarDDL(conn, """
                UPDATE usuario
                SET usuario = CASE
                    WHEN email LIKE '%@%' THEN substr(email, 1, instr(email, '@') - 1)
                    ELSE email
                END
                WHERE (usuario IS NULL OR usuario = '')
                """);
        }
    }

    private static void executarDDL(Connection conn, String ddl) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute(ddl);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(URL);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver SQLite não encontrado", e);
            }
        }
        return connection;
    }

    public static Connection getNewConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(URL);
        } catch (Exception e) {
            System.err.println("Erro ao abrir nova conexão: " + e.getMessage());
            return null;
        }
    }
}