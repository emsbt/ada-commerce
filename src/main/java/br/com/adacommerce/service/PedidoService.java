package br.com.adacommerce.service;

import br.com.adacommerce.domain.cliente.Cliente;
import br.com.adacommerce.domain.pedido.ItemPedido;
import br.com.adacommerce.domain.pedido.Pedido;
import br.com.adacommerce.domain.pedido.StatusPagamento;
import br.com.adacommerce.domain.pedido.StatusPedido;
import br.com.adacommerce.notification.Notificador;
import br.com.adacommerce.repository.PedidoRepository;

import java.util.List;
import java.util.Optional;

public class PedidoService {
    private final PedidoRepository repo;
    private final Notificador notificador;

    public PedidoService(PedidoRepository repo, Notificador notificador) {
        this.repo = repo;
        this.notificador = notificador;
    }

    public Pedido novo(Cliente cliente, List<ItemPedido> itens) {
        Pedido p = new Pedido(cliente);
        itens.forEach(p::adicionarItem);
        p.marcarPendentePagamento();
        repo.salvar(p);
        notificador.info("Pedido criado: " + p.getId() + " total=" + p.getTotal());
        return p;
    }

    public Optional<Pedido> buscar(int id) {
        return repo.buscarPorId(id);
    }

    public List<Pedido> listar() {
        return repo.listar();
    }

    public void aprovarPagamento(int pedidoId) {
        Pedido p = repo.buscarPorId(pedidoId).orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado"));
        repo.atualizarStatus(pedidoId, StatusPedido.PAGO.name(), StatusPagamento.APROVADO.name());
        notificador.info("Pagamento aprovado para pedido " + pedidoId);
    }

    public void cancelar(int pedidoId) {
        repo.atualizarStatus(pedidoId, StatusPedido.CANCELADO.name(), StatusPagamento.ESTORNADO.name());
        notificador.info("Pedido cancelado " + pedidoId);
    }
}
