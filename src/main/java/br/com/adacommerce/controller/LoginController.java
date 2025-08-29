package br.com.adacommerce.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtSenha;
    @FXML private Button btnLogin;
    @FXML private Label lblErro;

    @FXML
    private void initialize() {
        btnLogin.setOnAction(e -> fazerLogin());
        txtUsuario.setOnAction(e -> fazerLogin());
        txtSenha.setOnAction(e -> fazerLogin());
    }

    private void fazerLogin() {
        String usuario = txtUsuario.getText();
        String senha = txtSenha.getText();

        if (usuario.isEmpty() || senha.isEmpty()) {
            lblErro.setText("Preencha todos os campos");
            lblErro.setVisible(true);
            return;
        }

        // Por enquanto, aceita qualquer usuário/senha para testar
        if (usuario.equals("admin") && senha.equals("admin123")) {
            System.out.println("Login realizado com sucesso!");
            lblErro.setVisible(false);
        } else {
            lblErro.setText("Usuário ou senha inválidos");
            lblErro.setVisible(true);
        }
    }
}