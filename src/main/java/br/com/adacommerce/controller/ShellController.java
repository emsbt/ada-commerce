package br.com.adacommerce.controller;

import br.com.adacommerce.util.Navigation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class ShellController {

    @FXML private StackPane contentArea;
    @FXML private Label lblUsuario;
    @FXML private Label lblStatus;
    @FXML private Label lblTitulo;

    @FXML
    public void initialize() {
        lblUsuario.setText("Usuário: " + Navigation.getUsuarioLogado());
        abrirRelatorios(); // tela inicial
    }

    @FXML
    public void abrirRelatorios() {
        Navigation.loadInto(contentArea, "/fxml/relatorios.fxml");
        lblTitulo.setText("Relatórios");
        lblStatus.setText("Tela de Relatórios");
    }

    @FXML
    public void abrirCategorias() {
        Navigation.loadInto(contentArea, "/fxml/categorias.fxml");
        lblTitulo.setText("Categorias");
        lblStatus.setText("Tela de Categorias");
    }

    @FXML
    public void onLogout() {
        Navigation.showLogin();
        Navigation.close(contentArea);
    }

    @FXML
    public void onSair() {
        System.exit(0);
    }

    @FXML
    public void onSobre() {
        Navigation.alertInfo("Sobre", "Ada Commerce\nVersão Demo");
    }
}