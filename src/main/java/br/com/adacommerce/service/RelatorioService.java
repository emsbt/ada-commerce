package br.com.adacommerce.service;

import br.com.adacommerce.config.DatabaseConfig;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Serviço que executa consultas de relatório retornando listas de Map (coluna -> valor).
 */
public class RelatorioService {

    private Connection conn() {
        try {
            return DatabaseConfig.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao obter conexão: " + e.getMessage(), e);
        }
    }

    public enum TipoRelatorio {
        VENDAS_POR_DIA(1, "Vendas por Dia",
                "Soma dos pedidos CONFIRMADOS agrupados pela data do pedido."),
        PRODUTOS_MAIS_VENDIDOS(2, "Produtos mais vendidos",
                "Top produtos por quantidade no período."),
        CLIENTES_TOP(3, "Clientes que mais compram",
                "Clientes ordenados pelo valor total comprado."),
        ESTOQUE_BAIXO(4, "Estoque Baixo",
                "Produtos com estoque menor ou igual ao limite informado no campo Valor."),
        PEDIDOS_POR_STATUS(5, "Pedidos por Status",
                "Quantidade e total agrupados por status no período escolhido."),
        TICKET_MEDIO_DIA(6, "Ticket Médio por Dia",
                "Valor médio (total/qtde pedidos) por dia em pedidos CONFIRMADOS.");

        private final int id;
        private final String titulo;
        private final String descricao;

        TipoRelatorio(int id, String titulo, String descricao) {
            this.id = id;
            this.titulo = titulo;
            this.descricao = descricao;
        }
        public int getId() { return id; }
        public String getTitulo() { return titulo; }
        public String getDescricao() { return descricao; }

        public static Optional<TipoRelatorio> byId(int id) {
            return Arrays.stream(values()).filter(t -> t.id == id).findFirst();
        }
    }

    public List<Map<String,Object>> gerar(TipoRelatorio tipo,
                                          LocalDate inicio,
                                          LocalDate fim,
                                          String parametro) throws SQLException {
        return switch (tipo) {
            case VENDAS_POR_DIA -> vendasPorDia(inicio, fim);
            case PRODUTOS_MAIS_VENDIDOS -> produtosMaisVendidos(inicio, fim);
            case CLIENTES_TOP -> clientesTop(inicio, fim);
            case ESTOQUE_BAIXO -> estoqueBaixo(parametro);
            case PEDIDOS_POR_STATUS -> pedidosPorStatus(inicio, fim);
            case TICKET_MEDIO_DIA -> ticketMedioPorDia(inicio, fim);
        };
    }

    private List<Map<String,Object>> vendasPorDia(LocalDate ini, LocalDate fim) throws SQLException {
        String sql = """
            SELECT date(p.data_pedido) AS dia,
                   COUNT(p.id) AS qt_pedidos,
                   SUM(p.total_liquido) AS total
            FROM pedido p
            WHERE p.status='CONFIRMADO'
              AND p.data_pedido BETWEEN ? AND ?
            GROUP BY date(p.data_pedido)
            ORDER BY dia
            """;
        return runQuery(sql, ini, fim);
    }

    private List<Map<String,Object>> produtosMaisVendidos(LocalDate ini, LocalDate fim) throws SQLException {
        String sql = """
            SELECT pr.nome AS produto,
                   SUM(i.quantidade) AS quantidade,
                   SUM(i.subtotal)  AS total
            FROM pedido_item i
            JOIN pedido p ON p.id=i.pedido_id
            JOIN produto pr ON pr.id=i.produto_id
            WHERE p.status='CONFIRMADO'
              AND p.data_pedido BETWEEN ? AND ?
            GROUP BY pr.nome
            ORDER BY quantidade DESC, total DESC
            LIMIT 50
            """;
        return runQuery(sql, ini, fim);
    }

    private List<Map<String,Object>> clientesTop(LocalDate ini, LocalDate fim) throws SQLException {
        String sql = """
            SELECT c.nome AS cliente,
                   COUNT(p.id) AS qt_pedidos,
                   SUM(p.total_liquido) AS total
            FROM pedido p
            JOIN cliente c ON c.id=p.cliente_id
            WHERE p.status='CONFIRMADO'
              AND p.data_pedido BETWEEN ? AND ?
            GROUP BY c.nome
            ORDER BY total DESC
            LIMIT 50
            """;
        return runQuery(sql, ini, fim);
    }

    private List<Map<String,Object>> estoqueBaixo(String parametro) throws SQLException {
        int limite = 10;
        if (parametro != null && parametro.matches("\\d+")) {
            limite = Integer.parseInt(parametro);
        }
        String sql = """
            SELECT nome AS produto,
                   estoque_atual AS estoque
            FROM produto
            WHERE ativo=1 AND estoque_atual <= ?
            ORDER BY estoque_atual ASC, nome
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                return mapResult(rs);
            }
        }
    }

    private List<Map<String,Object>> pedidosPorStatus(LocalDate ini, LocalDate fim) throws SQLException {
        String sql = """
            SELECT status,
                   COUNT(id) AS qt,
                   SUM(total_liquido) AS total
            FROM pedido
            WHERE data_pedido BETWEEN ? AND ?
            GROUP BY status
            ORDER BY status
            """;
        return runQuery(sql, ini, fim);
    }

    private List<Map<String,Object>> ticketMedioPorDia(LocalDate ini, LocalDate fim) throws SQLException {
        String sql = """
            SELECT date(p.data_pedido) AS dia,
                   COUNT(p.id) AS qt_pedidos,
                   SUM(p.total_liquido) AS total,
                   CASE WHEN COUNT(p.id)=0 THEN 0 ELSE SUM(p.total_liquido)/COUNT(p.id) END AS ticket_medio
            FROM pedido p
            WHERE p.status='CONFIRMADO'
              AND p.data_pedido BETWEEN ? AND ?
            GROUP BY date(p.data_pedido)
            ORDER BY dia
            """;
        return runQuery(sql, ini, fim);
    }

    private List<Map<String,Object>> runQuery(String sql, LocalDate ini, LocalDate fim) throws SQLException {
        if (ini == null || fim == null) {
            throw new SQLException("Período (início e fim) é obrigatório para este relatório.");
        }
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(ini.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(fim.plusDays(1).atStartOfDay().minusSeconds(1)));
            try (ResultSet rs = ps.executeQuery()) {
                return mapResult(rs);
            }
        }
    }

    private List<Map<String,Object>> mapResult(ResultSet rs) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        while (rs.next()) {
            Map<String,Object> row = new LinkedHashMap<>();
            for (int i=1;i<=cols;i++) {
                row.put(md.getColumnLabel(i), rs.getObject(i));
            }
            list.add(row);
        }
        return list;
    }
}