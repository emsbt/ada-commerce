package br.com.adacommerce.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ViewLoader {

    public static Node loadNode(String path) throws Exception {
        System.out.println("[ViewLoader] loadNode: " + path);
        FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource(path));
        return loader.load();
    }

    public static void openOnNewStage(String path, String titulo) throws Exception {
        System.out.println("[ViewLoader] openOnNewStage: " + path + " title=" + titulo);
        FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource(path));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(titulo);
        stage.setScene(new Scene(root));
        stage.setMinWidth(1000);
        stage.setMinHeight(640);
        stage.show();
    }
}