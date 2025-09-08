package br.com.adacommerce.controller;

import br.com.adacommerce.session.AuthSession;
import br.com.adacommerce.util.ViewLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class ShellController {

    @FXML private Label lblUsuario;
    @FXML private Label lblStatus;
    @FXML private Label lblTitulo;
    @FXML private StackPane contentArea;

    @FXML
    private void initialize() {
        lblUsuario.setText("Usuário: " + AuthSession.getUsuarioLogado());
        abrirRelatorios();
    }

    @FXML
    private void abrirRelatorios() {
        carregar("/fxml/relatorios.fxml", "Relatórios", "Tela de Relatórios");
    }

    @FXML
    private void abrirCategorias() {
        carregar("/fxml/categorias.fxml", "Categorias", "Tela de Categorias");
    }

    @FXML
    private void onLogout() {
        AuthSession.clear();
        try {
            Stage s = (Stage) contentArea.getScene().getWindow();
            s.close();
            ViewLoader.openOnNewStage("/fxml/login.fxml", "Login - Ada Commerce");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSair() {
        System.exit(0);
    }

    private void carregar(String fxml, String titulo, String status) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            contentArea.getChildren().setAll(root);
            lblTitulo.setText(titulo);
            lblStatus.setText(status);
        } catch (Exception e) {
            lblStatus.setText("Erro carregando: " + fxml);
            e.printStackTrace();
        }
    }
}