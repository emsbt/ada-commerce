package br.com.adacommerce.report;

public enum ReportType {
    FATURAMENTO_POR_DIA,
    PEDIDOS_POR_STATUS,
    PRODUTOS_MAIS_VENDIDOS,
    RANKING_CLIENTES,
    TICKET_MEDIO,
    VENDAS_POR_CLIENTE;

    public String getDescricao() {
        return switch (this) {
            case FATURAMENTO_POR_DIA -> "Faturamento somado por dia (pedidos confirmados).";
            case PEDIDOS_POR_STATUS -> "Contagem de pedidos agrupados por status.";
            case PRODUTOS_MAIS_VENDIDOS -> "Lista de produtos mais vendidos no período.";
            case RANKING_CLIENTES -> "Clientes ordenados pelo total comprado.";
            case TICKET_MEDIO -> "Ticket médio diário (faturamento / pedidos confirmados).";
            case VENDAS_POR_CLIENTE -> "Detalhamento de vendas por cliente (filtra opcional por valor mínimo).";
        };
    }
}