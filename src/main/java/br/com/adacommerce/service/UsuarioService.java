package br.com.adacommerce.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.mindrot.jbcrypt.BCrypt;
import br.com.adacommerce.config.DatabaseConfig;

public class UsuarioService {

    /**
     * Autentica um usuário pelo email e verifica a senha.
     * Suporta fase de transição onde a senha ainda pode não estar em BCrypt.
     * @param email Email informado no login
     * @param senhaDigitada Senha em texto digitada pelo usuário
     * @return true se autenticação válida
     */
    public boolean autenticar(String email, String senhaDigitada) {
        String sql = "SELECT senha FROM usuario WHERE email = ? AND ativo = 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String senhaArmazenada = rs.getString("senha");

                // Se a senha armazenada ainda não está em formato BCrypt, faz comparação direta (legado)
                if (!isBcrypt(senhaArmazenada)) {
                    return senhaDigitada.equals(senhaArmazenada);
                }
                return BCrypt.checkpw(senhaDigitada, senhaArmazenada);
            }
        } catch (Exception e) {
            System.err.println("Erro ao autenticar usuário: " + e.getMessage());
        }
        return false;
    }

    private boolean isBcrypt(String valor) {
        if (valor == null) return false;
        return valor.startsWith("$2a$") || valor.startsWith("$2b$") || valor.startsWith("$2y$");
    }
}