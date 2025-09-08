package br.com.adacommerce.service;

import br.com.adacommerce.model.Usuario;
import br.com.adacommerce.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioService {
    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY id DESC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNome(rs.getString("nome"));
                u.setEmail(rs.getString("email"));
                u.setUsuario(rs.getString("usuario"));
                u.setSenha(rs.getString("senha"));
                u.setAtivo(rs.getInt("ativo") == 1);
                lista.add(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public void salvar(Usuario u) {
        String sql = "INSERT INTO usuario (nome,email,senha,usuario,ativo) VALUES (?,?,?,?,1)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getSenha());
            ps.setString(4, u.getUsuario());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}