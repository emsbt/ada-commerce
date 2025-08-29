package br.com.adacommerce.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Pedido {
    private int id;
    private Cliente cliente;
    private List<ItemPedido> itens;
    private Date dataPedido;
    private Date dataAtualizacao;
    private String status; // NOVO, EM_PROCESSAMENTO, ENVIADO, ENTREGUE, CANCELADO
    private BigDecimal valorTotal;
    private String formaPagamento;
    private String observacoes;
    
    // Construtores
    public Pedido() {
        this.dataPedido = new Date();
        this.dataAtualizacao = new Date();
        this.status = "NOVO";
        this.itens = new ArrayList<>();
        this.valorTotal = BigDecimal.ZERO;
    }
    
    public Pedido(Cliente cliente) {
        this();
        this.cliente = cliente;
    }
    
    // Métodos de negócio
    public void adicionarItem(Produto produto, int quantidade) {
        ItemPedido item = new ItemPedido(produto, quantidade);
        itens.add(item);
        recalcularValorTotal();
    }
    
    public void removerItem(ItemPedido item) {
        itens.remove(item);
        recalcularValorTotal();
    }
    
    private void recalcularValorTotal() {
        valorTotal = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            valorTotal = valorTotal.add(item.getValorTotal());
        }
    }
    
    public void atualizarStatus(String novoStatus) {
        this.status = novoStatus;
        this.dataAtualizacao = new Date();
    }
    
    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
        recalcularValorTotal();
    }

    public Date getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(Date dataPedido) {
        this.dataPedido = dataPedido;
    }

    public Date getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(Date dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.dataAtualizacao = new Date();
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    @Override
    public String toString() {
        return "Pedido #" + id + " - Cliente: " + cliente.getNome() + " - Status: " + status;
    }
}