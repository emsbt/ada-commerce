package br.com.adacommerce.service;

import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.model.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaService {

    public void salvar(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categoria (nome, descricao, categoria_pai_id, ativo, data_criacao, data_atualizacao) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = DatabaseConfig.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, categoria.getNome());
            stmt.setString(2, categoria.getDescricao());
            stmt.setObject(3, categoria.getCategoriaPai() != null ? categoria.getCategoriaPai().getId() : null);
            stmt.setBoolean(4, categoria.isAtivo());
            stmt.setTimestamp(5, new Timestamp(categoria.getDataCriacao().getTime()));
            stmt.setTimestamp(6, new Timestamp(categoria.getDataAtualizacao().getTime()));

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    categoria.setId(rs.getInt(1));
                }
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

            // Resolver relações de categoria pai (opcionalmente otimizar depois)
            for (Categoria categoria : categorias) {
                try {
                    Integer paiId = buscarCategoriaPaiId(categoria.getId());
                    if (paiId != null) {
                        categoria.setCategoriaPai(
                                categorias.stream()
                                        .filter(c -> c.getId() == paiId)
                                        .findFirst()
                                        .orElse(null)
                        );
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categorias;
    }

    public void excluir(int id) throws SQLException {
        // Verifica se há subcategorias
        try (PreparedStatement check = DatabaseConfig.getConnection()
                .prepareStatement("SELECT COUNT(*) FROM categoria WHERE categoria_pai_id = ?")) {
            check.setInt(1, id);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Existe(m) subcategoria(s). Remova-as antes de excluir esta.");
                }
            }
        }

        // Exclui a categoria
        try (PreparedStatement stmt = DatabaseConfig.getConnection()
                .prepareStatement("DELETE FROM categoria WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Integer buscarCategoriaPaiId(int categoriaId) throws SQLException {
        String sql = "SELECT categoria_pai_id FROM categoria WHERE id = ?";
        try (PreparedStatement stmt = DatabaseConfig.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, categoriaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int paiId = rs.getInt("categoria_pai_id");
                    return rs.wasNull() ? null : paiId;
                }
            }
        }
        return null;
    }
}