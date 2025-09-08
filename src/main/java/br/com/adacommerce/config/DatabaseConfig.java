package br.com.adacommerce.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final String URL = "jdbc:sqlite:adacommerce.db";
    private static Connection connection;

    public static void initialize() {
        // Criar tabelas na inicialização
        try (Connection conn = getConnection()) {
            String[] scripts = {
                    """
                    CREATE TABLE IF NOT EXISTS usuario (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL,
                        email TEXT UNIQUE NOT NULL,
                        senha TEXT NOT NULL,
                        ativo BOOLEAN DEFAULT 1,
                        data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                    """,
                    """
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
                    """,
                    """
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
                    """,
                    """
                    INSERT OR IGNORE INTO usuario (nome, email, senha) 
                    VALUES ('Admin', 'admin@adacommerce.com', 'admin123')
                    """
            };

            for (String script : scripts) {
                conn.createStatement().execute(script);
            }
            System.out.println("Tabelas criadas com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabelas: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(URL);
                System.out.println("Conexão estabelecida!");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver SQLite não encontrado", e);
            }
        }
        return connection;
    }

    public static Connection getNewConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(URL);
            System.out.println("Nova conexão estabelecida!");
            return conn;
        } catch (Exception e) {
            System.err.println("Erro ao conectar: " + e.getMessage());
            return null;
        }
    }
}