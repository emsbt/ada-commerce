package br.com.adacommerce.domain.pedido;

import br.com.adacommerce.domain.cliente.Cliente;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private Integer id;
    private Cliente cliente;
    private LocalDateTime dataCriacao = LocalDateTime.now();
    private StatusPedido status = StatusPedido.NOVO;
    private StatusPagamento statusPagamento = StatusPagamento.AGUARDANDO;
    private final List<ItemPedido> itens = new ArrayList<>();

    public Pedido(Cliente cliente) {
        if (cliente == null) throw new IllegalArgumentException("Cliente obrigatório");
        this.cliente = cliente;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public StatusPedido getStatus() { return status; }
    public StatusPagamento getStatusPagamento() { return statusPagamento; }
    public List<ItemPedido> getItens() { return List.copyOf(itens); }

    public void adicionarItem(ItemPedido item) {
        itens.add(item);
    }

    public double getTotal() {
        return itens.stream().mapToDouble(ItemPedido::getSubtotal).sum();
    }

    public void avançarPagamentoAprovado() {
        this.statusPagamento = StatusPagamento.APROVADO;
        if (status == StatusPedido.NOVO || status == StatusPedido.PENDENTE_PAGAMENTO) {
            this.status = StatusPedido.PAGO;
        }
    }

    public void cancelar(String motivo) {
        this.status = StatusPedido.CANCELADO;
        // motivo pode ser armazenado futuramente
    }

    public void finalizar() {
        if (status == StatusPedido.PAGO) {
            this.status = StatusPedido.FINALIZADO;
        } else {
            throw new IllegalStateException("Só pode finalizar pedido pago");
        }
    }

    public void marcarPendentePagamento() {
        this.status = StatusPedido.PENDENTE_PAGAMENTO;
    }
}
