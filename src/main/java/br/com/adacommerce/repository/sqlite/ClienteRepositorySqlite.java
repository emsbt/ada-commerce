package br.com.adacommerce.repository.sqlite;

import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.domain.cliente.Cliente;
import br.com.adacommerce.repository.ClienteRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClienteRepositorySqlite implements ClienteRepository {

    public ClienteRepositorySqlite() {
        criarTabelaSeNaoExistir();
    }

    private void criarTabelaSeNaoExistir() {
        String sql = """
            CREATE TABLE IF NOT EXISTS clientes(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              nome TEXT,
              documento TEXT UNIQUE,
              email TEXT,
              telefone TEXT,
              endereco TEXT,
              ativo INTEGER,
              data_cadastro TEXT
            )""";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro criando tabela clientes", e);
        }
    }

    @Override
    public Cliente salvar(Cliente cli) {
        if (cli.getId() == null) {
            String ins = "INSERT INTO clientes(nome,documento,email,telefone,endereco,ativo,data_cadastro) VALUES(?,?,?,?,?,?,?)";
            try (Connection c = DatabaseConfig.getConnection();
                 PreparedStatement ps = c.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, cli.getNome());
                ps.setString(2, cli.getDocumento());
                ps.setString(3, cli.getEmail());
                ps.setString(4, cli.getTelefone());
                ps.setString(5, cli.getEndereco());
                ps.setInt(6, cli.isAtivo() ? 1 : 0);
                ps.setString(7, cli.getDataCadastro().toString());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) cli.setId(rs.getInt(1));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Erro inserindo cliente", e);
            }
        } else {
            String upd = "UPDATE clientes SET nome=?, email=?, telefone=?, endereco=?, ativo=? WHERE id=?";
            try (Connection c = DatabaseConfig.getConnection();
                 PreparedStatement ps = c.prepareStatement(upd)) {
                ps.setString(1, cli.getNome());
                ps.setString(2, cli.getEmail());
                ps.setString(3, cli.getTelefone());
                ps.setString(4, cli.getEndereco());
                ps.setInt(5, cli.isAtivo() ? 1 : 0);
                ps.setInt(6, cli.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Erro atualizando cliente", e);
            }
        }
        return cli;
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) {
        String sql = "SELECT * FROM clientes WHERE id=?";
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
    public List<Cliente> listar() {
        String sql = "SELECT * FROM clientes";
        List<Cliente> lista = new ArrayList<>();
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
    public List<Cliente> buscarPorNome(String parte) {
        String sql = "SELECT * FROM clientes WHERE nome LIKE ?";
        List<Cliente> lista = new ArrayList<>();
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
        String sql = "UPDATE clientes SET ativo=0 WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Cliente map(ResultSet rs) throws SQLException {
        Cliente c = new Cliente(
                rs.getString("nome"),
                rs.getString("documento"),
                rs.getString("email")
        );
        c.setId(rs.getInt("id"));
        return c;
    }
}
