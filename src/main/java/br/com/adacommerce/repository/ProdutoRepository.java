package br.com.adacommerce.repository;

import br.com.adacommerce.domain.produto.Produto;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository {
    Produto salvar(Produto p);
    Optional<Produto> buscarPorId(int id);
    List<Produto> listar();
    List<Produto> buscarPorNome(String parte);
    void inativar(int id);
}
