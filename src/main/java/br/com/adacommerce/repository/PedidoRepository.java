package br.com.adacommerce.repository;

import br.com.adacommerce.domain.pedido.Pedido;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository {
    Pedido salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(int id);
    List<Pedido> listar();
    void atualizarStatus(int id, String statusPedido, String statusPagamento);
}
