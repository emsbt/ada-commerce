package br.com.adacommerce.controller;

import br.com.adacommerce.service.UsuarioService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtSenha;
    @FXML private Label lblErro;

    @FXML
    private void fazerLogin() {
        String usuario = txtUsuario.getText();
        String senha = txtSenha.getText();

        lblErro.setVisible(false);

        if (usuario.isEmpty() || senha.isEmpty()) {
            lblErro.setText("Preencha todos os campos!");
            lblErro.setVisible(true);
            return;
        }

        try {
            UsuarioService usuarioService = new UsuarioService();
            boolean resultado = usuarioService.autenticar(usuario, senha);
            if (resultado) {
                abrirShellPrincipal();
            } else {
                lblErro.setText("Usuário ou senha inválidos!");
                lblErro.setVisible(true);
                txtSenha.clear();
            }
        } catch (Exception e) {
            lblErro.setText("Erro de conexão com o banco de dados!");
            lblErro.setVisible(true);
            System.err.println("Erro no login: " + e.getMessage());
        }
    }

    private void abrirShellPrincipal() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ada Commerce");
            stage.setMaximized(true);
            stage.show();

            Stage loginStage = (Stage) txtUsuario.getScene().getWindow();
            loginStage.close();
        } catch (Exception e) {
            System.err.println("Erro ao abrir shell principal: " + e.getMessage());
        }
    }
}