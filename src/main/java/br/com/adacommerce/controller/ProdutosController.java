package br.com.adacommerce.controller;

import br.com.adacommerce.model.Categoria;
import br.com.adacommerce.model.Produto;
import br.com.adacommerce.service.CategoriaService;
import br.com.adacommerce.service.ProdutoService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ProdutosController {

    @FXML private TableView<Produto> tableProdutos;
    @FXML private TableColumn<Produto,String> colNome;
    @FXML private TableColumn<Produto,String> colCategoria;
    @FXML private TableColumn<Produto,Number> colPreco;
    @FXML private TableColumn<Produto,Number> colEstoque;
    @FXML private TableColumn<Produto,Boolean> colAtivo;

    @FXML private TextField txtFiltro;
    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private ComboBox<Categoria> comboCategoria;
    @FXML private TextField txtPreco;
    @FXML private TextField txtEstoque;
    @FXML private CheckBox chkAtivo;
    @FXML private Label lblModo;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    private final ProdutoService produtoService = new ProdutoService();
    private final CategoriaService categoriaService = new CategoriaService();
    private final ObservableList<Produto> produtos = FXCollections.observableArrayList();
    private Produto emEdicao;
    private enum Modo { VISUAL, NOVO, EDICAO }
    private Modo modo = Modo.VISUAL;

    @FXML
    public void initialize() {
        configurarColunas();
        tableProdutos.setItems(produtos);
        carregarCategorias();
        carregarProdutos();
        txtFiltro.textProperty().addListener((o, a, b) -> aplicarFiltro(b));
        tableProdutos.getSelectionModel().selectedItemProperty().addListener((o,a,sel)-> {
            if (sel != null && modo == Modo.VISUAL) preencherFormulario(sel);
        });
        aplicarModo(Modo.VISUAL);
    }

    private void configurarColunas() {
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colCategoria.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCategoria() != null ? c.getValue().getCategoria().getNome() : "")
        );
        colPreco.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPreco()));
        colEstoque.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getEstoqueAtual()));
        colAtivo.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isAtivo()));
        colAtivo.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : (v ? "Sim" : "Não"));
            }
        });
    }

    private void carregarCategorias() {
        List<br.com.adacommerce.model.Categoria> cats = categoriaService.listarTodas();
        comboCategoria.getItems().setAll(cats);
    }

    private void carregarProdutos() {
        produtos.setAll(produtoService.listarTodos());
    }

    private void aplicarFiltro(String termo) {
        if (termo == null || termo.isBlank()) {
            tableProdutos.setItems(produtos);
            return;
        }
        String t = termo.toLowerCase();
        tableProdutos.setItems(produtos.filtered(p ->
                (p.getNome()!=null && p.getNome().toLowerCase().contains(t)) ||
                        (p.getDescricao()!=null && p.getDescricao().toLowerCase().contains(t))
        ));
    }

    private void preencherFormulario(Produto p) {
        txtNome.setText(p.getNome());
        txtDescricao.setText(p.getDescricao());
        comboCategoria.getSelectionModel().select(p.getCategoria());
        txtPreco.setText(String.valueOf(p.getPreco()));
        txtEstoque.setText(String.valueOf(p.getEstoqueAtual()));
        chkAtivo.setSelected(p.isAtivo());
    }

    private void limparFormulario() {
        txtNome.clear();
        txtDescricao.clear();
        comboCategoria.getSelectionModel().clearSelection();
        txtPreco.clear();
        txtEstoque.clear();
        chkAtivo.setSelected(true);
    }

    private void aplicarModo(Modo m) {
        modo = m;
        boolean ed = (m != Modo.VISUAL);
        txtNome.setDisable(!ed);
        txtDescricao.setDisable(!ed);
        comboCategoria.setDisable(!ed);
        txtPreco.setDisable(!ed);
        txtEstoque.setDisable(!ed);
        chkAtivo.setDisable(!ed);
        btnSalvar.setDisable(!ed);
        btnCancelar.setDisable(!ed);
        lblModo.setText("Modo: " + (m == Modo.VISUAL ? "visualização" : (m == Modo.NOVO ? "novo" : "edição")));
    }

    @FXML
    private void onNovo() {
        emEdicao = null;
        limparFormulario();
        aplicarModo(Modo.NOVO);
    }

    @FXML
    private void onCancelar() {
        emEdicao = null;
        limparFormulario();
        aplicarModo(Modo.VISUAL);
    }

    @FXML
    private void onEditar() {
        Produto sel = tableProdutos.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        emEdicao = sel;
        preencherFormulario(sel);
        aplicarModo(Modo.EDICAO);
    }

    @FXML
    private void onRemover() {
        Produto sel = tableProdutos.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,"Remover produto?", ButtonType.YES, ButtonType.NO);
        conf.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    produtoService.excluir(sel.getId());
                    carregarProdutos();
                    limparFormulario();
                    aplicarModo(Modo.VISUAL);
                } catch (Exception e) { erro("Erro ao remover", e.getMessage()); }
            }
        });
    }

    @FXML
    private void onSalvar() {
        try {
            if (!validar()) return;
            if (emEdicao == null) {
                Produto p = new Produto();
                p.setNome(txtNome.getText().trim());
                p.setDescricao(txtDescricao.getText().trim());
                p.setCategoria(comboCategoria.getValue());
                p.setPreco(Double.parseDouble(txtPreco.getText().trim()));
                p.setEstoqueAtual(Integer.parseInt(txtEstoque.getText().trim()));
                p.setAtivo(chkAtivo.isSelected());
                p.setDataCriacao(new Date());
                p.setDataAtualizacao(new Date());
                produtoService.salvar(p);
            } else {
                emEdicao.setNome(txtNome.getText().trim());
                emEdicao.setDescricao(txtDescricao.getText().trim());
                emEdicao.setCategoria(comboCategoria.getValue());
                emEdicao.setPreco(Double.parseDouble(txtPreco.getText().trim()));
                emEdicao.setEstoqueAtual(Integer.parseInt(txtEstoque.getText().trim()));
                emEdicao.setAtivo(chkAtivo.isSelected());
                emEdicao.setDataAtualizacao(new Date());
                produtoService.atualizar(emEdicao);
            }
            carregarProdutos();
            limparFormulario();
            aplicarModo(Modo.VISUAL);
        } catch (Exception e) {
            erro("Erro ao salvar", e.getMessage());
        }
    }

    private boolean validar() {
        if (txtNome.getText().isBlank()) { erro("Validação","Nome obrigatório"); return false; }
        if (txtPreco.getText().isBlank()) { erro("Validação","Preço obrigatório"); return false; }
        try { Double.parseDouble(txtPreco.getText().trim()); } catch (NumberFormatException e) {
            erro("Validação","Preço inválido"); return false;
        }
        try { Integer.parseInt(txtEstoque.getText().trim()); } catch (NumberFormatException e) {
            erro("Validação","Estoque inválido"); return false;
        }
        return true;
    }

    private void erro(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(titulo);
        a.showAndWait();
    }
}