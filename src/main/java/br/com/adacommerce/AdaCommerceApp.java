package br.com.adacommerce;

import br.com.adacommerce.config.DatabaseConfig;
import br.com.adacommerce.util.ViewLoader;
import javafx.application.Application;
import javafx.stage.Stage;

public class AdaCommerceApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Inicializa banco antes de abrir login
        DatabaseConfig.initialize();
        ViewLoader.openOnNewStage("/fxml/login.fxml", "Login - Ada Commerce");
    }

    public static void main(String[] args) {
        launch(args);
    }
}