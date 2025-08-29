package br.com.adacommerce.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    // Banco SQLite local - arquivo será criado automaticamente
    private static final String URL = "jdbc:sqlite:adacommerce.db";

    private static Connection connection;

    public static void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(URL);
            System.out.println("Conexão com banco SQLite estabelecida!");
            criarTabelas();
        } catch (Exception e) {
            System.err.println("Erro ao conectar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void criarTabelas() {
        String[] scripts = {
                """
            CREATE TABLE IF NOT EXISTS usuario (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario VARCHAR(50) UNIQUE NOT NULL,
                senha VARCHAR(255) NOT NULL,
                nome VARCHAR(100) NOT NULL,
                email VARCHAR(100) NOT NULL,
                ativo BOOLEAN DEFAULT 1,
                data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS categoria (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome VARCHAR(100) NOT NULL,
                descricao TEXT,
                categoria_pai_id INTEGER,
                ativo BOOLEAN DEFAULT 1,
                data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (categoria_pai_id) REFERENCES categoria(id)
            )
            """,
                """
            INSERT OR IGNORE INTO usuario (usuario, senha, nome, email)
            VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9tArjmMs.Nt5WoO', 'Admin', 'admin@ada.com')
            """
        };

        try {
            for (String script : scripts) {
                connection.createStatement().execute(script);
            }
            System.out.println("Tabelas criadas com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabelas: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        return connection;
    }
}