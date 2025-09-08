package br.com.adacommerce.service;

import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClienteService {

    public void salvar(Cliente c) throws SQLException {
        if (c.getId() == null) {
            inserir(c);
        } else {
            atualizar(c);
        }
    }

    private void inserir(Cliente c) throws SQLException {
        String sql = """
            INSERT INTO cliente (nome,email,documento,telefone,ativo,data_criacao,data_atualizacao)
            VALUES (?,?,?,?,?,?,?)
            """;
        Date agora = new Date();
        if (c.getDataCriacao() == null) c.setDataCriacao(agora);
        c.setDataAtualizacao(agora);
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getDocumento());
            ps.setString(4, c.getTelefone());
            ps.setInt(5, c.isAtivo() ? 1 : 0);
            ps.setTimestamp(6, new Timestamp(c.getDataCriacao().getTime()));
            ps.setTimestamp(7, new Timestamp(c.getDataAtualizacao().getTime()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) c.setId(rs.getInt(1));
            }
        }
    }

    public void atualizar(Cliente c) throws SQLException {
        String sql = """
            UPDATE cliente SET nome=?, email=?, documento=?, telefone=?, ativo=?, data_atualizacao=?
            WHERE id=?
            """;
        c.setDataAtualizacao(new Date());
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getDocumento());
            ps.setString(4, c.getTelefone());
            ps.setInt(5, c.isAtivo() ? 1 : 0);
            ps.setTimestamp(6, new Timestamp(c.getDataAtualizacao().getTime()));
            ps.setInt(7, c.getId());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement("DELETE FROM cliente WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Cliente> listarTodos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM cliente ORDER BY nome";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cliente c = new Cliente();
                c.setId(rs.getInt("id"));
                c.setNome(rs.getString("nome"));
                c.setEmail(rs.getString("email"));
                c.setDocumento(rs.getString("documento"));
                c.setTelefone(rs.getString("telefone"));
                c.setAtivo(rs.getInt("ativo") == 1);
                c.setDataCriacao(rs.getTimestamp("data_criacao"));
                c.setDataAtualizacao(rs.getTimestamp("data_atualizacao"));
                lista.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}