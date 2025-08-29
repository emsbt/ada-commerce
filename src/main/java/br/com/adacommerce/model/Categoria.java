package br.com.adacommerce.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Categoria {
    private int id;
    private String nome;
    private String descricao;
    private Categoria categoriaPai;
    private List<Categoria> subcategorias;
    private boolean ativo;
    private Date dataCriacao;
    private Date dataAtualizacao;

    // Construtores
    public Categoria() {
        this.subcategorias = new ArrayList<>();
        this.ativo = true;
        this.dataCriacao = new Date();
        this.dataAtualizacao = new Date();
    }
    
    public Categoria(String nome) {
        this();
        this.nome = nome;
    }
    
    public Categoria(String nome, String descricao) {
        this(nome);
        this.descricao = descricao;
    }
    
    public Categoria(String nome, Categoria categoriaPai) {
        this(nome);
        this.categoriaPai = categoriaPai;
        if (categoriaPai != null) {
            categoriaPai.adicionarSubcategoria(this);
        }
    }
    
    // Métodos de negócio
    public void adicionarSubcategoria(Categoria subcategoria) {
        if (!this.subcategorias.contains(subcategoria)) {
            this.subcategorias.add(subcategoria);
            subcategoria.setCategoriaPai(this);
        }
    }
    
    public void removerSubcategoria(Categoria subcategoria) {
        if (this.subcategorias.contains(subcategoria)) {
            this.subcategorias.remove(subcategoria);
            subcategoria.setCategoriaPai(null);
        }
    }
    
    public boolean temSubcategorias() {
        return !this.subcategorias.isEmpty();
    }
    
    public boolean eSubcategoriaDe(Categoria categoria) {
        return this.categoriaPai != null && 
               (this.categoriaPai.equals(categoria) || this.categoriaPai.eSubcategoriaDe(categoria));
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
        this.dataAtualizacao = new Date();
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
        this.dataAtualizacao = new Date();
    }

    public Categoria getCategoriaPai() {
        return categoriaPai;
    }

    public void setCategoriaPai(Categoria categoriaPai) {
        // Evitar ciclos
        if (categoriaPai != null && categoriaPai.eSubcategoriaDe(this)) {
            throw new IllegalArgumentException("Não é possível criar ciclos na hierarquia de categorias");
        }
        this.categoriaPai = categoriaPai;
        this.dataAtualizacao = new Date();
    }

    public List<Categoria> getSubcategorias() {
        return subcategorias;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
        this.dataAtualizacao = new Date();
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Date getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(Date dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Categoria categoria = (Categoria) obj;
        
        if (id != 0 && id == categoria.id) return true;
        return nome != null && nome.equals(categoria.nome);
    }
    
    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (nome != null ? nome.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return nome + (categoriaPai != null ? " (em " + categoriaPai.getNome() + ")" : "");
    }
}