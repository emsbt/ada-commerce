package br.com.adacommerce.service;

import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.model.Categoria;
import br.com.adacommerce.model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProdutoService {

    public void criarTabelasSeNaoExistir() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS produto (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  nome TEXT NOT NULL,
                  descricao TEXT,
                  categoria_id INTEGER,
                  preco REAL NOT NULL,
                  estoque_atual INTEGER NOT NULL DEFAULT 0,
                  ativo INTEGER NOT NULL DEFAULT 1,
                  data_criacao TIMESTAMP NOT NULL,
                  data_atualizacao TIMESTAMP NOT NULL,
                  FOREIGN KEY(categoria_id) REFERENCES categoria(id)
                );
                """;
        try (Statement st = DatabaseConfig.getConnection().createStatement()) {
            st.execute(sql);
        }
    }

    public void salvar(Produto p) throws SQLException {
        String sql = """
            INSERT INTO produto (nome, descricao, categoria_id, preco, estoque_atual, ativo, data_criacao, data_atualizacao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getDescricao());
            ps.setObject(3, p.getCategoria() != null ? p.getCategoria().getId() : null);
            ps.setDouble(4, p.getPreco());
            ps.setInt(5, p.getEstoqueAtual());
            ps.setBoolean(6, p.isAtivo());
            Timestamp agora = new Timestamp(new Date().getTime());
            ps.setTimestamp(7, agora);
            ps.setTimestamp(8, agora);
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
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getDescricao());
            ps.setObject(3, p.getCategoria() != null ? p.getCategoria().getId() : null);
            ps.setDouble(4, p.getPreco());
            ps.setInt(5, p.getEstoqueAtual());
            ps.setBoolean(6, p.isAtivo());
            ps.setTimestamp(7, new Timestamp(new Date().getTime()));
            ps.setInt(8, p.getId());
            ps.executeUpdate();
        }
    }

    public List<Produto> listarTodos() {
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT * FROM produto ORDER BY nome";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Produto p = new Produto();
                p.setId(rs.getInt("id"));
                p.setNome(rs.getString("nome"));
                p.setDescricao(rs.getString("descricao"));
                p.setPreco(rs.getDouble("preco"));
                p.setEstoqueAtual(rs.getInt("estoque_atual"));
                p.setAtivo(rs.getBoolean("ativo"));
                p.setDataCriacao(rs.getTimestamp("data_criacao"));
                p.setDataAtualizacao(rs.getTimestamp("data_atualizacao"));
                // categoria lazy: você pode resolver depois se quiser (JOIN)
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public void excluir(int id) throws SQLException {
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement("DELETE FROM produto WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Produto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM produto WHERE id=?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Produto p = new Produto();
                    p.setId(rs.getInt("id"));
                    p.setNome(rs.getString("nome"));
                    return p;
                }
            }
        }
        return null;
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
}