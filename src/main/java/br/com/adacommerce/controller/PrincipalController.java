package br.com.adacommerce.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import br.com.adacommerce.model.Categoria;
import br.com.adacommerce.service.CategoriaService;

public class PrincipalController {

    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private ComboBox<Categoria> cbCategoriaPai;
    @FXML private CheckBox chkAtivo;
    @FXML private Button btnSalvar;
    @FXML private Button btnLimpar;
    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, String> colNome;
    @FXML private TableColumn<Categoria, String> colDescricao;
    @FXML private TableColumn<Categoria, String> colPai;
    @FXML private TableColumn<Categoria, Boolean> colAtivo;

    private CategoriaService categoriaService = new CategoriaService();
    private ObservableList<Categoria> categorias = FXCollections.observableArrayList();
    private Categoria categoriaEditando;

    @FXML
    private void initialize() {
        configurarTabela();
        carregarCategorias();
        chkAtivo.setSelected(true);

        btnSalvar.setOnAction(e -> salvarCategoria());
        btnLimpar.setOnAction(e -> limparFormulario());

        tblCategorias.getSelectionModel().selectedItemProperty().addListener((obs, old, nova) -> {
            if (nova != null) {
                preencherFormulario(nova);
            }
        });
    }

    private void configurarTabela() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colPai.setCellValueFactory(cellData -> {
            Categoria pai = cellData.getValue().getCategoriaPai();
            return new javafx.beans.property.SimpleStringProperty(pai != null ? pai.getNome() : "");
        });
        colAtivo.setCellValueFactory(new PropertyValueFactory<>("ativo"));
        tblCategorias.setItems(categorias);
    }

    private void carregarCategorias() {
        categorias.clear();
        categorias.addAll(categoriaService.listarTodas());
        cbCategoriaPai.setItems(categorias);
    }

    private void salvarCategoria() {
        if (txtNome.getText().trim().isEmpty()) {
            mostrarAlerta("Nome da categoria é obrigatório!");
            return;
        }

        Categoria categoria = categoriaEditando != null ? categoriaEditando : new Categoria();
        categoria.setNome(txtNome.getText().trim());
        categoria.setDescricao(txtDescricao.getText().trim());
        categoria.setCategoriaPai(cbCategoriaPai.getValue());
        categoria.setAtivo(chkAtivo.isSelected());

        try {
            if (categoriaEditando != null) {
                categoriaService.atualizar(categoria);
                mostrarAlerta("Categoria atualizada com sucesso!");
            } else {
                categoriaService.salvar(categoria);
                mostrarAlerta("Categoria salva com sucesso!");
            }

            limparFormulario();
            carregarCategorias();

        } catch (Exception e) {
            mostrarAlerta("Erro ao salvar categoria: " + e.getMessage());
        }
    }

    private void preencherFormulario(Categoria categoria) {
        categoriaEditando = categoria;
        txtNome.setText(categoria.getNome());
        txtDescricao.setText(categoria.getDescricao());
        cbCategoriaPai.setValue(categoria.getCategoriaPai());
        chkAtivo.setSelected(categoria.isAtivo());
    }

    private void limparFormulario() {
        categoriaEditando = null;
        txtNome.clear();
        txtDescricao.clear();
        cbCategoriaPai.setValue(null);
        chkAtivo.setSelected(true);
    }

    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ada Commerce");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}