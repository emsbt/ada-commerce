package br.com.adacommerce.session;

public final class AuthSession {
    private static String usuarioLogado;
    private AuthSession() {}

    public static void setUsuario(String u) { usuarioLogado = u; }
    public static String getUsuarioLogado() { return usuarioLogado; }
    public static void clear() { usuarioLogado = null; }
    public static boolean isLogado() { return usuarioLogado != null; }
}