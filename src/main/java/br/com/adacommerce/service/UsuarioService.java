package br.com.adacommerce.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.mindrot.jbcrypt.BCrypt;
import br.com.adacommerce.config.DatabaseConfig;

public class UsuarioService {

    public boolean autenticar(String usuario, String senha) {
        String sql = "SELECT senha FROM usuario WHERE usuario = ? AND ativo = true";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, usuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String senhaHash = rs.getString("senha");
                return BCrypt.checkpw(senha, senhaHash);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}