package br.com.adacommerce.controller;

import br.com.adacommerce.dao.CategoriaDao;
import br.com.adacommerce.model.Categoria;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CategoriasController {

    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private ComboBox<Categoria> cbCategoriaPai;
    @FXML private CheckBox chkAtivo;
    @FXML private Label lblMensagem;

    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria,String> colNome;
    @FXML private TableColumn<Categoria,String> colDescricao;
    @FXML private TableColumn<Categoria,String> colPai;
    @FXML private TableColumn<Categoria,Boolean> colAtivo;

    private final CategoriaDao dao = new CategoriaDao();
    private final ObservableList<Categoria> dados = FXCollections.observableArrayList();
    private Categoria emEdicao;

    @FXML
    public void initialize() {
        configurarColunas();
        recarregar();
        tblCategorias.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> {
            if (b != null) editar(b);
        });
        chkAtivo.setSelected(true);
    }

    private void configurarColunas() {
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colDescricao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescricao()));
        colPai.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCategoriaPai() != null ? c.getValue().getCategoriaPai().getNome() : "")
        );
        colAtivo.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isAtivo()));
        colAtivo.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : (v ? "Sim" : "Não"));
            }
        });
    }

    private void recarregar() {
        dados.setAll(dao.listar());
        tblCategorias.setItems(dados);
        cbCategoriaPai.setItems(dados);
    }

    @FXML
    private void onSalvar() {
        try {
            if (emEdicao == null) {
                Categoria nova = new Categoria();
                nova.setNome(txtNome.getText());
                nova.setDescricao(txtDescricao.getText());
                nova.setAtivo(chkAtivo.isSelected());
                nova.setCategoriaPai(cbCategoriaPai.getValue());
                dao.inserir(nova);
                lblMensagem.setText("Criada ID " + nova.getId());
            } else {
                emEdicao.setNome(txtNome.getText());
                emEdicao.setDescricao(txtDescricao.getText());
                emEdicao.setAtivo(chkAtivo.isSelected());
                emEdicao.setCategoriaPai(cbCategoriaPai.getValue());
                dao.atualizar(emEdicao);
                lblMensagem.setText("Atualizada");
            }
            limpar();
            recarregar();
        } catch (Exception e) {
            e.printStackTrace();
            lblMensagem.setText("Erro: " + e.getMessage());
        }
    }

    @FXML private void onLimpar() { limpar(); }

    @FXML
    private void onExcluir() {
        Categoria sel = tblCategorias.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMensagem.setText("Selecione uma categoria.");
            return;
        }
        try {
            if (!dao.podeExcluir(sel.getId())) {
                lblMensagem.setText("Há subcategorias. Remova vínculo antes.");
                return;
            }
            dao.excluir(sel.getId());
            lblMensagem.setText("Excluída.");
            recarregar();
            limpar();
        } catch (Exception e) {
            e.printStackTrace();
            lblMensagem.setText("Erro: " + e.getMessage());
        }
    }

    private void editar(Categoria c) {
        emEdicao = c;
        txtNome.setText(c.getNome());
        txtDescricao.setText(c.getDescricao());
        chkAtivo.setSelected(c.isAtivo());
        if (c.getCategoriaPai() != null) {
            cbCategoriaPai.getSelectionModel().select(
                    dados.stream()
                            .filter(x -> x.getId().equals(c.getCategoriaPai().getId()))
                            .findFirst().orElse(null));
        } else {
            cbCategoriaPai.getSelectionModel().clearSelection();
        }
    }

    private void limpar() {
        txtNome.clear();
        txtDescricao.clear();
        cbCategoriaPai.getSelectionModel().clearSelection();
        chkAtivo.setSelected(true);
        tblCategorias.getSelectionModel().clearSelection();
        emEdicao = null;
        lblMensagem.setText("");
    }
}