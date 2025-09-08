package br.com.adacommerce.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import br.com.adacommerce.config.DatabaseConfig;

public class EncodingFixer {

    // Classe auxiliar para armazenar dados temporários
    private static class UsuarioInfo {
        int id;
        String usuario;
        String nome;
        String email;
        UsuarioInfo(int id, String usuario, String nome, String email) {
            this.id = id;
            this.usuario = usuario;
            this.nome = nome;
            this.email = email;
        }
    }

    public static void corrigirEncodingUsuarios() {
        System.out.println("=== CORRIGINDO ENCODING DOS USUÁRIOS ===");
        Connection conn = null;

        try {
            conn = DatabaseConfig.getConnection();
            // Desativar autocommit para usar transação
            conn.setAutoCommit(false);

            // Primeiro lê todos os dados
            List<UsuarioInfo> usuarios = lerUsuarios(conn);

            // Depois atualiza (na mesma conexão/transação)
            atualizarUsuarios(conn, usuarios);

            // Commit da transação
            conn.commit();
            System.out.println("Encoding dos usuários corrigido com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao corrigir encoding: " + e.getMessage());
            try {
                // Rollback em caso de erro
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                // Restaurar autocommit e fechar conexão
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<UsuarioInfo> lerUsuarios(Connection conn) throws SQLException {
        List<UsuarioInfo> usuarios = new ArrayList<>();
        String sqlSelect = "SELECT id, usuario, nome, email FROM usuario";

        try (PreparedStatement selectStmt = conn.prepareStatement(sqlSelect);
             ResultSet rs = selectStmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String usuario = rs.getString("usuario");
                String nome = rs.getString("nome");
                String email = rs.getString("email");

                System.out.println("Antes - ID: " + id +
                        ", Usuário: " + usuario +
                        ", Nome: " + nome +
                        ", Email: " + email);

                String usuarioCorrigido = corrigirEncoding(usuario);
                String nomeCorrigido = corrigirEncoding(nome);
                String emailCorrigido = corrigirEncoding(email);

                System.out.println("Depois - ID: " + id +
                        ", Usuário: " + usuarioCorrigido +
                        ", Nome: " + nomeCorrigido +
                        ", Email: " + emailCorrigido);

                usuarios.add(new UsuarioInfo(id, usuarioCorrigido, nomeCorrigido, emailCorrigido));
            }
        }
        return usuarios;
    }

    private static void atualizarUsuarios(Connection conn, List<UsuarioInfo> usuarios) throws SQLException {
        String sqlUpdate = "UPDATE usuario SET usuario = ?, nome = ?, email = ? WHERE id = ?";

        try (PreparedStatement updateStmt = conn.prepareStatement(sqlUpdate)) {
            for (UsuarioInfo info : usuarios) {
                updateStmt.setString(1, info.usuario);
                updateStmt.setString(2, info.nome);
                updateStmt.setString(3, info.email);
                updateStmt.setInt(4, info.id);
                updateStmt.executeUpdate();
            }
        }
    }

    private static String corrigirEncoding(String texto) {
        if (texto == null) return null;

        try {
            // Tentativa de correção para o caso comum de Latin1 mal interpretado como UTF-8
            return new String(texto.getBytes("ISO-8859-1"), "UTF-8");
        } catch (Exception e) {
            System.err.println("Erro ao corrigir encoding: " + e.getMessage());
            return texto; // Retorna original se não conseguir corrigir
        }
    }
}