module ada.commerce {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.mindrot.jbcrypt;

    opens br.com.adacommerce.controller to javafx.fxml;
    opens br.com.adacommerce.config to javafx.fxml;
    opens br.com.adacommerce.report to javafx.fxml;

    exports br.com.adacommerce;
}