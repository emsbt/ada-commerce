package br.com.adacommerce.model;

import java.util.Date;

public class Cliente {
    private int id;
    private String nome;
    private String documento; // CPF ou CNPJ
    private String email;
    private String telefone;
    private String endereco;
    private Date dataCadastro;
    private boolean ativo;

    // Construtores
    public Cliente() {
        this.dataCadastro = new Date();
        this.ativo = true;
    }
    
    public Cliente(String nome, String documento, String email) {
        this();
        this.nome = nome;
        this.documento = documento;
        this.email = email;
    }
    
    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public Date getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Date dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
    
    @Override
    public String toString() {
        return nome + " - " + documento;
    }
}