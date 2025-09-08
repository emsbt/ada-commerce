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
        String usuario = txtUsuario.getText();
        // (Validação simples)
        if (usuario == null || usuario.isBlank()) {
            // TODO: Mostrar alerta se quiser
            return;
        }

        AuthSession.setUsuarioLogado(usuario);

        try {
            Stage atual = (Stage) txtUsuario.getScene().getWindow();
            atual.close();
            ViewLoader.openOnNewStage("/fxml/main.fxml", "Ada Commerce");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}