package br.com.adacommerce.app;

import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.model.*;
import br.com.adacommerce.service.ClienteService;
import br.com.adacommerce.service.PedidoService;
import br.com.adacommerce.service.ProdutoService;

import java.util.Date;

public class ExemploExecucao {
    public static void main(String[] args) throws Exception {
        DatabaseConfig.initialize();

        ClienteService clienteService = new ClienteService();
        ProdutoService produtoService = new ProdutoService();
        PedidoService pedidoService = new PedidoService();

        Cliente c = new Cliente();
        c.setNome("Cliente Console");
        c.setEmail("console@teste.com");
        c.setDocumento("00000000000");
        c.setTelefone("11999998888");
        c.setAtivo(true);
        clienteService.salvar(c);

        Produto p1 = new Produto();
        p1.setNome("Produto A");
        p1.setPreco(10.0);
        p1.setEstoqueAtual(50);
        p1.setAtivo(true);
        produtoService.salvar(p1);

        Produto p2 = new Produto();
        p2.setNome("Produto B");
        p2.setPreco(25.0);
        p2.setEstoqueAtual(30);
        p2.setAtivo(true);
        produtoService.salvar(p2);

        Pedido pedido = new Pedido();
        pedido.setNumero("P" + System.currentTimeMillis());
        pedido.setCliente(c);
        pedido.setDataPedido(new Date());
        pedido.setStatus(PedidoStatus.RASCUNHO);

        PedidoItem i1 = new PedidoItem();
        i1.setProduto(p1);
        i1.setQuantidade(2);
        i1.setPrecoUnitario(p1.getPreco());
        pedido.adicionarItem(i1);

        PedidoItem i2 = new PedidoItem();
        i2.setProduto(p2);
        i2.setQuantidade(1);
        i2.setPrecoUnitario(p2.getPreco());
        pedido.adicionarItem(i2);

        pedido.recalcularTotais();
        pedidoService.salvarRascunho(pedido);
        System.out.println("Rascunho salvo ID=" + pedido.getId() + " Total=" + pedido.getTotalLiquido());

        pedidoService.confirmarPedido(pedido);
        System.out.println("Confirmado. Status=" + pedido.getStatus());

        pedidoService.cancelarPedido(pedido);
        System.out.println("Cancelado. Status=" + pedido.getStatus());
    }
}