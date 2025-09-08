module ada.commerce {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens br.com.adacommerce.controller to javafx.fxml;
    opens br.com.adacommerce.model to javafx.base; // para TableView se necessário

    exports br.com.adacommerce;
}