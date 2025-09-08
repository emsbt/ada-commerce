package br.com.adacommerce.model;

import java.util.Date;

public class Categoria {
    private Integer id;
    private String nome;
    private String descricao;
    private Categoria categoriaPai;
    private boolean ativo = true;
    private Date dataCriacao = new Date();
    private Date dataAtualizacao = new Date();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Categoria getCategoriaPai() { return categoriaPai; }
    public void setCategoriaPai(Categoria categoriaPai) { this.categoriaPai = categoriaPai; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public Date getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(Date dataCriacao) { this.dataCriacao = dataCriacao; }
    public Date getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(Date dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
    @Override public String toString() { return nome; }
}