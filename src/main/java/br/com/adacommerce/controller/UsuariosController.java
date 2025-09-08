package br.com.adacommerce.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UsuariosController {

    @FXML private TableView<UsuarioVM> tableUsuarios;
    @FXML private TableColumn<UsuarioVM, String> colLogin;
    @FXML private TableColumn<UsuarioVM, String> colNome;
    @FXML private TableColumn<UsuarioVM, Boolean> colAtivo;

    @FXML private TextField txtLogin;
    @FXML private TextField txtNome;
    @FXML private PasswordField txtSenha;
    @FXML private CheckBox chkAtivo;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    private final ObservableList<UsuarioVM> dados = FXCollections.observableArrayList();
    private UsuarioVM emEdicao;

    @FXML
    public void initialize() {
        colLogin.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().login()));
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nome()));
        colAtivo.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().ativo()));
        colAtivo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? "Sim" : "Não"));
            }
        });
        tableUsuarios.setItems(dados);

        dados.addAll(
                new UsuarioVM("admin","Administrador", true),
                new UsuarioVM("joao","João Silva", true),
                new UsuarioVM("maria","Maria Souza", false)
        );

        tableUsuarios.getSelectionModel().selectedItemProperty()
                .addListener((o, old, sel) -> {
                    if (sel != null && btnSalvar.isDisabled()) preencher(sel);
                });
    }

    @FXML
    private void onNovo() {
        limpar();
        setEdicao(true);
        emEdicao = null;
    }

    @FXML
    private void onSalvar() {
        if (txtLogin.getText().isBlank()) { alerta("Login é obrigatório"); return; }
        if (emEdicao == null) {
            dados.add(new UsuarioVM(txtLogin.getText(), txtNome.getText(), chkAtivo.isSelected()));
        } else {
            dados.remove(emEdicao);
            dados.add(new UsuarioVM(txtLogin.getText(), txtNome.getText(), chkAtivo.isSelected()));
        }
        dados.sort((a,b) -> a.login().compareToIgnoreCase(b.login()));
        limpar();
        setEdicao(false);
    }

    @FXML
    private void onCancelar() {
        limpar();
        setEdicao(false);
    }

    private void preencher(UsuarioVM vm) {
        txtLogin.setText(vm.login());
        txtNome.setText(vm.nome());
        chkAtivo.setSelected(vm.ativo());
    }

    private void limpar() {
        txtLogin.clear();
        txtNome.clear();
        txtSenha.clear();
        chkAtivo.setSelected(true);
    }

    private void setEdicao(boolean ed) {
        txtLogin.setDisable(!ed);
        txtNome.setDisable(!ed);
        txtSenha.setDisable(!ed);
        chkAtivo.setDisable(!ed);
        btnSalvar.setDisable(!ed);
        btnCancelar.setDisable(!ed);
    }

    private void alerta(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    public record UsuarioVM(String login, String nome, boolean ativo) {}
}