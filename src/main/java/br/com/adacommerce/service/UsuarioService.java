package br.com.adacommerce.service;

import br.com.adacommerce.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Serviço de autenticação simples.
 * Aceita login via email OU usuario (texto plano, sem BCrypt).
 * Mantém fallback caso a coluna 'usuario' ainda não exista no banco.
 * IMPORTANTE: comparar senha em texto plano não é seguro para produção.
 */
public class UsuarioService {

    /**
     * Autentica usando email OU usuario.
     * @param login valor digitado (email ou usuario)
     * @param senhaDigitada senha digitada em texto
     * @return true se credenciais válidas
     */
    public boolean autenticar(String login, String senhaDigitada) {
        String sql = "SELECT senha FROM usuario WHERE ativo = 1 AND (email = ? OR usuario = ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, login);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String senhaArmazenada = rs.getString("senha");
                    return senhaDigitada != null && senhaDigitada.equals(senhaArmazenada);
                }
            }
        } catch (SQLException e) {
            // Fallback: coluna 'usuario' pode não existir ainda
            if (e.getMessage() != null && e.getMessage().contains("no such column: usuario")) {
                return autenticarSomenteEmail(login, senhaDigitada);
            }
            System.err.println("Erro ao autenticar usuário: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro inesperado na autenticação: " + e.getMessage());
        }
        return false;
    }

    // Modo legado: apenas email
    private boolean autenticarSomenteEmail(String email, String senhaDigitada) {
        String sql = "SELECT senha FROM usuario WHERE email = ? AND ativo = 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String senhaArmazenada = rs.getString("senha");
                    return senhaDigitada != null && senhaDigitada.equals(senhaArmazenada);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao autenticar (fallback email): " + e.getMessage());
        }
        return false;
    }
}