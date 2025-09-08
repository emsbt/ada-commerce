package br.com.adacommerce.report;

public enum ReportType {
    FATURAMENTO_POR_DIA("1 - Faturamento por Dia"),
    PEDIDOS_POR_STATUS("2 - Pedidos por Status"),
    PRODUTOS_MAIS_VENDIDOS("3 - Produtos Mais Vendidos"),
    RANKING_CLIENTES("4 - Ranking de Clientes"),
    TICKET_MEDIO("5 - Ticket Médio por Dia"),
    VENDAS_POR_CLIENTE("6 - Vendas por Cliente (Detalhado)");

    private final String label;

    ReportType(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }

    // NOVO:
    public String getDescricao() {
        return switch (this) {
            case FATURAMENTO_POR_DIA ->
                    "Soma do faturamento (valor = quantidade * preço_unitário dos itens confirmados) agrupada por dia.";
            case PEDIDOS_POR_STATUS ->
                    "Quantidade de pedidos e total (faturamento) agrupados por status.";
            case PRODUTOS_MAIS_VENDIDOS ->
                    "Lista os produtos mais vendidos (quantidade) no intervalo selecionado.";
            case RANKING_CLIENTES ->
                    "Ranking de clientes ordenado pelo total comprado (pedidos confirmados).";
            case TICKET_MEDIO ->
                    "Ticket médio diário = faturamento / número de pedidos confirmados do dia.";
            case VENDAS_POR_CLIENTE ->
                    "Detalhamento de vendas por cliente; pode filtrar por valor mínimo e limitar resultados.";
        };
    }
}