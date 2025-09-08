package br.com.adacommerce.service;

import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.model.*;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PedidoService {

    public List<Pedido> listar() throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT * FROM pedido ORDER BY id DESC";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pedido p = mapPedido(rs);
                p.getItens().addAll(listarItens(p.getId()));
                p.recalcularTotais();
                lista.add(p);
            }
        }
        return lista;
    }

    public void salvarRascunho(Pedido p) throws SQLException {
        if (p.getStatus() == null) p.setStatus(PedidoStatus.RASCUNHO);
        if (p.getId() == null) {
            inserir(p);
        } else {
            atualizarCabecalho(p);
            limparItens(p.getId());
            inserirItens(p);
        }
    }

    public void confirmarPedido(Pedido p) throws SQLException {
        if (p.getId() == null) throw new SQLException("Pedido não salvo.");
        p.setStatus(PedidoStatus.CONFIRMADO);
        atualizarStatus(p);
    }

    public void cancelarPedido(Pedido p) throws SQLException {
        if (p.getId() == null) throw new SQLException("Pedido não salvo.");
        p.setStatus(PedidoStatus.CANCELADO);
        atualizarStatus(p);
    }

    /* ================= PRIVADOS ================= */

    private Pedido mapPedido(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setId(rs.getInt("id"));
        p.setNumero("P" + p.getId()); // Ajuste se tiver coluna 'numero'
        int cliId = rs.getInt("cliente_id");
        if (!rs.wasNull()) {
            Cliente c = new Cliente();
            c.setId(cliId);
            p.setCliente(c);
        }
        // Correção: aceita tanto TIMESTAMP quanto TEXT
        try {
            Timestamp ts = rs.getTimestamp("data_criacao");
            if (ts != null) {
                p.setDataPedido(new Date(ts.getTime()));
            } else {
                String txt = rs.getString("data_criacao");
                if (txt != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    p.setDataPedido(sdf.parse(txt));
                }
            }
        } catch (Exception ignore) {}
        String statusStr = rs.getString("status_pedido");
        if (statusStr != null) {
            try { p.setStatus(PedidoStatus.valueOf(statusStr)); } catch (IllegalArgumentException ignore) {}
        }
        return p;
    }

    private void inserir(Pedido p) throws SQLException {
        String sql = """
        INSERT INTO pedido (cliente_id,data_criacao,status_pedido,status_pagamento)
        VALUES (?,?,?,?)
        """;
        // Garanta que a data sempre seja preenchida!
        if (p.getDataPedido() == null) p.setDataPedido(new Date());
        if (p.getStatus() == null) p.setStatus(PedidoStatus.RASCUNHO);
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (p.getCliente()!=null && p.getCliente().getId()!=null)
                ps.setInt(1, p.getCliente().getId());
            else
                ps.setNull(1, Types.INTEGER);
            // Salva data como texto formatado
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ps.setString(2, sdf.format(p.getDataPedido()));
            ps.setString(3, p.getStatus().name());
            ps.setString(4, "PENDENTE");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
        }
        inserirItens(p);
    }

    private void atualizarCabecalho(Pedido p) throws SQLException {
        String sql = """
            UPDATE pedido SET cliente_id=?, status_pedido=?, status_pagamento=? WHERE id=?
            """;
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            if (p.getCliente()!=null && p.getCliente().getId()!=null)
                ps.setInt(1, p.getCliente().getId());
            else
                ps.setNull(1, Types.INTEGER);
            ps.setString(2, p.getStatus().name());
            ps.setString(3, "PENDENTE");
            ps.setInt(4, p.getId());
            ps.executeUpdate();
        }
    }

    private void atualizarStatus(Pedido p) throws SQLException {
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement("UPDATE pedido SET status_pedido=? WHERE id=?")) {
            ps.setString(1, p.getStatus().name());
            ps.setInt(2, p.getId());
            ps.executeUpdate();
        }
    }

    private void inserirItens(Pedido p) throws SQLException {
        if (p.getItens().isEmpty()) return;
        String sql = """
            INSERT INTO itens_pedido (pedido_id,produto_id,produto_nome_snapshot,quantidade,preco_unitario)
            VALUES (?,?,?,?,?)
            """;
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            for (ItemPedido it : p.getItens()) {
                if (it.getProduto()==null || it.getProduto().getId()==null)
                    throw new SQLException("Item sem produto.");
                ps.setInt(1, p.getId());
                ps.setInt(2, it.getProduto().getId());
                ps.setString(3, it.getProduto().getNome());
                ps.setInt(4, it.getQuantidade());
                ps.setDouble(5, it.getPrecoUnitario());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void limparItens(int pedidoId) throws SQLException {
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement("DELETE FROM itens_pedido WHERE pedido_id=?")) {
            ps.setInt(1, pedidoId);
            ps.executeUpdate();
        }
    }

    private List<ItemPedido> listarItens(int pedidoId) throws SQLException {
        List<ItemPedido> itens = new ArrayList<>();
        String sql = "SELECT * FROM itens_pedido WHERE pedido_id=?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItemPedido it = new ItemPedido();
                    it.setId(rs.getInt("id"));
                    Produto prod = new Produto();
                    prod.setId(rs.getInt("produto_id"));
                    prod.setNome(rs.getString("produto_nome_snapshot"));
                    it.setProduto(prod);
                    it.setQuantidade(rs.getInt("quantidade"));
                    it.setPrecoUnitario(rs.getDouble("preco_unitario"));
                    itens.add(it);
                }
            }
        }
        return itens;
    }
}