package br.com.adacommerce.domain.produto;

import java.time.LocalDateTime;

public class Produto {
    private Integer id;
    private String nome;
    private double precoBase;
    private boolean ativo = true;
    private LocalDateTime dataCriacao = LocalDateTime.now();

    public Produto(String nome, double precoBase) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do produto obrigatório");
        }
        if (precoBase < 0) {
            throw new IllegalArgumentException("Preço base não pode ser negativo");
        }
        this.nome = nome;
        this.precoBase = precoBase;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNome() { return nome; }
    public double getPrecoBase() { return precoBase; }
    public boolean isAtivo() { return ativo; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }

    public void inativar() { this.ativo = false; }
    public void ativar() { this.ativo = true; }

    public void atualizar(String nome, Double precoBase, Boolean ativo) {
        if (nome != null && !nome.isBlank()) this.nome = nome;
        if (precoBase != null && precoBase >= 0) this.precoBase = precoBase;
        if (ativo != null) this.ativo = ativo;
    }
}