package br.com.adacommerce.app;

import br.com.adacommerce.domain.cliente.Cliente;
import br.com.adacommerce.domain.pedido.ItemPedido;
import br.com.adacommerce.domain.produto.Produto;
import br.com.adacommerce.notification.ConsoleNotificador;
import br.com.adacommerce.notification.Notificador;
import br.com.adacommerce.repository.PedidoRepository;
import br.com.adacommerce.repository.ProdutoRepository;
import br.com.adacommerce.repository.ClienteRepository;
import br.com.adacommerce.repository.sqlite.ClienteRepositorySqlite;
import br.com.adacommerce.repository.sqlite.PedidoRepositorySqlite;
import br.com.adacommerce.repository.sqlite.ProdutoRepositorySqlite;
import br.com.adacommerce.service.ClienteService;
import br.com.adacommerce.service.PedidoService;
import br.com.adacommerce.service.ProdutoService;

import java.util.List;

public class ExemploExecucao {

    public static void main(String[] args) {
        Notificador notificador = new ConsoleNotificador();

        ClienteRepository clienteRepo = new ClienteRepositorySqlite();
        ProdutoRepository produtoRepo = new ProdutoRepositorySqlite();
        PedidoRepository pedidoRepo = new PedidoRepositorySqlite();

        ClienteService clienteService = new ClienteService(clienteRepo, notificador);
        ProdutoService produtoService = new ProdutoService(produtoRepo, notificador);
        PedidoService pedidoService = new PedidoService(pedidoRepo, notificador);

        Cliente cli = clienteService.criar("João Silva", "12345678900", "joao@mail");
        Produto prodA = produtoService.criar("Teclado", 150.00);
        Produto prodB = produtoService.criar("Mouse", 70.00);

        ItemPedido item1 = new ItemPedido(prodA, 1, prodA.getPrecoBase());
        ItemPedido item2 = new ItemPedido(prodB, 2, prodB.getPrecoBase());

        var pedido = pedidoService.novo(cli, List.of(item1, item2));
        System.out.println("Total do pedido: " + pedido.getTotal());

        pedidoService.aprovarPagamento(pedido.getId());
    }
}