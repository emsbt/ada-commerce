package br.com.adacommerce.repository.sqlite;

import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.domain.produto.Produto;
import br.com.adacommerce.repository.ProdutoRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProdutoRepositorySqlite implements ProdutoRepository {

    public ProdutoRepositorySqlite() { criarTabelaSeNaoExistir(); }

    private void criarTabelaSeNaoExistir() {
        String sql = """
            CREATE TABLE IF NOT EXISTS produtos(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              nome TEXT,
              preco_base REAL,
              ativo INTEGER,
              data_criacao TEXT
            )""";
        try (Connection c = DatabaseConfig.getConnection(); Statement st = c.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro criando tabela produtos", e);
        }
    }

    @Override
    public Produto salvar(Produto p) {
        if (p.getId() == null) {
            String ins = "INSERT INTO produtos(nome,preco_base,ativo,data_criacao) VALUES(?,?,?,?)";
            try (Connection c = DatabaseConfig.getConnection();
                 PreparedStatement ps = c.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, p.getNome());
                ps.setDouble(2, p.getPrecoBase());
                ps.setInt(3, p.isAtivo() ? 1 : 0);
                ps.setString(4, p.getDataCriacao().toString());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) p.setId(rs.getInt(1));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erro inserindo produto", e);
            }
        } else {
            String upd = "UPDATE produtos SET nome=?, preco_base=?, ativo=? WHERE id=?";
            try (Connection c = DatabaseConfig.getConnection();
                 PreparedStatement ps = c.prepareStatement(upd)) {
                ps.setString(1, p.getNome());
                ps.setDouble(2, p.getPrecoBase());
                ps.setInt(3, p.isAtivo() ? 1 : 0);
                ps.setInt(4, p.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Erro atualizando produto", e);
            }
        }
        return p;
    }

    @Override
    public Optional<Produto> buscarPorId(int id) {
        String sql = "SELECT * FROM produtos WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<Produto> listar() {
        String sql = "SELECT * FROM produtos";
        List<Produto> lista = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }

    @Override
    public List<Produto> buscarPorNome(String parte) {
        String sql = "SELECT * FROM produtos WHERE nome LIKE ?";
        List<Produto> lista = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + parte + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }

    @Override
    public void inativar(int id) {
        String sql = "UPDATE produtos SET ativo=0 WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Produto map(ResultSet rs) throws SQLException {
        Produto p = new Produto(
                rs.getString("nome"),
                rs.getDouble("preco_base")
        );
        p.setId(rs.getInt("id"));
        return p;
    }
}
