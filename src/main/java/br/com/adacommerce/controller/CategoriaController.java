package br.com.adacommerce.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CategoriaController {

    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private ComboBox<String> cbCategoriaPai;
    @FXML private CheckBox chkAtivo;
    @FXML private TableView<?> tblCategorias;
    @FXML private TableColumn<?,?> colNome;
    @FXML private TableColumn<?,?> colDescricao;
    @FXML private TableColumn<?,?> colPai;
    @FXML private TableColumn<?,?> colAtivo;
    @FXML private Label lblMensagem;

    @FXML
    public void onSalvar() {
        lblMensagem.setText("Salvo (exemplo)");
        onLimpar();
    }

    @FXML
    public void onLimpar() {
        txtNome.clear();
        txtDescricao.clear();
        cbCategoriaPai.getSelectionModel().clearSelection();
        chkAtivo.setSelected(false);
    }
}