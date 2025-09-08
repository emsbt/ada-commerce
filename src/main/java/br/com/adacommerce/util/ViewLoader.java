package br.com.adacommerce.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Utilitário central para carregar FXML.
 */
public final class ViewLoader {

    private ViewLoader() {}

    /**
     * Abre um FXML em uma nova Stage.
     */
    public static void openOnNewStage(String fxml, String title) throws IOException {
        Parent root = loadNode(fxml);
        Stage s = new Stage();
        s.setTitle(title);
        s.setScene(new Scene(root));
        s.show();
    }

    /**
     * Carrega e retorna o nó raiz de um FXML.
     * Aceita caminhos como "/fxml/login.fxml" ou "fxml/login.fxml".
     */
    public static Parent loadNode(String fxml) throws IOException {
        String path = normalizePath(fxml);
        FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource(path));
        if (loader.getLocation() == null) {
            throw new IOException("FXML não encontrado no classpath: " + path);
        }
        return loader.load();
    }

    /**
     * Substitui todo o conteúdo de um Pane (ex: StackPane, AnchorPane, BorderPane center)
     * pelo FXML informado.
     */
    public static void setContent(Pane container, String fxml) {
        try {
            Parent node = loadNode(fxml);
            container.getChildren().setAll(node);
        } catch (IOException e) {
            throw new RuntimeException("Falha carregando view: " + fxml + " -> " + e.getMessage(), e);
        }
    }

    private static String normalizePath(String fxml) {
        if (fxml == null || fxml.isBlank()) {
            throw new IllegalArgumentException("Caminho FXML vazio");
        }
        // Garante que começa com /
        return fxml.startsWith("/") ? fxml : (fxml.startsWith("fxml/") ? "/" + fxml : "/fxml/" + fxml);
    }
}