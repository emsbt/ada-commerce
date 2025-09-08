package br.com.adacommerce.service;

import br.com.adacommerce.dao.UserDao;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final UserDao userDao = new UserDao();

    public boolean autenticar(String usuario, String senhaDigitada) {
        if (usuario == null || senhaDigitada == null) return false;
        String stored = userDao.findPasswordHashByUsuario(usuario.trim());
        if (stored == null) return false;

        // Aceita hash ou texto puro (fallback) – remova o fallback depois
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return BCrypt.checkpw(senhaDigitada, stored);
        }
        return senhaDigitada.equals(stored);
    }
}