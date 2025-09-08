package br.com.adacommerce.domain.cliente;

import java.time.LocalDateTime;

public class Cliente {
    private Integer id;
    private String nome;
    private String documento;
    private String email;
    private String telefone;
    private String endereco;
    private boolean ativo = true;
    private LocalDateTime dataCadastro = LocalDateTime.now();

    public Cliente(String nome, String documento, String email) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("Documento obrigatório");
        }
        this.nome = nome;
        this.documento = documento;
        this.email = email;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNome() { return nome; }
    public String getDocumento() { return documento; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }
    public String getEndereco() { return endereco; }
    public boolean isAtivo() { return ativo; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }

    public void atualizar(String nome, String email, String telefone, String endereco) {
        if (nome != null && !nome.isBlank()) this.nome = nome;
        if (email != null && !email.isBlank()) this.email = email;
        if (telefone != null) this.telefone = telefone;
        if (endereco != null) this.endereco = endereco;
    }
}