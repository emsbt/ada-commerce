package br.com.adacommerce.model;

public enum PedidoStatus {
    RASCUNHO,
    CONFIRMADO,
    CANCELADO;

    public boolean isRascunho()   { return this == RASCUNHO; }
    public boolean isConfirmado() { return this == CONFIRMADO; }
    public boolean isCancelado()  { return this == CANCELADO; }
}