package br.com.adacommerce.controller;

import br.com.adacommerce.session.AuthSession;
import br.com.adacommerce.util.ViewLoader;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtSenha;

    @FXML
    private void onEntrar() {
        // Validação simples
        String user = txtUsuario.getText().trim();
        if (user.isEmpty()) user = "desconhecido";
        AuthSession.setUsuarioLogado(user);
        try {
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.close();
            ViewLoader.openOnNewStage("/fxml/main.fxml", "Ada Commerce");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}