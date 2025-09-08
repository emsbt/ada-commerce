package br.com.adacommerce.report;

import br.com.adacommerce.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            case PRODUTOS_MAIS_VENDIDOS -> produtosMaisVendidos(inicio, fim, limite != null ? limite : 10);
            case RANKING_CLIENTES -> rankingClientes(inicio, fim, limite != null ? limite : 10);
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
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
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
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
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

    private List<ReportRow> produtosMaisVendidos(LocalDate ini, LocalDate fim, int limite) throws SQLException {
        String sql = """
            SELECT p.nome,
                   SUM(i.quantidade) quantidade,
                   SUM(i.quantidade * i.preco_unitario) total
            FROM itens_pedido i
            JOIN pedido p2 ON p2.id = i.pedido_id
            JOIN produto p ON p.id = i.produto_id
            WHERE date(p2.data_criacao) BETWEEN ? AND ?
            GROUP BY p.nome
            ORDER BY quantidade DESC
            LIMIT ?
            """;
        List<ReportRow> out = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ini.toString());
            ps.setString(2, fim.toString());
            ps.setInt(3, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRow r = new ReportRow();
                    r.put("Produto", rs.getString("nome"));
                    r.put("Quantidade", rs.getInt("quantidade"));
                    r.put("Total", rs.getDouble("total"));
                    out.add(r);
                }
            }
        }
        return out;
    }

    private List<ReportRow> rankingClientes(LocalDate ini, LocalDate fim, int limite) throws SQLException {
        String sql = """
            SELECT c.nome,
                   SUM(i.quantidade * i.preco_unitario) total
            FROM pedido p
            JOIN cliente c ON c.id = p.cliente_id
            JOIN itens_pedido i ON i.pedido_id = p.id
            WHERE date(p.data_criacao) BETWEEN ? AND ?
              AND p.status_pedido='CONFIRMADO'
            GROUP BY c.nome
            ORDER BY total DESC
            LIMIT ?
            """;
        List<ReportRow> out = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ini.toString());
            ps.setString(2, fim.toString());
            ps.setInt(3, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRow r = new ReportRow();
                    r.put("Cliente", rs.getString("nome"));
                    r.put("Total", rs.getDouble("total"));
                    out.add(r);
                }
            }
        }
        return out;
    }

    private List<ReportRow> pedidosPorStatus(LocalDate ini, LocalDate fim) throws SQLException {
        String sql = """
            SELECT status_pedido,
                   COUNT(*) quantidade
            FROM pedido
            WHERE date(data_criacao) BETWEEN ? AND ?
            GROUP BY status_pedido
            ORDER BY status_pedido
            """;
        List<ReportRow> out = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ini.toString());
            ps.setString(2, fim.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRow r = new ReportRow();
                    r.put("Status", rs.getString("status_pedido"));
                    r.put("Quantidade", rs.getInt("quantidade"));
                    out.add(r);
                }
            }
        }
        return out;
    }

    private List<ReportRow> vendasPorClienteDetalhado(LocalDate ini, LocalDate fim, Double valorMinimo) throws SQLException {
        String base = """
            SELECT c.nome cliente,
                   p.id pedido_id,
                   date(p.data_criacao) dia,
                   SUM(i.quantidade * i.preco_unitario) valor_pedido
            FROM pedido p
            JOIN cliente c ON c.id = p.cliente_id
            JOIN itens_pedido i ON i.pedido_id = p.id
            WHERE date(p.data_criacao) BETWEEN ? AND ?
            GROUP BY c.nome, p.id, date(p.data_criacao)
            HAVING 1=1
            """;
        if (valorMinimo != null) {
            base += " AND valor_pedido >= ? ";
        }
        base += " ORDER BY valor_pedido DESC";
        List<ReportRow> out = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(base)) {
            ps.setString(1, ini.toString());
            ps.setString(2, fim.toString());
            if (valorMinimo != null) ps.setDouble(3, valorMinimo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportRow r = new ReportRow();
                    r.put("Cliente", rs.getString("cliente"));
                    r.put("Pedido", rs.getInt("pedido_id"));
                    r.put("Dia", rs.getString("dia"));
                    r.put("Valor Pedido", rs.getDouble("valor_pedido"));
                    out.add(r);
                }
            }
        }
        return out;
    }
}