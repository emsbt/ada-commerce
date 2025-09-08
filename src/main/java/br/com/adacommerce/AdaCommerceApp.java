package br.com.adacommerce;

import br.com.adacommerce.util.ViewLoader;
import javafx.application.Application;
import javafx.stage.Stage;

public class AdaCommerceApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        ViewLoader.openOnNewStage("/fxml/login.fxml", "Login - Ada Commerce");
    }
    public static void main(String[] args) { launch(args); }
}