package br.com.adacommerce.repository;

import br.com.adacommerce.domain.cliente.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository {
    Cliente salvar(Cliente c);
    Optional<Cliente> buscarPorId(int id);
    List<Cliente> listar();
    List<Cliente> buscarPorNome(String parte);
    void inativar(int id);
}
