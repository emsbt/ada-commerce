package br.com.adacommerce.controller;

import br.com.adacommerce.ui.ViewLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label lblStatus;
    @FXML private Label lblUser;

    @FXML private MenuItem menuCategorias;
    @FXML private MenuItem menuProdutos;
    @FXML private MenuItem menuClientes;
    @FXML private MenuItem menuUsuarios;
    @FXML private MenuItem menuPedidos;
    @FXML private MenuItem menuLogout;
    @FXML private MenuItem menuSair;

    @FXML private Button btnNavCategorias;
    @FXML private Button btnNavProdutos;
    @FXML private Button btnNavClientes;

    private BaseController current;

    @FXML
    private void initialize() {
        menuCategorias.setOnAction(e -> openView("/fxml/categorias.fxml"));
        btnNavCategorias.setOnAction(e -> openView("/fxml/categorias.fxml"));

        menuProdutos.setOnAction(e -> setStatus("Tela de produtos não implementada."));
        btnNavProdutos.setOnAction(e -> setStatus("Tela de produtos não implementada."));
        menuClientes.setOnAction(e -> setStatus("Tela de clientes não implementada."));
        btnNavClientes.setOnAction(e -> setStatus("Tela de clientes não implementada."));
        menuUsuarios.setOnAction(e -> setStatus("Tela de usuários não implementada."));
        menuPedidos.setOnAction(e -> setStatus("Tela de pedidos não implementada."));

        menuLogout.setOnAction(e -> setStatus("Logout (implementar retorno ao login)."));
        menuSair.setOnAction(e -> System.exit(0));

        openView("/fxml/categorias.fxml");
        lblUser.setText("Usuário: admin");
    }

    private void openView(String path) {
        try {
            ViewLoader.ViewTuple tuple = ViewLoader.load(path);
            Node view = tuple.getView();
            BaseController controller = tuple.getController();

            if (current != null) current.onHide();
            contentArea.getChildren().setAll(view);
            current = controller;
            current.onShow();
            setStatus("Carregado: " + path.substring(path.lastIndexOf('/') + 1));
        } catch (Exception ex) {
            ex.printStackTrace();
            setStatus("Erro ao carregar: " + path + " - " + ex.getMessage());
        }
    }

    private void setStatus(String msg) {
        lblStatus.setText(msg);
    }
}