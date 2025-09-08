package br.com.adacommerce.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class PrincipalController {

    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private ComboBox<String> cbCategoriaPai; // ajuste o tipo conforme seu model
    @FXML private CheckBox chkAtivo;
    @FXML private Button btnSalvar;
    @FXML private Button btnLimpar;
    @FXML private TableView<?> tblCategorias;
    @FXML private TableColumn<?,?> colNome;
    @FXML private TableColumn<?,?> colDescricao;
    @FXML private TableColumn<?,?> colPai;
    @FXML private TableColumn<?,?> colAtivo;
    @FXML private Label lblMensagem;

    @FXML
    public void initialize() {
        // Carregar categorias, preencher combo, etc.
        // (Coloque sua lógica real aqui depois.)
    }

    @FXML
    public void onSalvar() {
        // Lógica para salvar categoria
        lblMensagem.setText("Categoria salva (exemplo).");
        limparFormulario();
    }

    @FXML
    public void onLimpar() {
        limparFormulario();
    }

    private void limparFormulario() {
        txtNome.clear();
        txtDescricao.clear();
        cbCategoriaPai.getSelectionModel().clearSelection();
        chkAtivo.setSelected(false);
    }

    @FXML
    public void abrirRelatorios() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/relatorios.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Relatórios");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erro abrindo Relatórios: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void onSair() {
        System.exit(0);
    }
}