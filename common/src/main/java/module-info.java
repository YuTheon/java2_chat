module com.example.common {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.common to javafx.fxml;
    exports com.example.common;
}