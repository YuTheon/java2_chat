package com.example.server;

import com.example.common.Message;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Date;

public class Main extends Application {

    public static void main(String[] args) {
        System.out.println("Starting server");
        launch();
        System.out.println("launch close");
    }

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("sever start");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("server.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Chatting Server");
        System.out.println("server show");
        stage.show();
        stage.setOnCloseRequest(windowEvent -> {

            try {
                /**
                 * 1. 告诉所有在线用户，quit
                 * 2. 保存所有东西
                 * 3. 关闭所有socket
                 */
                Server.userOos.values().forEach(s -> {
                    try {
                        s.writeObject(new Message("SERVERQUIT", new Date(), "", "server", ""));
                        s.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                Thread.sleep(100);
                Server.userOos.values().forEach(s -> {
                    try {
                        s.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                Server.userOis.values().forEach(s -> {
                    try {
                        s.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                Server.onlineUsers.values().forEach(s -> {
                    try {
                        s.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                stage.close();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("server close");
            Platform.exit();
            stage.close();
        });
    }
}
