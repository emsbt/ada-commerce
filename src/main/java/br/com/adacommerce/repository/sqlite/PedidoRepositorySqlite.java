package br.com.adacommerce.repository.sqlite;

import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.domain.cliente.Cliente;
import br.com.adacommerce.domain.pedido.*;
import br.com.adacommerce.domain.produto.Produto;
import br.com.adacommerce.repository.PedidoRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PedidoRepositorySqlite implements PedidoRepository {

    public PedidoRepositorySqlite() {
        criarTabelas();
    }

    private void criarTabelas() {
        String pedido = """
            CREATE TABLE IF NOT EXISTS pedidos(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              cliente_id INTEGER,
              data_criacao TEXT,
              status_pedido TEXT,
              status_pagamento TEXT
            )""";
        String item = """
            CREATE TABLE IF NOT EXISTS itens_pedido(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              pedido_id INTEGER,
              produto_id INTEGER,
              produto_nome_snapshot TEXT,
              quantidade INTEGER,
              preco_unitario REAL
            )""";
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement()) {
            st.execute(pedido);
            st.execute(item);
        } catch (SQLException e) {
            throw new RuntimeException("Erro criando tabelas de pedido", e);
        }
    }

    @Override
    public Pedido salvar(Pedido pedido) {
        try (Connection c = DatabaseConfig.getConnection()) {
            c.setAutoCommit(false);
            try {
                if (pedido.getId() == null) {
                    String ins = "INSERT INTO pedidos(cliente_id,data_criacao,status_pedido,status_pagamento) VALUES(?,?,?,?)";
                    try (PreparedStatement ps = c.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, pedido.getCliente().getId());
                        ps.setString(2, pedido.getDataCriacao().toString());
                        ps.setString(3, pedido.getStatus().name());
                        ps.setString(4, pedido.getStatusPagamento().name());
                        ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) pedido.setId(rs.getInt(1));
                        }
                    }
                    String insItem = "INSERT INTO itens_pedido(pedido_id,produto_id,produto_nome_snapshot,quantidade,preco_unitario) VALUES(?,?,?,?,?)";
                    try (PreparedStatement psi = c.prepareStatement(insItem)) {
                        for (ItemPedido it : pedido.getItens()) {
                            psi.setInt(1, pedido.getId());
                            psi.setInt(2, it.getProduto().getId());
                            psi.setString(3, it.getProdutoNomeSnapshot());
                            psi.setInt(4, it.getQuantidade());
                            psi.setDouble(5, it.getPrecoUnitario());
                            psi.addBatch();
                        }
                        psi.executeBatch();
                    }
                } else {
                    String upd = "UPDATE pedidos SET status_pedido=?, status_pagamento=? WHERE id=?";
                    try (PreparedStatement ps = c.prepareStatement(upd)) {
                        ps.setString(1, pedido.getStatus().name());
                        ps.setString(2, pedido.getStatusPagamento().name());
                        ps.setInt(3, pedido.getId());
                        ps.executeUpdate();
                    }
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro salvando pedido", e);
        }
        return pedido;
    }

    @Override
    public Optional<Pedido> buscarPorId(int id) {
        String sql = "SELECT * FROM pedidos WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                int clienteId = rs.getInt("cliente_id");
                // Apenas reconstrução simples (Cliente minimal)
                Cliente cliente = new Cliente("Cliente#" + clienteId, "DOC" + clienteId, "cli"+clienteId+"@mail");
                cliente.setId(clienteId);

                Pedido p = new Pedido(cliente);
                p.setId(rs.getInt("id"));
                // Status já foi salvo — para simplificar ignoramos reatribuição por reflexao
                // Carregar itens
                carregarItens(p, c);
                return Optional.of(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void carregarItens(Pedido pedido, Connection c) throws SQLException {
        String sql = "SELECT * FROM itens_pedido WHERE pedido_id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, pedido.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Produto produto = new Produto(rs.getString("produto_nome_snapshot"), rs.getDouble("preco_unitario"));
                    produto.setId(rs.getInt("produto_id"));
                    ItemPedido item = new ItemPedido(produto, rs.getInt("quantidade"), rs.getDouble("preco_unitario"));
                    item.setId(rs.getInt("id"));
                    pedido.adicionarItem(item);
                }
            }
        }
    }

    @Override
    public List<Pedido> listar() {
        String sql = "SELECT id FROM pedidos ORDER BY id DESC";
        List<Pedido> lista = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                buscarPorId(rs.getInt("id")).ifPresent(lista::add);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }

    @Override
    public void atualizarStatus(int id, String statusPedido, String statusPagamento) {
        String sql = "UPDATE pedidos SET status_pedido=?, status_pagamento=? WHERE id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, statusPedido);
            ps.setString(2, statusPagamento);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
