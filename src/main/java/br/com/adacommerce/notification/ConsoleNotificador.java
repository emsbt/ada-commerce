package br.com.adacommerce.notification;

public class ConsoleNotificador implements Notificador {
    @Override
    public void info(String msg) {
        System.out.println("[INFO] " + msg);
    }

    @Override
    public void erro(String msg, Throwable t) {
        System.err.println("[ERRO] " + msg);
        if (t != null) t.printStackTrace();
    }
}
