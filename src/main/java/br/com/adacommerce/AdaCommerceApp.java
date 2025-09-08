package br.com.adacommerce;

import javafx.application.Application;
import javafx.stage.Stage;
import br.com.adacommerce.util.Navigation;

public class AdaCommerceApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Navigation.showLogin(); // abre a tela de login
    }

    public static void main(String[] args) {
        launch(args);
    }
}