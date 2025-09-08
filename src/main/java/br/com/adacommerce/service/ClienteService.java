package br.com.adacommerce.service;

import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClienteService {

    public void criarTabelasSeNaoExistir() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS cliente (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  nome TEXT NOT NULL,
                  email TEXT,
                  documento TEXT,
                  telefone TEXT,
                  ativo INTEGER NOT NULL DEFAULT 1,
                  data_criacao TIMESTAMP NOT NULL,
                  data_atualizacao TIMESTAMP NOT NULL
                );
                """;
        try (Statement st = DatabaseConfig.getConnection().createStatement()) {
            st.execute(sql);
        }
    }

    public void salvar(Cliente c) throws SQLException {
        String sql = """
            INSERT INTO cliente (nome, email, documento, telefone, ativo, data_criacao, data_atualizacao)
            VALUES (?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getDocumento());
            ps.setString(4, c.getTelefone());
            ps.setBoolean(5, c.isAtivo());
            Timestamp agora = new Timestamp(new Date().getTime());
            ps.setTimestamp(6, agora);
            ps.setTimestamp(7, agora);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) c.setId(rs.getInt(1));
            }
        }
    }

    public void atualizar(Cliente c) throws SQLException {
        String sql = """
            UPDATE cliente SET nome=?, email=?, documento=?, telefone=?, ativo=?, data_atualizacao=? WHERE id=?
            """;
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getDocumento());
            ps.setString(4, c.getTelefone());
            ps.setBoolean(5, c.isAtivo());
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            ps.setInt(7, c.getId());
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
                c.setAtivo(rs.getBoolean("ativo"));
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