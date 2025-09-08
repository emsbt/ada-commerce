package br.com.adacommerce.util;

public class SecurityUtil {

    public static String hashPassword(String password) {
        System.out.println("Hash gerado para: " + password);
        return password; // Retorna a senha em texto puro para debug
    }

    public static boolean checkPassword(String inputPassword, String storedHash) {
        System.out.println("Verificando: input='" + inputPassword + "', stored='" + storedHash + "'");
        boolean match = inputPassword.equals(storedHash);
        System.out.println("Resultado: " + (match ? "CORRETO" : "INCORRETO"));
        return match;
    }
}