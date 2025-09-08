package br.com.adacommerce.report;

import br.com.adacommerce.config.DatabaseConfig;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportService {

    public List<ReportRow> generate(ReportType type,
                                    LocalDate inicio,
                                    LocalDate fim,
                                    Double valorOpcional,
                                    Integer limite) throws SQLException {
        if (inicio == null || fim == null) throw new IllegalArgumentException("Datas início e fim são obrigatórias");
        if (fim.isBefore(inicio)) throw new IllegalArgumentException("Data final antes da inicial");

        return switch (type) {
            case FATURAMENTO_POR_DIA -> faturamentoPorDia(inicio, fim);
            case TICKET_MEDIO -> ticketMedioPorDia(inicio, fim);
            case PRODUTOS_MAIS_VENDIDOS -> produtosMaisVendidos(inicio, fim, limite != null? limite:10);
            case RANKING_CLIENTES -> rankingClientes(inicio, fim, limite != null? limite:10);
            case PEDIDOS_POR_STATUS -> pedidosPorStatus(inicio, fim);
            case VENDAS_POR_CLIENTE -> vendasPorClienteDetalhado(inicio, fim, valorOpcional);
        };
    }

    private List<ReportRow> faturamentoPorDia(LocalDate ini, LocalDate fim) throws SQLException {
        String sql = """
            SELECT date(p.data_criacao) dia,
                   SUM(i.quantidade * i.preco_unitario) faturamento
            FROM pedido p
            JOIN itens_pedido i ON i.pedido_id = p.id
            WHERE p.status_pedido='CONFIRMADO'
              AND date(p.data_criacao) BETWEEN ? AND ?
            GROUP BY date(p.data_criacao)
            ORDER BY dia
            """;
        List<ReportRow> out = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, ini.toString());
            ps.setString(2, fim.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRow r = new ReportRow();
                    r.put("Dia", rs.getString("dia"));
                    r.put("Faturamento", rs.getDouble("faturamento"));
                    out.add(r);
                }
            }
        }
        return out;
    }

    private List<ReportRow> ticketMedioPorDia(LocalDate ini, LocalDate fim) throws SQLException {
        String sql = """
            SELECT date(p.data_criacao) dia,
                   COUNT(DISTINCT p.id) pedidos,
                   SUM(i.quantidade * i.preco_unitario) faturamento,
                   ROUND(SUM(i.quantidade*i.preco_unitario) / NULLIF(COUNT(DISTINCT p.id),0),2) ticket_medio
            FROM pedido p
            JOIN itens_pedido i ON i.pedido_id = p.id
            WHERE p.status_pedido='CONFIRMADO'
              AND date(p.data_criacao) BETWEEN ? AND ?
            GROUP BY date(p.data_criacao)
            ORDER BY dia
            """;
        List<ReportRow> out = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, ini.toString());
            ps.setString(2, fim.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRow r = new ReportRow();
                    r.put("Dia", rs.getString("dia"));
                    r.put("Pedidos", rs.getInt("pedidos"));
                    r.put("Faturamento", rs.getDouble("faturamento"));
                    r.put("Ticket Médio", rs.getDouble("ticket_medio"));
                    out.add(r);
                }
            }
        }
        return out;
    }

    private List<ReportRow> produtosMaisVendidos(LocalDate ini, LocalDate fim, int limit) throws SQLException {
        String sql = """
            SELECT i.produto_id,
                   i.produto_nome_snapshot nome,
                   SUM(i.quantidade) total_qtd,
                   SUM(i.quantidade * i.preco_unitario) total_venda
            FROM pedido p
            JOIN itens_pedido i ON i.pedido_id = p.id
            WHERE p.status_pedido='CONFIRMADO'
              AND date(p.data_criacao) BETWEEN ? AND ?
            GROUP BY i.produto_id, i.produto_nome_snapshot
            ORDER BY total_qtd DESC
            LIMIT ?
            """;
        List<ReportRow> out = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, ini.toString());
            ps.setString(2, fim.toString());
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRow r = new ReportRow();
                    r.put("Produto ID", rs.getInt("produto_id"));
                    r.put("Nome", rs.getString("nome"));
                    r.put("Quantidade", rs.getInt("total_qtd"));
                    r.put("Total Venda", rs.getDouble("total_venda"));
                    out.add(r);
                }
            }
        }
        return out;
    }

    private List<ReportRow> rankingClientes(LocalDate ini, LocalDate fim, int limit) throws SQLException {
        String sql = """
            SELECT p.cliente_id,
                   COALESCE(c.nome,'(sem cliente)') nome,
                   COUNT(DISTINCT p.id) pedidos,
                   SUM(i.quantidade * i.preco_unitario) total
            FROM pedido p
            LEFT JOIN cliente c ON c.id = p.cliente_id
            JOIN itens_pedido i ON i.pedido_id = p.id
            WHERE p.status_pedido='CONFIRMADO'
              AND date(p.data_criacao) BETWEEN ? AND ?
            GROUP BY p.cliente_id, nome
            ORDER BY total DESC
            LIMIT ?
            """;
        List<ReportRow> out = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, ini.toString());
            ps.setString(2, fim.toString());
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRow r = new ReportRow();
                    r.put("Cliente ID", rs.getInt("cliente_id"));
                    r.put("Nome", rs.getString("nome"));
                    r.put("Pedidos", rs.getInt("pedidos"));
                    r.put("Total", rs.getDouble("total"));
                    out.add(r);
                }
            }
        }
        return out;
    }

    private List<ReportRow> pedidosPorStatus(LocalDate ini, LocalDate fim) throws SQLException {
        String sql = """
            SELECT p.status_pedido status,
                   COUNT(DISTINCT p.id) qtd,
                   SUM(i.quantidade * i.preco_unitario) faturamento
            FROM pedido p
            LEFT JOIN itens_pedido i ON i.pedido_id = p.id
            WHERE date(p.data_criacao) BETWEEN ? AND ?
            GROUP BY p.status_pedido
            ORDER BY qtd DESC
            """;
        List<ReportRow> out = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, ini.toString());
            ps.setString(2, fim.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRow r = new ReportRow();
                    r.put("Status", rs.getString("status"));
                    r.put("Pedidos", rs.getInt("qtd"));
                    r.put("Faturamento", rs.getDouble("faturamento"));
                    out.add(r);
                }
            }
        }
        return out;
    }

    private List<ReportRow> vendasPorClienteDetalhado(LocalDate ini, LocalDate fim, Double minimo) throws SQLException {
        String sql = """
            SELECT p.id pedido_id,
                   date(p.data_criacao) dia,
                   COALESCE(c.nome,'(sem cliente)') cliente,
                   SUM(i.quantidade * i.preco_unitario) total
            FROM pedido p
            JOIN itens_pedido i ON i.pedido_id = p.id
            LEFT JOIN cliente c ON c.id = p.cliente_id
            WHERE p.status_pedido='CONFIRMADO'
              AND date(p.data_criacao) BETWEEN ? AND ?
            GROUP BY p.id, dia, cliente
            HAVING (? IS NULL OR total >= ?)
            ORDER BY total DESC
            """;
        List<ReportRow> out = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, ini.toString());
            ps.setString(2, fim.toString());
            if (minimo == null) {
                ps.setNull(3, Types.DOUBLE);
                ps.setNull(4, Types.DOUBLE);
            } else {
                ps.setDouble(3, minimo);
                ps.setDouble(4, minimo);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRow r = new ReportRow();
                    r.put("Pedido ID", rs.getInt("pedido_id"));
                    r.put("Dia", rs.getString("dia"));
                    r.put("Cliente", rs.getString("cliente"));
                    r.put("Total", rs.getDouble("total"));
                    out.add(r);
                }
            }
        }
        return out;
    }
}