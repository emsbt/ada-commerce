package br.com.adacommerce.controller;

import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.service.AuthService;
import br.com.adacommerce.session.AuthSession;
import br.com.adacommerce.util.ViewLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtSenha;
    @FXML private Label lblErro;

    private final AuthService auth = new AuthService();

    @FXML
    private void initialize() {
        DatabaseConfig.initialize(); // garante tabelas + seed/migração
    }

    @FXML
    private void onEntrar() {
        String u = txtUsuario.getText() == null ? "" : txtUsuario.getText().trim();
        String s = txtSenha.getText() == null ? "" : txtSenha.getText();
        if (u.isEmpty() || s.isEmpty()) {
            lblErro.setText("Informe usuário e senha.");
            return;
        }
        try {
            if (auth.autenticar(u, s)) {
                AuthSession.setUsuarioLogado(u);
                Stage st = (Stage) txtUsuario.getScene().getWindow();
                st.close();
                ViewLoader.openOnNewStage("/fxml/shell.fxml", "Ada Commerce");
            } else {
                lblErro.setText("Credenciais inválidas.");
            }
        } catch (Exception e) {
            lblErro.setText("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}