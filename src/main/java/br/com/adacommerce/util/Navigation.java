package br.com.adacommerce.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public final class Navigation {

    private static String usuarioLogado;

    private Navigation(){}

    public static void setUsuarioLogado(String u) { usuarioLogado = u; }
    public static String getUsuarioLogado() { return usuarioLogado; }

    public static void showLogin() {
        try {
            Parent root = FXMLLoader.load(Navigation.class.getResource("/fxml/login.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Login - Ada Commerce");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            alertErro("Erro abrindo login: " + e.getMessage());
        }
    }

    public static void showShell() {
        try {
            Parent root = FXMLLoader.load(Navigation.class.getResource("/fxml/shell.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Ada Commerce");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            alertErro("Erro abrindo shell: " + e.getMessage());
        }
    }

    public static void loadInto(StackPane target, String fxmlPath) {
        try {
            Parent node = FXMLLoader.load(Navigation.class.getResource(fxmlPath));
            target.getChildren().setAll(node);
        } catch (Exception e) {
            alertErro("Falha carregando view: " + fxmlPath + "\n" + e.getMessage());
        }
    }

    public static void close(Node any) {
        Stage s = (Stage) any.getScene().getWindow();
        s.close();
    }

    public static void alertErro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setHeaderText("Erro");
        a.showAndWait();
    }

    public static void alertInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}