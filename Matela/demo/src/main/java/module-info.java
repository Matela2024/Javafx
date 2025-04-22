module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.h2database;

    opens com.example.demo to javafx.fxml;
    exports com.example.demo;
}