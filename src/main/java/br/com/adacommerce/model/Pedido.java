package br.com.adacommerce.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Pedido {

    private Integer id;
    private String numero;
    private Cliente cliente;
    private Date dataPedido;
    private double desconto;
    private double totalBruto;
    private double totalLiquido;
    private PedidoStatus status = PedidoStatus.RASCUNHO;
    private final List<ItemPedido> itens = new ArrayList<>();

    public Pedido() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Date getDataPedido() { return dataPedido; }
    public void setDataPedido(Date dataPedido) { this.dataPedido = dataPedido; }

    public double getDesconto() { return desconto; }
    public void setDesconto(double desconto) { this.desconto = desconto; }

    public double getTotalBruto() { return totalBruto; }
    // adicionado: setter para totalBruto
    public void setTotalBruto(double totalBruto) { this.totalBruto = totalBruto; }

    public double getTotalLiquido() { return totalLiquido; }
    // adicionado: setter para totalLiquido
    public void setTotalLiquido(double totalLiquido) { this.totalLiquido = totalLiquido; }

    public PedidoStatus  getStatus() { return status; }
    public void setStatus(PedidoStatus  status) { this.status = status; }

    public List<ItemPedido> getItens() { return itens; }

    public void recalcularTotais() {
        totalBruto = itens.stream().mapToDouble(ItemPedido::getSubtotal).sum();
        totalLiquido = Math.max(0, totalBruto - desconto);
    }
}