package br.com.adacommerce.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.model.Categoria;

public class CategoriaService {

    public void salvar(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categoria (nome, descricao, categoria_pai_id, ativo, data_criacao, data_atualizacao) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, categoria.getNome());
            stmt.setString(2, categoria.getDescricao());
            stmt.setObject(3, categoria.getCategoriaPai() != null ? categoria.getCategoriaPai().getId() : null);
            stmt.setBoolean(4, categoria.isAtivo());
            stmt.setTimestamp(5, new Timestamp(categoria.getDataCriacao().getTime()));
            stmt.setTimestamp(6, new Timestamp(categoria.getDataAtualizacao().getTime()));

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                categoria.setId(rs.getInt(1));
            }
        }
    }

    public void atualizar(Categoria categoria) throws SQLException {
        String sql = "UPDATE categoria SET nome = ?, descricao = ?, categoria_pai_id = ?, ativo = ?, data_atualizacao = ? WHERE id = ?";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setString(1, categoria.getNome());
            stmt.setString(2, categoria.getDescricao());
            stmt.setObject(3, categoria.getCategoriaPai() != null ? categoria.getCategoriaPai().getId() : null);
            stmt.setBoolean(4, categoria.isAtivo());
            stmt.setTimestamp(5, new Timestamp(categoria.getDataAtualizacao().getTime()));
            stmt.setInt(6, categoria.getId());
            stmt.executeUpdate();
        }
    }

    public List<Categoria> listarTodas() {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT * FROM categoria ORDER BY nome";

        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Categoria categoria = new Categoria();
                categoria.setId(rs.getInt("id"));
                categoria.setNome(rs.getString("nome"));
                categoria.setDescricao(rs.getString("descricao"));
                categoria.setAtivo(rs.getBoolean("ativo"));
                categoria.setDataCriacao(rs.getTimestamp("data_criacao"));
                categoria.setDataAtualizacao(rs.getTimestamp("data_atualizacao"));
                categorias.add(categoria);
            }

            for (Categoria categoria : categorias) {
                Integer paiId = buscarCategoriaPaiId(categoria.getId());
                if (paiId != null) {
                    categoria.setCategoriaPai(categorias.stream()
                            .filter(c -> c.getId() == paiId)
                            .findFirst().orElse(null));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categorias;
    }

    private Integer buscarCategoriaPaiId(int categoriaId) throws SQLException {
        String sql = "SELECT categoria_pai_id FROM categoria WHERE id = ?";
        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, categoriaId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int paiId = rs.getInt("categoria_pai_id");
                return rs.wasNull() ? null : paiId;
            }
        }
        return null;
    }
}