package br.com.adacommerce.controller;

import br.com.adacommerce.session.AuthSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label lblUsuario;
    @FXML private Label lblStatus;
    @FXML private Label lblTitulo;

    @FXML
    public void initialize() {
        if (lblUsuario != null) {
            lblUsuario.setText("Usuário: " + (AuthSession.getUsuarioLogado() == null ? "admin" : AuthSession.getUsuarioLogado()));
        }
        abrirRelatorios(); // carrega relatório padrão
    }

    @FXML public void abrirRelatorios() { carregar("/fxml/relatorios.fxml", "Relatórios", "Tela de Relatórios"); }
    @FXML public void abrirCategorias() { carregar("/fxml/categorias.fxml", "Categorias", "Gerenciamento de Categorias"); }
    @FXML public void abrirClientes()   { carregar("/fxml/clientes.fxml", "Clientes", "Cadastro de Clientes"); }
    @FXML public void abrirProdutos()   { carregar("/fxml/produtos.fxml", "Produtos", "Cadastro de Produtos"); }
    @FXML public void abrirPedidos()    { carregar("/fxml/pedidos.fxml", "Pedidos", "Gestão de Pedidos"); }
    @FXML public void abrirConfig()     { carregar("/fxml/configuracoes.fxml", "Configurações", "Preferências do Sistema"); }

    @FXML public void onLogout() {
        AuthSession.clear();
        // Você pode voltar para login se tiver tela de login
        // ...
        System.exit(0);
    }

    @FXML public void onSair() { System.exit(0); }

    private void carregar(String fxml, String titulo, String status) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            contentArea.getChildren().setAll(root);
            if (lblTitulo != null) lblTitulo.setText(titulo);
            if (lblStatus != null) lblStatus.setText(status);
        } catch (Exception e) {
            if (lblStatus != null) lblStatus.setText("Erro carregando: " + fxml + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}