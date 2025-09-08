package br.com.adacommerce.service;

import br.com.adacommerce.domain.produto.Produto;
import br.com.adacommerce.notification.Notificador;
import br.com.adacommerce.repository.ProdutoRepository;

import java.util.List;
import java.util.Optional;

public class ProdutoService {
    private final ProdutoRepository repo;
    private final Notificador notificador;

    public ProdutoService(ProdutoRepository repo, Notificador notificador) {
        this.repo = repo;
        this.notificador = notificador;
    }

    public Produto criar(String nome, double preco) {
        Produto p = new Produto(nome, preco);
        repo.salvar(p);
        notificador.info("Produto criado: " + p.getId());
        return p;
    }

    public List<Produto> listar() {
        return repo.listar();
    }

    public Optional<Produto> buscar(int id) {
        return repo.buscarPorId(id);
    }

    public void inativar(int id) {
        repo.inativar(id);
        notificador.info("Produto inativado: " + id);
    }
}
