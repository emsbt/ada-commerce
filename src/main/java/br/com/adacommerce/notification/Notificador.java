package br.com.adacommerce.notification;

public interface Notificador {
    void info(String msg);
    void erro(String msg, Throwable t);
}
