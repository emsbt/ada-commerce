package br.com.adacommerce.controller;

import br.com.adacommerce.session.AuthSession;
import br.com.adacommerce.util.ViewLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label lblStatus;
    @FXML private Label lblUsuario;

    @FXML
    public void initialize() {
        if (AuthSession.getUsuarioLogado() != null) {
            lblUsuario.setText("Usuário: " + AuthSession.getUsuarioLogado());
        }
        lblStatus.setText("Aplicação iniciada.");
    }

    // Menus / Botões
    @FXML private void menuCategorias() { load("/fxml/categorias.fxml", "Categorias"); }
    @FXML private void menuProdutos()  { load("/fxml/produtos.fxml", "Produtos"); }
    @FXML private void menuClientes()  { load("/fxml/clientes.fxml", "Clientes"); }
    @FXML private void menuPedidos()   { load("/fxml/pedidos.fxml", "Pedidos"); }
    @FXML private void menuUsuarios()  { load("/fxml/usuarios.fxml", "Usuários"); }

    @FXML
    private void menuLogout() {
        try {
            AuthSession.clear();
            // fecha janela atual
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.close();
            // reabre login
            ViewLoader.openOnNewStage("/fxml/login.fxml", "Login - Ada Commerce");
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("Falha no logout: " + e.getMessage());
        }
    }

    @FXML
    private void menuSair() {
        Stage stage = (Stage) contentArea.getScene().getWindow();
        stage.close();
    }

    private void load(String fxml, String titulo) {
        try {
            Node node = ViewLoader.loadNode(fxml);
            contentArea.getChildren().setAll(node);
            setStatus("Carregado: " + fxml);
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("Erro ao carregar: " + fxml);
            contentArea.getChildren().setAll(new Label("Erro ao carregar módulo " + titulo));
        }
    }

    private void setStatus(String msg) {
        lblStatus.setText(msg);
    }
}