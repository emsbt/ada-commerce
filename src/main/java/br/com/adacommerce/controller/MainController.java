package br.com.adacommerce.controller;

import br.com.adacommerce.session.AuthSession;
import br.com.adacommerce.util.ViewLoader;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainController {

    @FXML private BorderPane root;
    @FXML private StackPane contentRoot;
    @FXML private Label lblTitulo;
    @FXML private Label lblUsuarioLogado;
    @FXML private Label lblStatus;
    @FXML private MenuBar menuBar;

    @FXML
    public void initialize() {
        if (lblUsuarioLogado != null) {
            String user = AuthSession.getUsuarioLogado();
            lblUsuarioLogado.setText(user != null ? "Usuário: " + user : "(não logado)");
        }
        load("/fxml/relatorios.fxml", "Relatórios");
    }

    private void setCenter(Node n) {
        if (contentRoot != null) {
            contentRoot.getChildren().setAll(n);
        } else if (root != null) {
            root.setCenter(n);
        }
    }

    private void load(String path, String titulo) {
        try {
            Node n = ViewLoader.loadNode(path);
            setCenter(n);
            if (lblTitulo != null) lblTitulo.setText(titulo);
            setStatus("Carregado: " + titulo);
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("Erro ao carregar: " + path);
        }
    }

    private void setStatus(String msg) {
        if (lblStatus != null) lblStatus.setText(msg);
    }

    private Stage currentStage() {
        if (root != null && root.getScene() != null) return (Stage) root.getScene().getWindow();
        if (contentRoot != null && contentRoot.getScene() != null) return (Stage) contentRoot.getScene().getWindow();
        if (menuBar != null && menuBar.getScene() != null) return (Stage) menuBar.getScene().getWindow();
        throw new IllegalStateException("Stage não disponível.");
    }

    // Menus de navegação
    @FXML private void menuRelatorios() { load("/fxml/relatorios.fxml", "Relatórios"); }
    @FXML private void menuCategorias() { load("/fxml/categorias.fxml", "Categorias"); }
    @FXML private void menuClientes()   { load("/fxml/clientes.fxml",   "Clientes"); }
    @FXML private void menuProdutos()   { load("/fxml/produtos.fxml",   "Produtos"); }
    @FXML private void menuPedidos()    { load("/fxml/pedidos.fxml",    "Pedidos"); }
    @FXML private void menuUsuarios() { load("/fxml/usuarios.fxml", "Usuários"); }

    @FXML
    private void menuLogout() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja realmente encerrar a sessão?",
                ButtonType.YES, ButtonType.NO);
        a.setHeaderText("Logout");
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                AuthSession.clear();
                Stage stage = currentStage();
                stage.close();
                try {
                    ViewLoader.openOnNewStage("/fxml/login.fxml", "Login - Ada Commerce");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void menuSair() {
        Platform.exit();
    }

}