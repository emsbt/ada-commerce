package br.com.adacommerce.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ViewLoader {

    public static Node loadNode(String path) throws IOException {
        URL url = ViewLoader.class.getResource(path);
        if (url == null) {
            throw new IllegalStateException("FXML não encontrado no classpath: " + path +
                    ". Verifique se existe em src/main/resources" + path);
        }
        FXMLLoader loader = new FXMLLoader(url);
        return loader.load();
    }

    public static void openOnNewStage(String path, String title) throws IOException {
        URL url = ViewLoader.class.getResource(path);
        if (url == null) {
            throw new IllegalStateException("FXML não encontrado no classpath: " + path);
        }
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();
    }
}