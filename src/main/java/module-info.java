module com.tusksmochagarden {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires mysql.connector.j;
    requires jbcrypt;

    opens com.tusksmochagarden.controller to javafx.fxml, javafx.base;
    opens com.tusksmochagarden.model to javafx.fxml, javafx.base;
    exports com.tusksmochagarden.app;
}