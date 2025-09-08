package br.com.adacommerce.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Configuração simples para acesso ao SQLite.
 * Ajuste o caminho se quiser armazenar o banco em outro local.
 */
public class DatabaseConfig {

    private static final String DB_DIR = "data";
    private static final String DB_FILE = "ada-commerce.db";
    private static final String URL = "jdbc:sqlite:" + DB_DIR + "/" + DB_FILE;

    static {
        try {
            Path dir = Path.of(DB_DIR);
            if (Files.notExists(dir)) {
                Files.createDirectories(dir);
            }
            // Carregar driver explicitamente (opcional em JDBC moderno)
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            throw new RuntimeException("Falha ao inicializar DatabaseConfig", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}