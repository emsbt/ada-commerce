package br.com.adacommerce.model;

import java.math.BigDecimal;

public class ItemPedido {
    private int id;
    private Produto produto;
    private int quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal valorTotal;
    private String observacoes;
    
    // Construtores
    public ItemPedido() {
    }
    
    public ItemPedido(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = produto.getPrecoBase();
        calcularValorTotal();
    }
    
    // Métodos de negócio
    private void calcularValorTotal() {
        if (this.precoUnitario != null && this.quantidade > 0) {
            this.valorTotal = this.precoUnitario.multiply(BigDecimal.valueOf(this.quantidade));
        } else {
            this.valorTotal = BigDecimal.ZERO;
        }
    }
    
    public void atualizarQuantidade(int novaQuantidade) {
        this.quantidade = novaQuantidade;
        calcularValorTotal();
    }
    
    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
        if (produto != null) {
            this.precoUnitario = produto.getPrecoBase();
            calcularValorTotal();
        }
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
        calcularValorTotal();
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
        calcularValorTotal();
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    protected void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    @Override
    public String toString() {
        return produto.getNome() + " x " + quantidade + " = R$ " + valorTotal;
    }
}