package br.com.adacommerce.dao;

import br.com.adacommerce.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDao {

    public String findPasswordHashByUsuario(String usuario) {
        String sql = "SELECT senha FROM usuario WHERE usuario = ? AND ativo = 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("senha");
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Erro consultando usuário: " + e.getMessage(), e);
        }
    }
}