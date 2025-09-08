package br.com.adacommerce;

import br.com.adacommerce.config.DatabaseConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdaCommerceApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        DatabaseConfig.initialize();
        // Se quiser passar pela tela de login, abra login.fxml aqui antes.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Ada Commerce");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}