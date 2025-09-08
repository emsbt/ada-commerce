package br.com.adacommerce.service;

import br.com.adacommerce.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthService {

    public boolean autenticar(String login, String senhaPura) {
        if (login == null || senhaPura == null) return false;
        String sql = "SELECT senha FROM usuario WHERE login = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login.trim());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false;
            String hash = rs.getString("senha");
            return BCrypt.checkpw(senhaPura, hash);
        } catch (Exception e) {
            throw new RuntimeException("Erro autenticando: " + e.getMessage(), e);
        }
    }
}