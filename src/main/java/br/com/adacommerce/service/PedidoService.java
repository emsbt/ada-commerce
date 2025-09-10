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

    private void validateItensHavePrecoUnitario(Pedido p) throws SQLException {
        if (p.getItens() == null) return;
        for (ItemPedido it : p.getItens()) {
            if (it.getPrecoUnitario() <= 0) {
                throw new SQLException("Item " + (it.getProduto()!=null?it.getProduto().getNome():"?") +
                        " precisa ter preço de venda informado (>0).");
            }
        }
    }

    public void confirmarPedido(Pedido p) throws SQLException {
        if (p.getId() == null) throw new SQLException("Pedido não salvo.");
        p.setStatus(PedidoStatus.CONFIRMADO);
        atualizarStatus(p);
    }
    public void abrirPedido(Pedido p) throws SQLException {
        if (p.getId() == null) throw new SQLException("Pedido não salvo.");
        p.setStatus(PedidoStatus.ABERTO);
        atualizarStatus(p);
    }
    public void aguardarPagamento(Pedido p) throws SQLException {
        if (p.getId() == null) throw new SQLException("Pedido não salvo.");
        // valida itens e total antes de marcar aguardando pagamento
        validateItensHavePrecoUnitario(p);
        if (p.getItens() == null || p.getItens().isEmpty()) {
            throw new SQLException("Não é possível aguardar pagamento de pedido sem itens.");
        }
        // garante que totais estão atualizados
        p.recalcularTotais();
        double total = p.getTotalLiquido();
        if (total <= 0) {
            throw new SQLException("O valor do pedido deve ser maior que zero para aguardar pagamento.");
        }
        p.setStatus(PedidoStatus.AGUARDANDO_PAGAMENTO);
        atualizarStatus(p);

        // Notificação mínima (substitua por serviço de e-mail)
        System.out.println("NOTIF: Pedido " + p.getNumero() + " aguardando pagamento. Notificar cliente: " + (p.getCliente()!=null?p.getCliente().getEmail():"?"));
    }

    public void pagarPedido(Pedido p) throws SQLException {
        if (p.getId() == null) throw new SQLException("Pedido não salvo.");
        // só aceitar pagamento se estiver aguardando pagamento
        if (p.getStatus() == null || !p.getStatus().isAguardandoPagamento()) {
            throw new SQLException("Pagamento só permitido para pedidos com status AGUARDANDO_PAGAMENTO.");
        }
        p.setStatus(PedidoStatus.PAGO);
        atualizarStatus(p);

        // Notificação pagamento confirmado
        System.out.println("NOTIF: Pedido " + p.getNumero() + " pago. Notificar cliente: " + (p.getCliente()!=null?p.getCliente().getEmail():"?"));
    }

    public void finalizarPedido(Pedido p) throws SQLException {
        if (p.getId() == null) throw new SQLException("Pedido não salvo.");
        // permitir finalização (entrega) apenas se estiver pago
        if (p.getStatus() == null || !p.getStatus().isPago()) {
            throw new SQLException("Só é possível finalizar (entregar) pedidos com status PAGO.");
        }
        p.setStatus(PedidoStatus.FINALIZADO);
        atualizarStatus(p);

        // Notificação entrega realizada
        System.out.println("NOTIF: Pedido " + p.getNumero() + " finalizado/entregue. Notificar cliente: " + (p.getCliente()!=null?p.getCliente().getEmail():"?"));
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
        // tenta recuperar numero se existir, senão gera P<id>
        try {
            String numero = null;
            try { numero = rs.getString("numero"); } catch (SQLException ignore) {}
            if (numero != null && !numero.isBlank()) p.setNumero(numero);
            else p.setNumero("P" + p.getId());
        } catch (Exception ignore) {
            p.setNumero("P" + p.getId());
        }

        int cliId = 0;
        try { cliId = rs.getInt("cliente_id"); } catch (SQLException ignore) {}
        if (!rs.wasNull() && cliId != 0) {
            // monta objeto Cliente e popula com dados da tabela cliente para que o ComboBox consiga selecionar corretamente
            Cliente c = new Cliente();
            c.setId(cliId);
            Connection conn = null;
            try {
                Statement st = rs.getStatement();
                if (st != null) conn = st.getConnection();
            } catch (Exception ignore) {}
            if (conn == null) conn = DatabaseConfig.getConnection();
            try (PreparedStatement pcs = conn.prepareStatement("SELECT nome,documento,email,telefone,endereco FROM cliente WHERE id=?")) {
                pcs.setInt(1, cliId);
                try (ResultSet crs = pcs.executeQuery()) {
                    if (crs.next()) {
                        c.setNome(crs.getString("nome"));
                        c.setDocumento(crs.getString("documento"));
                        c.setEmail(crs.getString("email"));
                        c.setTelefone(crs.getString("telefone"));
                        c.setEndereco(crs.getString("endereco"));
                    }
                }
            } catch (SQLException ignore) {
                // se falhar, ainda retornamos cliente com id apenas (a controller já trata seleção por id)
            }
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

        String statusStr = null;
        try { statusStr = rs.getString("status_pedido"); } catch (SQLException ignore) {}
        if (statusStr != null) {
            try { p.setStatus(PedidoStatus.valueOf(statusStr)); } catch (IllegalArgumentException ignore) {}
        }

        // tenta ler desconto/totais se existirem na tabela
        try {
            double desconto = rs.getDouble("desconto");
            if (!rs.wasNull()) p.setDesconto(desconto);
        } catch (SQLException ignore) {}
        try {
            double tb = rs.getDouble("total_bruto");
            if (!rs.wasNull()) p.setTotalBruto(tb);
        } catch (SQLException ignore) {}
        try {
            double tl = rs.getDouble("total_liquido");
            if (!rs.wasNull()) p.setTotalLiquido(tl);
        } catch (SQLException ignore) {}

        return p;
    }

    private void inserir(Pedido p) throws SQLException {
        String sql = """
        INSERT INTO pedido (cliente_id,data_criacao,status_pedido,status_pagamento,desconto,total_bruto,total_liquido,numero)
        VALUES (?,?,?,?,?,?,?,?)
        """;
        // Garanta que a data sempre seja preenchida!
        if (p.getDataPedido() == null) p.setDataPedido(new Date());
        if (p.getStatus() == null) p.setStatus(PedidoStatus.RASCUNHO);
        // recalcula totais antes de persistir
        p.recalcularTotais();
        // valida itens (se houver)
        validateItensHavePrecoUnitario(p);
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
            ps.setDouble(5, p.getDesconto());
            ps.setDouble(6, p.getTotalBruto());
            ps.setDouble(7, p.getTotalLiquido());
            ps.setString(8, p.getNumero() != null ? p.getNumero() : "P" + System.currentTimeMillis());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
        }
        inserirItens(p);
    }

    private void atualizarCabecalho(Pedido p) throws SQLException {
        String sql = """
            UPDATE pedido SET cliente_id=?, status_pedido=?, status_pagamento=?, desconto=?, total_bruto=?, total_liquido=? WHERE id=?
            """;
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            if (p.getCliente()!=null && p.getCliente().getId()!=null)
                ps.setInt(1, p.getCliente().getId());
            else
                ps.setNull(1, Types.INTEGER);
            ps.setString(2, p.getStatus().name());
            ps.setString(3, "PENDENTE");
            p.recalcularTotais();
            ps.setDouble(4, p.getDesconto());
            ps.setDouble(5, p.getTotalBruto());
            ps.setDouble(6, p.getTotalLiquido());
            ps.setInt(7, p.getId());
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
        // valida antes de inserir
        validateItensHavePrecoUnitario(p);
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