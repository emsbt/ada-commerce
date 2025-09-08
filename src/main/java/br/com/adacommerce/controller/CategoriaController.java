package br.com.adacommerce.controller;

import br.com.adacommerce.model.Categoria;
import br.com.adacommerce.service.CategoriaService;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class CategoriaController extends BaseController {

    @FXML private TextField txtFiltro;
    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private ComboBox<Categoria> cbCategoriaPai;
    @FXML private CheckBox chkAtivo;
    @FXML private Button btnNova;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;
    @FXML private Label  lblInfo;
    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, String> colNome;
    @FXML private TableColumn<Categoria, String> colDescricao;
    @FXML private TableColumn<Categoria, String> colPai;
    @FXML private TableColumn<Categoria, Boolean> colAtivo;
    @FXML private TableColumn<Categoria, Void> colAcoes;

    private final CategoriaService service = new CategoriaService();
    private final ObservableList<Categoria> masterData = FXCollections.observableArrayList();
    private FilteredList<Categoria> filtered;
    private Categoria editando;

    @Override
    public void onShow() {
        if (masterData.isEmpty()) {
            carregar();
        }
    }

    @FXML
    private void initialize() {
        configurarTabela();
        configurarEventos();
        modoVisualizacao();
    }

    private void configurarTabela() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colPai.setCellValueFactory(cd ->
            Bindings.createStringBinding(() -> {
                Categoria pai = cd.getValue().getCategoriaPai();
                return pai != null ? pai.getNome() : "";
            })
        );
        colAtivo.setCellValueFactory(new PropertyValueFactory<>("ativo"));
        adicionarColunaAcoes();

        filtered = new FilteredList<>(masterData, c -> true);
        txtFiltro.textProperty().addListener((obs, o, n) -> {
            String termo = n == null ? "" : n.toLowerCase();
            filtered.setPredicate(cat ->
                termo.isBlank()
                    || cat.getNome().toLowerCase().contains(termo)
                    || (cat.getDescricao() != null && cat.getDescricao().toLowerCase().contains(termo))
            );
        });

        tblCategorias.setItems(filtered);

        tblCategorias.setRowFactory(tv -> {
            TableRow<Categoria> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    iniciarEdicao(row.getItem());
                }
            });
            return row;
        });
    }

    private void adicionarColunaAcoes() {
        Callback<TableColumn<Categoria, Void>, TableCell<Categoria, Void>> factory = col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("X");

            {
                btnEditar.getStyleClass().add("btn-mini");
                btnExcluir.getStyleClass().addAll("btn-mini", "btn-danger");
                btnEditar.setOnAction(e -> iniciarEdicao(getTableView().getItems().get(getIndex())));
                btnExcluir.setOnAction(e -> excluir(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ToolBar bar = new ToolBar(btnEditar, btnExcluir);
                    bar.setStyle("-fx-padding:0; -fx-background-color:transparent;");
                    setGraphic(bar);
                }
            }
        };
        colAcoes.setCellFactory(factory);
    }

    private void configurarEventos() {
        btnNova.setOnAction(e -> novo());
        btnSalvar.setOnAction(e -> salvar());
        btnCancelar.setOnAction(e -> cancelar());
    }

    private void carregar() {
        masterData.setAll(service.listarTodas());
        cbCategoriaPai.setItems(masterData);
    }

    private void novo() {
        editando = null;
        limparFormulario();
        modoEdicao("Novo registro");
    }

    private void iniciarEdicao(Categoria c) {
        editando = c;
        txtNome.setText(c.getNome());
        txtDescricao.setText(c.getDescricao());
        cbCategoriaPai.setValue(c.getCategoriaPai());
        chkAtivo.setSelected(c.isAtivo());
        modoEdicao("Editando: " + c.getNome());
    }

    private void salvar() {
        if (txtNome.getText().isBlank()) {
            alerta("Nome é obrigatório.");
            return;
        }
        try {
            Categoria alvo = editando != null ? editando : new Categoria();
            alvo.setNome(txtNome.getText().trim());
            alvo.setDescricao(txtDescricao.getText().trim());
            alvo.setCategoriaPai(cbCategoriaPai.getValue());
            alvo.setAtivo(chkAtivo.isSelected());

            if (editando == null) {
                service.salvar(alvo);
                masterData.add(alvo);
                alerta("Categoria criada.");
            } else {
                service.atualizar(alvo);
                carregar();
                alerta("Categoria atualizada.");
            }
            cancelar();
        } catch (Exception ex) {
            ex.printStackTrace();
            alerta("Erro: " + ex.getMessage());
        }
    }

    private void excluir(Categoria c) {
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
            "Excluir categoria '" + c.getNome() + "'?", ButtonType.YES, ButtonType.NO);
        conf.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    service.excluir(c.getId());
                    masterData.remove(c);
                } catch (Exception e) {
                    alerta("Não foi possível excluir: " + e.getMessage());
                }
            }
        });
    }

    private void cancelar() {
        limparFormulario();
        modoVisualizacao();
    }

    private void limparFormulario() {
        txtNome.clear();
        txtDescricao.clear();
        cbCategoriaPai.setValue(null);
        chkAtivo.setSelected(true);
    }

    private void modoEdicao(String info) {
        btnSalvar.setDisable(false);
        btnCancelar.setDisable(false);
        btnNova.setDisable(true);
        lblInfo.setText(info);
    }

    private void modoVisualizacao() {
        btnSalvar.setDisable(true);
        btnCancelar.setDisable(true);
        btnNova.setDisable(false);
        lblInfo.setText("Modo: visualização");
    }

    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}