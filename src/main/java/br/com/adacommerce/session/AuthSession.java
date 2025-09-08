package br.com.adacommerce.session;

public class AuthSession {
    private static String usuarioLogado;

    public static void setUsuarioLogado(String u) {
        usuarioLogado = u;
    }
    public static String getUsuarioLogado() {
        return usuarioLogado;
    }
    public static void clear() {
        usuarioLogado = null;
    }
}