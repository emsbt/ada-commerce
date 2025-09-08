package br.com.adacommerce.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public class DatabaseConfig {

    private static final String DB_DIR = "data";
    private static final String DB_FILE = "ada-commerce.db";
    private static final String URL = "jdbc:sqlite:" + DB_DIR + "/" + DB_FILE;
    private static Connection connection;

    // DDLs (compatível com Java 11)
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

    private static final String TRIGGER_PEDIDO_UPDATE =
            "CREATE TRIGGER IF NOT EXISTS trg_pedido_update_timestamp " +
            "AFTER UPDATE ON pedido " +
            "FOR EACH ROW BEGIN " +
            " UPDATE pedido SET data_atualizacao = CURRENT_TIMESTAMP WHERE id = NEW.id; " +
            "END;";

    private static final String INSERT_ADMIN =
            "INSERT OR IGNORE INTO usuario (nome, email, senha, usuario) " +
            "VALUES ('Admin', 'admin@adacommerce.com', 'admin123', 'admin')";

    static {
        try {
            Path dir = Path.of(DB_DIR);
            if (Files.notExists(dir)) {
                Files.createDirectories(dir);
            }
            Class.forName("org.sqlite.JDBC");
            initialize();
        } catch (Exception e) {
            System.err.println("Falha ao inicializar DatabaseConfig: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized void initialize() {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            executarDDL(conn, DDL_USUARIO);
            garantirColunaUsuario(conn);
            executarDDL(conn, DDL_CATEGORIA);
            executarDDL(conn, DDL_CLIENTE);
            executarDDL(conn, DDL_PRODUTO);
            executarDDL(conn, DDL_PEDIDO);
            executarDDL(conn, DDL_ITEM_PEDIDO);
            executarDDL(conn, TRIGGER_PEDIDO_UPDATE);
            executarDDL(conn, INSERT_ADMIN);

            conn.commit();
            System.out.println("Estrutura de banco atualizada com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro na inicialização do banco: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void garantirColunaUsuario(Connection conn) throws SQLException {
        boolean temColuna = false;
        try (PreparedStatement ps = conn.prepareStatement("PRAGMA table_info(usuario)");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if ("usuario".equalsIgnoreCase(rs.getString("name"))) {
                    temColuna = true;
                    break;
                }
            }
        }
        if (!temColuna) {
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE usuario ADD COLUMN usuario TEXT UNIQUE");
            }
        }
    }

    private static void executarDDL(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }

    // Tornado público para uso pelos repositories / services
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
        }
        return connection;
    }
}