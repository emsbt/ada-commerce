package br.com.adacommerce.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class CadastrosController {

    @FXML private Button btnProdutos;
    @FXML private Button btnClientes;
    @FXML private Button btnFornecedores;
    @FXML private Button btnVoltar;

    @FXML
    private void initialize() {
        btnProdutos.setOnAction(e -> abrirTelaProdutos());
        btnClientes.setOnAction(e -> abrirTelaClientes());
        btnFornecedores.setOnAction(e -> abrirTelaFornecedores());
        btnVoltar.setOnAction(e -> voltar());
    }

    private void abrirTelaProdutos() {
        // Implementar navegação para tela de produtos
    }

    private void abrirTelaClientes() {
        // Implementar navegação para tela de clientes
    }

    private void abrirTelaFornecedores() {
        // Implementar navegação para tela de fornecedores
    }

    private void voltar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/principal.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnVoltar.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Ada Commerce - Sistema Principal");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}