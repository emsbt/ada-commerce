package br.com.adacommerce.model;

public class PedidoItem {
    private Integer id;
    private Produto produto;
    private int quantidade;
    private double precoUnitario;
    private double subtotal;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) {
        this.produto = produto;
        if (produto != null && precoUnitario == 0) {
            this.precoUnitario = produto.getPreco();
        }
        recalcularSubtotal();
    }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; recalcularSubtotal(); }
    public double getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(double precoUnitario) { this.precoUnitario = precoUnitario; recalcularSubtotal(); }
    public double getSubtotal() { return subtotal; }
    private void recalcularSubtotal() { this.subtotal = precoUnitario * quantidade; }
}