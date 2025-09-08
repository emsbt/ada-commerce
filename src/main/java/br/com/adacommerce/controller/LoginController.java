package br.com.adacommerce.controller;

import br.com.adacommerce.service.AuthService;
import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.util.Navigation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtSenha;
    @FXML private Label lblMsg;

    private final AuthService auth = new AuthService();

    @FXML
    public void initialize() {
        // Garante que banco / seed já aconteceram
        DatabaseConfig.getConnection();
    }

    @FXML
    public void onEntrar() {
        String u = txtUsuario.getText();
        String s = txtSenha.getText();
        lblMsg.setText("");
        if (u == null || u.isBlank() || s == null || s.isBlank()) {
            lblMsg.setText("Informe usuário e senha.");
            return;
        }
        try {
            if (auth.autenticar(u, s)) {
                Navigation.setUsuarioLogado(u);
                Navigation.showShell();
                Navigation.close(lblMsg);
            } else {
                lblMsg.setText("Credenciais inválidas.");
            }
        } catch (Exception e) {
            lblMsg.setText("Erro: " + e.getMessage());
        }
    }
}