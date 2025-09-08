package br.com.adacommerce.controller;

import br.com.adacommerce.model.Usuario;
import br.com.adacommerce.service.UsuarioService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;

public class UsuariosController {

    @FXML private TableView<Usuario> tableUsuarios;
    @FXML private TableColumn<Usuario, String> colNome;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colUsuario;
    @FXML private TableColumn<Usuario, Boolean> colAtivo;
    private Usuario emEdicao = null;

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
        try {
            if (emEdicao == null) {
                // Novo usuário
                Usuario u = new Usuario();
                u.setNome(txtNome.getText());
                u.setEmail(txtEmail.getText());
                u.setUsuario(txtUsuario.getText());
                u.setSenha(txtSenha.getText());
                u.setAtivo(true);
                usuarioService.salvar(u);
            } else {
                // Edição
                emEdicao.setNome(txtNome.getText());
                emEdicao.setEmail(txtEmail.getText());
                emEdicao.setUsuario(txtUsuario.getText());
                emEdicao.setSenha(txtSenha.getText());
                usuarioService.atualizar(emEdicao);
            }
            listarUsuarios();
            limparCampos();
            emEdicao = null;
        } catch (SQLException e) {
            // Mostra um alerta para o usuário
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.setHeaderText("Erro ao salvar usuário");
            alert.showAndWait();
        }
    }

    @FXML
    private void onEditar() {
        Usuario sel = tableUsuarios.getSelectionModel().getSelectedItem();
        if (sel != null) {
            emEdicao = sel;
            txtNome.setText(sel.getNome());
            txtEmail.setText(sel.getEmail());
            txtUsuario.setText(sel.getUsuario());
            txtSenha.setText(sel.getSenha());
        }
    }

    private void limparCampos() {
        txtNome.clear();
        txtEmail.clear();
        txtUsuario.clear();
        txtSenha.clear();
    }
}