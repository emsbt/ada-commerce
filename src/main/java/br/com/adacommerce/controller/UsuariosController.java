package br.com.adacommerce.controller;

import br.com.adacommerce.model.Usuario;
import br.com.adacommerce.service.UsuarioService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UsuariosController {

    @FXML private TableView<Usuario> tableUsuarios;
    @FXML private TableColumn<Usuario, String> colNome;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colUsuario;
    @FXML private TableColumn<Usuario, Boolean> colAtivo;

    @FXML private TextField txtNome;
    @FXML private TextField txtEmail;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtSenha;
    @FXML private Button btnSalvar;

    private final UsuarioService usuarioService = new UsuarioService();
    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNome()));
        colEmail.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
        colUsuario.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUsuario()));
        colAtivo.setCellValueFactory(data -> new javafx.beans.property.SimpleBooleanProperty(data.getValue().isAtivo()));
        tableUsuarios.setItems(usuarios);
        listarUsuarios();
    }

    private void listarUsuarios() {
        usuarios.setAll(usuarioService.listar());
    }

    @FXML
    private void onSalvar() {
        Usuario u = new Usuario();
        u.setNome(txtNome.getText());
        u.setEmail(txtEmail.getText());
        u.setUsuario(txtUsuario.getText());
        u.setSenha(txtSenha.getText());
        u.setAtivo(true);
        usuarioService.salvar(u);
        listarUsuarios();
        limparCampos();
    }

    private void limparCampos() {
        txtNome.clear();
        txtEmail.clear();
        txtUsuario.clear();
        txtSenha.clear();
    }
}