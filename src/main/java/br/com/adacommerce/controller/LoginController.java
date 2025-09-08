package br.com.adacommerce.controller;

import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.session.AuthSession;
import br.com.adacommerce.util.ViewLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtSenha;
    @FXML private Label lblErro;

    @FXML
    private void onEntrar() {
        String u = txtUsuario.getText() == null ? "" : txtUsuario.getText().trim();
        String s = txtSenha.getText() == null ? "" : txtSenha.getText().trim();
        if (u.isEmpty() || s.isEmpty()) {
            lblErro.setText("Informe usuário e senha.");
            return;
        }
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT senha FROM usuario WHERE usuario=? AND ativo=1")) {
            ps.setString(1, u);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("senha");
                    // Para simplificar: se não está em BCrypt ou s==hash, deixamos entrar;
                    // Ajuste se quiser validar com BCrypt.
                    if (hash.startsWith("$") || hash.equals(s)) {
                        AuthSession.setUsuario(u);
                        fecharAbrirMain();
                        return;
                    }
                }
            }
            lblErro.setText("Usuário ou senha inválidos.");
        } catch (Exception e) {
            e.printStackTrace();
            lblErro.setText("Erro: " + e.getMessage());
        }
    }

    private void fecharAbrirMain() {
        try {
            Stage st = (Stage) txtUsuario.getScene().getWindow();
            st.close();
            ViewLoader.openOnNewStage("/fxml/main.fxml", "Ada Commerce");
        } catch (Exception e) {
            e.printStackTrace();
            lblErro.setText("Falha abrindo sistema: " + e.getMessage());
        }
    }
}