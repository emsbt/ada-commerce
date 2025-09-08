package br.com.adacommerce.controller;

import br.com.adacommerce.model.Categoria;
import br.com.adacommerce.service.CategoriaService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.Date;

public class CategoriaController {

    @FXML private TableView<Categoria> tableCategorias;
    @FXML private TableColumn<Categoria, String> colNome;
    @FXML private TableColumn<Categoria, String> colDescricao;
    @FXML private TableColumn<Categoria, String> colCategoriaPai;
    @FXML private TableColumn<Categoria, Boolean> colAtivo;
    // (Coluna Ações se existir: @FXML private TableColumn<Categoria, Void> colAcoes;)

    @FXML private TextField txtFiltro;
    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private ComboBox<Categoria> comboCategoriaPai;
    @FXML private CheckBox chkAtivo;
    @FXML private Label lblModo;
    @FXML private Button btnNova;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    private final CategoriaService categoriaService = new CategoriaService();
    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();

    private Categoria categoriaEmEdicao = null;
    private enum Modo { VISUALIZACAO, NOVO, EDICAO }
    private Modo modoAtual = Modo.VISUALIZACAO;

    @FXML
    public void initialize() {
        configurarColunas();
        tableCategorias.setItems(categorias);
        carregarCategorias();
        configurarSelecaoTabela();
        aplicarModo(Modo.VISUALIZACAO);

        txtFiltro.textProperty().addListener((obs, o, n) -> aplicarFiltro(n));
    }

    private void configurarColunas() {
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colDescricao.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDescricao() == null ? "" : c.getValue().getDescricao()
        ));
        colCategoriaPai.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCategoriaPai() != null ? c.getValue().getCategoriaPai().getNome() : ""
        ));
        colAtivo.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isAtivo()));
        colAtivo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean ativo, boolean empty) {
                super.updateItem(ativo, empty);
                if (empty || ativo == null) {
                    setText(null);
                } else {
                    setText(ativo ? "Sim" : "Não");
                }
            }
        });
    }

    private void carregarCategorias() {
        categorias.clear();
        categorias.addAll(categoriaService.listarTodas());
        comboCategoriaPai.getItems().setAll(categorias);
    }

    private void configurarSelecaoTabela() {
        tableCategorias.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> {
                    if (sel != null && modoAtual == Modo.VISUALIZACAO) {
                        preencherFormulario(sel);
                    }
                });
    }

    private void preencherFormulario(Categoria c) {
        txtNome.setText(c.getNome());
        txtDescricao.setText(c.getDescricao());
        chkAtivo.setSelected(c.isAtivo());
        if (c.getCategoriaPai() != null) {
            comboCategoriaPai.getSelectionModel().select(c.getCategoriaPai());
        } else {
            comboCategoriaPai.getSelectionModel().clearSelection();
        }
    }

    private void limparFormulario() {
        txtNome.clear();
        txtDescricao.clear();
        chkAtivo.setSelected(true);
        comboCategoriaPai.getSelectionModel().clearSelection();
    }

    private void aplicarModo(Modo modo) {
        modoAtual = modo;
        switch (modo) {
            case VISUALIZACAO -> {
                lblModo.setText("Modo: visualização");
                btnNova.setDisable(false);
                btnSalvar.setDisable(true);
                btnCancelar.setDisable(true);
                setCamposEditable(false);
            }
            case NOVO -> {
                lblModo.setText("Modo: novo");
                btnNova.setDisable(true);
                btnSalvar.setDisable(false);
                btnCancelar.setDisable(false);
                setCamposEditable(true);
            }
            case EDICAO -> {
                lblModo.setText("Modo: edição");
                btnNova.setDisable(true);
                btnSalvar.setDisable(false);
                btnCancelar.setDisable(false);
                setCamposEditable(true);
            }
        }
    }

    private void setCamposEditable(boolean editable) {
        txtNome.setDisable(!editable);
        txtDescricao.setDisable(!editable);
        comboCategoriaPai.setDisable(!editable);
        chkAtivo.setDisable(!editable);
    }

    private void aplicarFiltro(String termo) {
        if (termo == null || termo.isBlank()) {
            tableCategorias.setItems(categorias);
            return;
        }
        String t = termo.toLowerCase();
        tableCategorias.setItems(categorias.filtered(c ->
                (c.getNome() != null && c.getNome().toLowerCase().contains(t)) ||
                        (c.getDescricao() != null && c.getDescricao().toLowerCase().contains(t))
        ));
    }

    @FXML
    private void onNova() {
        categoriaEmEdicao = null;
        limparFormulario();
        aplicarModo(Modo.NOVO);
    }

    @FXML
    private void onCancelar() {
        categoriaEmEdicao = null;
        limparFormulario();
        aplicarModo(Modo.VISUALIZACAO);
        tableCategorias.getSelectionModel().clearSelection();
    }

    @FXML
    private void onSalvar() {
        try {
            if (!validarFormulario()) return;

            if (categoriaEmEdicao == null) {
                Categoria nova = new Categoria();
                nova.setNome(txtNome.getText().trim());
                nova.setDescricao(txtDescricao.getText().trim());
                nova.setCategoriaPai(comboCategoriaPai.getValue());
                nova.setAtivo(chkAtivo.isSelected());
                Date agora = new Date();
                nova.setDataCriacao(agora);
                nova.setDataAtualizacao(agora);
                categoriaService.salvar(nova);
            } else {
                categoriaEmEdicao.setNome(txtNome.getText().trim());
                categoriaEmEdicao.setDescricao(txtDescricao.getText().trim());
                categoriaEmEdicao.setCategoriaPai(comboCategoriaPai.getValue());
                categoriaEmEdicao.setAtivo(chkAtivo.isSelected());
                categoriaEmEdicao.setDataAtualizacao(new Date());
                categoriaService.atualizar(categoriaEmEdicao);
            }

            carregarCategorias();
            aplicarModo(Modo.VISUALIZACAO);
            limparFormulario();
        } catch (SQLException e) {
            mostrarErro("Erro ao salvar categoria", e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validarFormulario() {
        if (txtNome.getText() == null || txtNome.getText().isBlank()) {
            mostrarErro("Validação", "Nome é obrigatório.");
            return false;
        }
        return true;
    }

    private void mostrarErro(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro");
        a.setHeaderText(titulo);
        a.setContentText(msg);
        a.showAndWait();
    }

    @FXML
    private void onEditarExistente() {
        Categoria sel = tableCategorias.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        categoriaEmEdicao = sel;
        preencherFormulario(sel);
        aplicarModo(Modo.EDICAO);
    }

    @FXML
    private void onRemover() {
        Categoria sel = tableCategorias.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "Remover a categoria selecionada?", ButtonType.YES, ButtonType.NO);
        conf.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    categoriaService.excluir(sel.getId());
                    carregarCategorias();
                    limparFormulario();
                    aplicarModo(Modo.VISUALIZACAO);
                } catch (SQLException e) {
                    mostrarErro("Erro ao excluir", e.getMessage());
                }
            }
        });
    }
}