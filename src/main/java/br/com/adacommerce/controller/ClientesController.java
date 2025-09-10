package br.com.adacommerce.controller;

import br.com.adacommerce.model.Cliente;
import br.com.adacommerce.service.ClienteService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Date;

public class ClientesController {

    @FXML private TableView<Cliente> tableClientes;
    @FXML private TableColumn<Cliente,String> colNome;
    @FXML private TableColumn<Cliente,String> colEmail;
    @FXML private TableColumn<Cliente,String> colDocumento;
    @FXML private TableColumn<Cliente,String> colTelefone;
    @FXML private TableColumn<Cliente,Boolean> colAtivo;

    @FXML private TextField txtFiltro;
    @FXML private TextField txtNome;
    @FXML private TextField txtEmail;
    @FXML private TextField txtDocumento;
    @FXML private TextField txtTelefone;
    @FXML private CheckBox chkAtivo;
    @FXML private Label lblModo;
    @FXML private Button btnNovo;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    private final ClienteService clienteService = new ClienteService();
    private final ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private Cliente emEdicao;

    private enum Modo { VISUAL, NOVO, EDICAO }
    private Modo modo = Modo.VISUAL;

    @FXML
    public void initialize() {
        configurarColunas();
        tableClientes.setItems(clientes);
        carregarClientes();
        tableClientes.getSelectionModel().selectedItemProperty()
                .addListener((obs,old,sel)-> {
                    if (sel != null && modo == Modo.VISUAL) preencherFormulario(sel);
                });
        txtFiltro.textProperty().addListener((o,a,b)->aplicarFiltro(b));
        aplicarModo(Modo.VISUAL);
    }

    private void configurarColunas() {
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEmail()==null? "": c.getValue().getEmail()));
        colDocumento.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDocumento()==null? "": c.getValue().getDocumento()));
        colTelefone.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTelefone()==null? "": c.getValue().getTelefone()));
        colAtivo.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isAtivo()));
        colAtivo.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v==null? null : (v ? "Sim":"Não"));
            }
        });
    }

    private void carregarClientes() {
        clientes.setAll(clienteService.listarTodos());
    }

    private void aplicarFiltro(String termo) {
        if (termo == null || termo.isBlank()) {
            tableClientes.setItems(clientes);
            return;
        }
        String t = termo.toLowerCase();
        tableClientes.setItems(clientes.filtered(c ->
                (c.getNome()!=null && c.getNome().toLowerCase().contains(t)) ||
                        (c.getEmail()!=null && c.getEmail().toLowerCase().contains(t)) ||
                        (c.getDocumento()!=null && c.getDocumento().toLowerCase().contains(t))
        ));
    }

    private void preencherFormulario(Cliente c) {
        txtNome.setText(c.getNome());
        txtEmail.setText(c.getEmail());
        txtDocumento.setText(c.getDocumento());
        txtTelefone.setText(c.getTelefone());
        chkAtivo.setSelected(c.isAtivo());
    }

    private void limparFormulario() {
        txtNome.clear();
        txtEmail.clear();
        txtDocumento.clear();
        txtTelefone.clear();
        chkAtivo.setSelected(true);
    }

    private void aplicarModo(Modo m) {
        modo = m;
        boolean edit = (m != Modo.VISUAL);
        txtNome.setDisable(!edit);
        txtEmail.setDisable(!edit);
        txtDocumento.setDisable(!edit);
        txtTelefone.setDisable(!edit);
        chkAtivo.setDisable(!edit);
        btnSalvar.setDisable(!edit);
        btnCancelar.setDisable(!edit);
        lblModo.setText("Modo: " + (m==Modo.VISUAL?"visualização": m==Modo.NOVO?"novo":"edição"));
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
        tableClientes.getSelectionModel().clearSelection();
    }

    @FXML
    private void onEditar() {
        Cliente sel = tableClientes.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        emEdicao = sel;
        preencherFormulario(sel);
        aplicarModo(Modo.EDICAO);
    }

    @FXML
    private void onSalvar() {
        if (txtDocumento.getText() == null || txtDocumento.getText().isBlank()) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Documento obrigatório!", ButtonType.OK);
            a.showAndWait();
            return;
        }

        try {
            if (!validar()) return;

            if (emEdicao == null) {
                Cliente c = new Cliente();
                c.setNome(txtNome.getText().trim());
                c.setEmail(txtEmail.getText().trim());
                c.setDocumento(txtDocumento.getText().trim());
                c.setTelefone(txtTelefone.getText().trim());
                c.setAtivo(chkAtivo.isSelected());
                c.setDataCriacao(new Date());
                c.setDataAtualizacao(new Date());
                clienteService.salvar(c);
            } else {
                emEdicao.setNome(txtNome.getText().trim());
                emEdicao.setEmail(txtEmail.getText().trim());
                emEdicao.setDocumento(txtDocumento.getText().trim());
                emEdicao.setTelefone(txtTelefone.getText().trim());
                emEdicao.setAtivo(chkAtivo.isSelected());
                emEdicao.setDataAtualizacao(new Date());
                clienteService.salvar(emEdicao);
            }
            carregarClientes();
            limparFormulario();
            aplicarModo(Modo.VISUAL);
        } catch (Exception e) {
            erro("Erro ao salvar", e.getMessage());
        }
    }

    private boolean validar() {
        if (txtNome.getText().isBlank()) { erro("Validação","Nome obrigatório"); return false; }
        return true;
    }

    @FXML
    private void onRemover() {
        Cliente sel = tableClientes.getSelectionModel().getSelectedItem();
        if (sel == null) {
            erro("Remoção", "Selecione um cliente.");
            return;
        }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "Remover o cliente '" + sel.getNome() + "'?", ButtonType.YES, ButtonType.NO);
        conf.setHeaderText("Confirmar exclusão");
        conf.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    clienteService.excluir(sel.getId());
                    carregarClientes();
                    limparFormulario();
                    aplicarModo(Modo.VISUAL);
                } catch (Exception e) {
                    erro("Erro ao remover", e.getMessage());
                }
            }
        });
    }

    private void erro(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(titulo);
        a.showAndWait();
    }
}