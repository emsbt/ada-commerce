package br.com.adacommerce.model;

import java.math.BigDecimal;
import java.util.Date;

public class Produto {
    private int id;
    private String nome;
    private String descricao;
    private BigDecimal precoBase;
    private int quantidade;
    private Date dataCadastro;
    private boolean ativo;

    // Construtores
    public Produto() {
        this.dataCadastro = new Date();
        this.ativo = true;
    }
    
    public Produto(String nome, BigDecimal precoBase, int quantidade) {
        this();
        this.nome = nome;
        this.precoBase = precoBase;
        this.quantidade = quantidade;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPrecoBase() {
        return precoBase;
    }

    public void setPrecoBase(BigDecimal precoBase) {
        this.precoBase = precoBase;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
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
        return nome + " - R$ " + precoBase;
    }
}