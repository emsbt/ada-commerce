package br.com.adacommerce.service;

import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.model.*;

import java.sql.*;
import java.util.*;

public class PedidoService {

    private final ProdutoService produtoService = new ProdutoService();

    public void criarTabelasSeNaoExistir() throws SQLException {
        try (Statement st = DatabaseConfig.getConnection().createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS pedido (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  numero TEXT NOT NULL,
                  cliente_id INTEGER NOT NULL,
                  data_pedido TIMESTAMP NOT NULL,
                  status TEXT NOT NULL,
                  total_bruto REAL NOT NULL,
                  desconto REAL NOT NULL,
                  total_liquido REAL NOT NULL,
                  FOREIGN KEY(cliente_id) REFERENCES cliente(id)
                );
                """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS pedido_item (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  pedido_id INTEGER NOT NULL,
                  produto_id INTEGER NOT NULL,
                  quantidade INTEGER NOT NULL,
                  preco_unitario REAL NOT NULL,
                  subtotal REAL NOT NULL,
                  FOREIGN KEY(pedido_id) REFERENCES pedido(id),
                  FOREIGN KEY(produto_id) REFERENCES produto(id)
                );
                """);
        }
    }

    public void salvarRascunho(Pedido pedido) throws SQLException {
        if (pedido.getId() == null) {
            inserirPedido(pedido);
        } else {
            atualizarCabecalho(pedido);
            excluirItens(pedido.getId());
        }
        inserirItens(pedido);
    }

    private void inserirPedido(Pedido p) throws SQLException {
        String sql = """
            INSERT INTO pedido (numero, cliente_id, data_pedido, status, total_bruto, desconto, total_liquido)
            VALUES (?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNumero());
            ps.setInt(2, p.getCliente().getId());
            ps.setTimestamp(3, new Timestamp(p.getDataPedido().getTime()));
            ps.setString(4, p.getStatus().name());
            ps.setDouble(5, p.getTotalBruto());
            ps.setDouble(6, p.getDesconto());
            ps.setDouble(7, p.getTotalLiquido());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
        }
    }

    private void atualizarCabecalho(Pedido p) throws SQLException {
        String sql = """
            UPDATE pedido SET numero=?, cliente_id=?, data_pedido=?, status=?, total_bruto=?, desconto=?, total_liquido=?
            WHERE id=?
            """;
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, p.getNumero());
            ps.setInt(2, p.getCliente().getId());
            ps.setTimestamp(3, new Timestamp(p.getDataPedido().getTime()));
            ps.setString(4, p.getStatus().name());
            ps.setDouble(5, p.getTotalBruto());
            ps.setDouble(6, p.getDesconto());
            ps.setDouble(7, p.getTotalLiquido());
            ps.setInt(8, p.getId());
            ps.executeUpdate();
        }
    }

    private void inserirItens(Pedido p) throws SQLException {
        String sql = """
            INSERT INTO pedido_item (pedido_id, produto_id, quantidade, preco_unitario, subtotal)
            VALUES (?,?,?,?,?)
            """;
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            for (PedidoItem item : p.getItens()) {
                ps.setInt(1, p.getId());
                ps.setInt(2, item.getProduto().getId());
                ps.setInt(3, item.getQuantidade());
                ps.setDouble(4, item.getPrecoUnitario());
                ps.setDouble(5, item.getSubtotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void excluirItens(int pedidoId) throws SQLException {
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement("DELETE FROM pedido_item WHERE pedido_id=?")) {
            ps.setInt(1, pedidoId);
            ps.executeUpdate();
        }
    }

    public void confirmarPedido(Pedido p) throws SQLException {
        Connection conn = DatabaseConfig.getConnection();
        boolean auto = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            // Validar estoque
            for (PedidoItem it : p.getItens()) {
                Produto prod = produtoService.buscarPorId(it.getProduto().getId());
                if (prod == null) throw new SQLException("Produto não encontrado ID=" + it.getProduto().getId());
                if (prod.getEstoqueAtual() < it.getQuantidade()) {
                    throw new SQLException("Estoque insuficiente para produto " + prod.getNome());
                }
            }
            // Debitar estoque
            for (PedidoItem it : p.getItens()) {
                produtoService.ajustarEstoque(it.getProduto().getId(), -it.getQuantidade());
            }

            p.setStatus(PedidoStatus.CONFIRMADO);
            p.recalcularTotais();
            salvarRascunho(p); // reaproveita lógica (atualiza status e itens)

            conn.commit();
        } catch (Exception ex) {
            conn.rollback();
            throw new SQLException("Falha ao confirmar pedido: " + ex.getMessage(), ex);
        } finally {
            conn.setAutoCommit(auto);
        }
    }

    public void cancelarPedido(Pedido p) throws SQLException {
        if (p.getStatus() != PedidoStatus.CONFIRMADO) {
            p.setStatus(PedidoStatus.CANCELADO);
            salvarRascunho(p);
            return;
        }
        Connection conn = DatabaseConfig.getConnection();
        boolean auto = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            // Repor estoque
            for (PedidoItem it : p.getItens()) {
                produtoService.ajustarEstoque(it.getProduto().getId(), it.getQuantidade());
            }
            p.setStatus(PedidoStatus.CANCELADO);
            salvarRascunho(p);

            conn.commit();
        } catch (Exception ex) {
            conn.rollback();
            throw new SQLException("Erro ao cancelar pedido: " + ex.getMessage(), ex);
        } finally {
            conn.setAutoCommit(auto);
        }
    }
}