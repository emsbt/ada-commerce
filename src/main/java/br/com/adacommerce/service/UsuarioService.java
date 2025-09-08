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

    public void atualizar(Usuario u) throws SQLException {
        // Verifica se já existe outro usuário com este email
        String checkSql = "SELECT id FROM usuario WHERE email=? AND id<>?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement check = c.prepareStatement(checkSql)) {
            check.setString(1, u.getEmail());
            check.setInt(2, u.getId());
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) throw new SQLException("Já existe outro usuário com este e-mail.");
            }
        }
        String sql = "UPDATE usuario SET nome=?, email=?, senha=?, usuario=?, ativo=? WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getSenha());
            ps.setString(4, u.getUsuario());
            ps.setInt(5, u.isAtivo() ? 1 : 0);
            ps.setInt(6, u.getId());
            ps.executeUpdate();
        }
    }

    public void salvar(Usuario u) throws SQLException {
        // Verifica se já existe usuário com este email
        String checkSql = "SELECT id FROM usuario WHERE email=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement check = c.prepareStatement(checkSql)) {
            check.setString(1, u.getEmail());
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) throw new SQLException("Já existe usuário com este e-mail.");
            }
        }
        String sql = "INSERT INTO usuario (nome,email,senha,usuario,ativo) VALUES (?,?,?,?,1)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getSenha());
            ps.setString(4, u.getUsuario());
            ps.executeUpdate();
        }
    }
}