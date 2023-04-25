package com.example.server;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;

import java.net.URL;
import java.util.ResourceBundle;

public class MainThread implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("start thread");
        Server server = new Server();
        Thread thread = new Thread(server);
        thread.start();
        System.out.println("thread start");
    }

    @FXML
    public void closeServer() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("click x on top-right :)");
        alert.show();
    }
}
