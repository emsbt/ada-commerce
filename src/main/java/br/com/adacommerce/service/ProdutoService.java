package br.com.adacommerce.service;

import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.model.Categoria;
import br.com.adacommerce.model.Produto;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ProdutoService {

    public List<Produto> listar() throws SQLException {
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT * FROM produto ORDER BY id DESC";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Produto p = mapProduto(rs);
                lista.add(p);
            }
        }
        return lista;
    }

    public void salvar(Produto p) throws SQLException {
        if (p.getId() == null) inserir(p);
        else atualizar(p);
    }

    private Produto mapProduto(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setId(rs.getInt("id"));
        p.setNome(rs.getString("nome"));
        p.setDescricao(rs.getString("descricao"));
        p.setPreco(rs.getDouble("preco"));
        p.setEstoqueAtual(rs.getInt("estoque_atual"));
        p.setAtivo(rs.getInt("ativo") == 1);

        // ler categoria_id e popular objeto Categoria (se existir)
        try {
            int catId = rs.getInt("categoria_id");
            if (!rs.wasNull() && catId != 0) {
                Categoria c = new Categoria();
                c.setId(catId);
                // opcional: popular nome/descricao da categoria para seleção no combo
                try (PreparedStatement pcs = DatabaseConfig.getConnection().prepareStatement(
                        "SELECT nome, descricao FROM categoria WHERE id=?")) {
                    pcs.setInt(1, catId);
                    try (ResultSet crs = pcs.executeQuery()) {
                        if (crs.next()) {
                            c.setNome(crs.getString("nome"));
                            c.setDescricao(crs.getString("descricao"));
                        }
                    }
                } catch (Exception ignore) {}
                p.setCategoria(c);
            }
        } catch (SQLException ignore) {}

        // data_criacao / data_atualizacao podem ser lidas se quiser
        return p;
    }

    private void inserir(Produto p) throws SQLException {
        String sql = "INSERT INTO produto (nome,descricao,categoria_id,preco,estoque_atual,ativo,data_criacao) VALUES (?,?,?,?,?,?,?)";
        if (p.getDataCriacao() == null) {
            p.setDataCriacao(new java.util.Date());
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getDescricao());
            if (p.getCategoria() != null && p.getCategoria().getId() != null) ps.setInt(3, p.getCategoria().getId());
            else ps.setNull(3, Types.INTEGER);
            ps.setDouble(4, p.getPreco());
            ps.setInt(5, p.getEstoqueAtual());
            ps.setInt(6, p.isAtivo() ? 1 : 0);
            ps.setString(7, sdf.format(p.getDataCriacao()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
        }
    }

    // tornamos público para a controller poder chamar diretamente
    public void atualizar(Produto p) throws SQLException {
        String sql = "UPDATE produto SET nome=?, descricao=?, categoria_id=?, preco=?, estoque_atual=?, ativo=?, data_atualizacao=? WHERE id=?";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getDescricao());
            if (p.getCategoria() != null && p.getCategoria().getId() != null) ps.setInt(3, p.getCategoria().getId());
            else ps.setNull(3, Types.INTEGER);
            ps.setDouble(4, p.getPreco());
            ps.setInt(5, p.getEstoqueAtual());
            ps.setInt(6, p.isAtivo() ? 1 : 0);
            ps.setString(7, sdf.format(new java.util.Date()));
            ps.setInt(8, p.getId());
            ps.executeUpdate();
        }
    }

    // método público para excluir produtos (controller chama produtoService.excluir(id))
    public void excluir(Integer id) throws SQLException {
        if (id == null) return;
        String sql = "DELETE FROM produto WHERE id=?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}