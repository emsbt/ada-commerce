package br.com.adacommerce.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Pedido {
    private Integer id;
    private String numero;
    private Cliente cliente;
    private Date dataPedido;
    private PedidoStatus status;
    private double totalBruto;
    private double desconto;
    private double totalLiquido;
    private final List<PedidoItem> itens = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Date getDataPedido() { return dataPedido; }
    public void setDataPedido(Date dataPedido) { this.dataPedido = dataPedido; }
    public PedidoStatus getStatus() { return status; }
    public void setStatus(PedidoStatus status) { this.status = status; }
    public double getTotalBruto() { return totalBruto; }
    public void setTotalBruto(double totalBruto) { this.totalBruto = totalBruto; }
    public double getDesconto() { return desconto; }
    public void setDesconto(double desconto) { this.desconto = desconto; recalcTotal(); }
    public double getTotalLiquido() { return totalLiquido; }
    public List<PedidoItem> getItens() { return itens; }

    public void adicionarItem(PedidoItem item) {
        itens.add(item);
        recalcularTotais();
    }

    public void removerItem(PedidoItem item) {
        itens.remove(item);
        recalcularTotais();
    }

    public void recalcularTotais() {
        totalBruto = itens.stream().mapToDouble(PedidoItem::getSubtotal).sum();
        recalcTotal();
    }

    private void recalcTotal() {
        totalLiquido = Math.max(0, totalBruto - desconto);
    }
}