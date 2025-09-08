package br.com.adacommerce.dao;

import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.model.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoriaDao {

    public List<Categoria> listar() {
        String sql = "SELECT id, nome, descricao, categoria_pai_id, ativo, data_criacao, data_atualizacao FROM categoria ORDER BY nome";
        List<Categoria> categorias = new ArrayList<>();

        // 1. Carrega base
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Categoria cat = new Categoria();
                cat.setId(rs.getInt("id"));
                cat.setNome(rs.getString("nome"));
                cat.setDescricao(rs.getString("descricao"));
                cat.setAtivo(rs.getBoolean("ativo"));
                cat.setDataCriacao(rs.getTimestamp("data_criacao"));
                cat.setDataAtualizacao(rs.getTimestamp("data_atualizacao"));
                // pai será preenchido depois
                int paiId = rs.getInt("categoria_pai_id");
                if (!rs.wasNull()) {
                    // guarda temporariamente dentro do objeto usando um id no map auxiliar
                    // usaremos uma lista para resolver depois
                    // truque: armazena o id do pai como atributo “descricao” temporário? (não)
                    // Melhor: mantém lista de pendências
                }
                categorias.add(cat);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro listando categorias: " + e.getMessage(), e);
        }

        // 2. Resolver referências de pai (faz outra consulta para cada que tenha pai)
        // Poderia otimizar, mas por enquanto simples:
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, nome, descricao, categoria_pai_id, ativo, data_criacao, data_atualizacao FROM categoria WHERE id=?")) {
            for (Categoria cat : categorias) {
                Integer paiId = buscarPaiId(cat.getId());
                if (paiId != null) {
                    ps.setInt(1, paiId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            Categoria pai = new Categoria();
                            pai.setId(rs.getInt("id"));
                            pai.setNome(rs.getString("nome"));
                            pai.setDescricao(rs.getString("descricao"));
                            pai.setAtivo(rs.getBoolean("ativo"));
                            pai.setDataCriacao(rs.getTimestamp("data_criacao"));
                            pai.setDataAtualizacao(rs.getTimestamp("data_atualizacao"));
                            cat.setCategoriaPai(pai);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categorias;
    }

    private Integer buscarPaiId(Integer idFilho) throws SQLException {
        String sql = "SELECT categoria_pai_id FROM categoria WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idFilho);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int pid = rs.getInt("categoria_pai_id");
                    return rs.wasNull() ? null : pid;
                }
                return null;
            }
        }
    }

    public Optional<Categoria> buscarPorId(int id) {
        String sql = "SELECT id, nome, descricao, categoria_pai_id, ativo, data_criacao, data_atualizacao FROM categoria WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Categoria cat = new Categoria();
                    cat.setId(rs.getInt("id"));
                    cat.setNome(rs.getString("nome"));
                    cat.setDescricao(rs.getString("descricao"));
                    cat.setAtivo(rs.getBoolean("ativo"));
                    cat.setDataCriacao(rs.getTimestamp("data_criacao"));
                    cat.setDataAtualizacao(rs.getTimestamp("data_atualizacao"));

                    int pid = rs.getInt("categoria_pai_id");
                    if (!rs.wasNull()) {
                        buscarPorId(pid).ifPresent(cat::setCategoriaPai);
                    }
                    return Optional.of(cat);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando categoria id=" + id + ": " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void inserir(Categoria cat) throws SQLException {
        String sql = "INSERT INTO categoria (nome, descricao, categoria_pai_id, ativo, data_criacao, data_atualizacao) VALUES (?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cat.getNome());
            ps.setString(2, cat.getDescricao());
            if (cat.getCategoriaPai() == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, cat.getCategoriaPai().getId());
            ps.setBoolean(4, cat.isAtivo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) cat.setId(keys.getInt(1));
            }
        }
    }

    public void atualizar(Categoria cat) throws SQLException {
        String sql = "UPDATE categoria SET nome=?, descricao=?, categoria_pai_id=?, ativo=?, data_atualizacao=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cat.getNome());
            ps.setString(2, cat.getDescricao());
            if (cat.getCategoriaPai() == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, cat.getCategoriaPai().getId());
            ps.setBoolean(4, cat.isAtivo());
            ps.setInt(5, cat.getId());
            ps.executeUpdate();
        }
    }

    public boolean podeExcluir(int id) throws SQLException {
        // Checa se tem filhos
        String sql = "SELECT COUNT(*) FROM categoria WHERE categoria_pai_id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 0;
            }
        }
    }

    public void excluir(int id) throws SQLException {
        if (!podeExcluir(id)) {
            throw new IllegalStateException("Existem subcategorias. Exclua ou remova a relação antes.");
        }
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM categoria WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}