package br.com.adacommerce.domain.pedido;

import br.com.adacommerce.domain.produto.Produto;

public class ItemPedido {
    private Integer id;
    private Produto produto;
    private int quantidade;
    private double precoUnitario;
    private String produtoNomeSnapshot; // auditoria

    public ItemPedido(Produto produto, int quantidade, double precoUnitario) {
        if (produto == null) throw new IllegalArgumentException("Produto obrigatório");
        if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser > 0");
        if (precoUnitario < 0) throw new IllegalArgumentException("Preço unitário inválido");
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.produtoNomeSnapshot = produto.getNome();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Produto getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }
    public double getSubtotal() { return quantidade * precoUnitario; }
    public String getProdutoNomeSnapshot() { return produtoNomeSnapshot; }

    public void setQuantidade(int quantidade) {
        if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser > 0");
        this.quantidade = quantidade;
    }
}
