package br.com.adacommerce.service;

import br.com.adacommerce.domain.cliente.Cliente;
import br.com.adacommerce.notification.Notificador;
import br.com.adacommerce.repository.ClienteRepository;

import java.util.List;
import java.util.Optional;

public class ClienteService {
    private final ClienteRepository repo;
    private final Notificador notificador;

    public ClienteService(ClienteRepository repo, Notificador notificador) {
        this.repo = repo;
        this.notificador = notificador;
    }

    public Cliente criar(String nome, String documento, String email) {
        Cliente c = new Cliente(nome, documento, email);
        repo.salvar(c);
        notificador.info("Cliente criado: " + c.getId());
        return c;
    }

    public List<Cliente> listar() {
        return repo.listar();
    }

    public Optional<Cliente> buscar(int id) {
        return repo.buscarPorId(id);
    }

    public void inativar(int id) {
        repo.inativar(id);
        notificador.info("Cliente inativado: " + id);
    }
}
