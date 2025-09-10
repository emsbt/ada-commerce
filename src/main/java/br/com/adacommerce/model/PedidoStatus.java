package br.com.adacommerce.model;

public enum PedidoStatus {
    RASCUNHO,
    CONFIRMADO,
    CANCELADO,
    ABERTO,
    AGUARDANDO_PAGAMENTO,
    PAGO,
    FINALIZADO;


    public boolean isRascunho()   { return this == RASCUNHO; }
    public boolean isConfirmado() { return this == CONFIRMADO; }
    public boolean isCancelado()  { return this == CANCELADO; }
    public boolean isAberto()    { return this == ABERTO; }
    public boolean isAguardandoPagamento() { return this == AGUARDANDO_PAGAMENTO; }
    public boolean isPago()      { return this == PAGO; }
    public boolean isFinalizado(){ return this == FINALIZADO; }

}