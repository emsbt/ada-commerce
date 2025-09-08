package br.com.adacommerce.service;

import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.model.Categoria;
import br.com.adacommerce.model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProdutoService {

    public void salvar(Produto p) throws SQLException {
        if (p.getId() == null) inserir(p); else atualizar(p);
    }

    public void inserir(Produto p) throws SQLException {
        String sql = """
          INSERT INTO produto (nome,descricao,categoria_id,preco,estoque_atual,ativo,data_criacao,data_atualizacao)
          VALUES (?,?,?,?,?,?,?,?)
          """;
        Date agora = new Date();
        if (p.getDataCriacao() == null) p.setDataCriacao(agora);
        p.setDataAtualizacao(agora);
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getDescricao());
            if (p.getCategoria() != null && p.getCategoria().getId() != null)
                ps.setInt(3, p.getCategoria().getId());
            else
                ps.setNull(3, Types.INTEGER);
            ps.setDouble(4, p.getPreco());
            ps.setInt(5, p.getEstoqueAtual());
            ps.setInt(6, p.isAtivo() ? 1 : 0);
            ps.setTimestamp(7, new Timestamp(p.getDataCriacao().getTime()));
            ps.setTimestamp(8, new Timestamp(p.getDataAtualizacao().getTime()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
        }
    }

    public void atualizar(Produto p) throws SQLException {
        String sql = """
          UPDATE produto SET nome=?, descricao=?, categoria_id=?, preco=?, estoque_atual=?, ativo=?, data_atualizacao=?
          WHERE id=?
          """;
        p.setDataAtualizacao(new Date());
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getDescricao());
            if (p.getCategoria() != null && p.getCategoria().getId() != null)
                ps.setInt(3, p.getCategoria().getId());
            else
                ps.setNull(3, Types.INTEGER);
            ps.setDouble(4, p.getPreco());
            ps.setInt(5, p.getEstoqueAtual());
            ps.setInt(6, p.isAtivo() ? 1 : 0);
            ps.setTimestamp(7, new Timestamp(p.getDataAtualizacao().getTime()));
            ps.setInt(8, p.getId());
            ps.executeUpdate();
        }
    }

    public List<Produto> listarTodos() {
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT * FROM produto ORDER BY nome";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Produto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM produto WHERE id=?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void excluir(int id) throws SQLException {
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement("DELETE FROM produto WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void ajustarEstoque(int produtoId, int delta) throws SQLException {
        String sql = "UPDATE produto SET estoque_atual = estoque_atual + ?, data_atualizacao=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, produtoId);
            ps.executeUpdate();
        }
    }

    private Produto map(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setId(rs.getInt("id"));
        p.setNome(rs.getString("nome"));
        p.setDescricao(rs.getString("descricao"));
        p.setPreco(rs.getDouble("preco"));
        p.setEstoqueAtual(rs.getInt("estoque_atual"));
        p.setAtivo(rs.getInt("ativo") == 1);
        p.setDataCriacao(rs.getTimestamp("data_criacao"));
        p.setDataAtualizacao(rs.getTimestamp("data_atualizacao"));
        int catId = rs.getInt("categoria_id");
        if (!rs.wasNull() && catId > 0) {
            Categoria c = new Categoria();
            c.setId(catId);
            p.setCategoria(c);
        }
        return p;
    }
}