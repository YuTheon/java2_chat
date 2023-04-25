package com.example.client;

import com.example.common.Message;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    //    TODO 不知道这里private怎么在Controller访问，暂时先改成public了
    public static Socket socket;
    public static Scanner in;
    public static PrintWriter out;

    public static void main(String[] args) throws IOException {
        final int PORT = 8895;
        socket = new Socket("localhost", PORT);
        launch();
    }
//    进入launch之后到下面这个start，然后到setscene进入login，之后一直到show，关闭这个窗口launch才结束

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Chatting Client");
        stage.show();
        stage.setOnCloseRequest(windowEvent -> {

            try {
                Controller.oos.writeObject(new Message("QUIT", new Date(),
                        Controller.username, "SERVER", ""));
                Controller.oos.flush();
                Thread.sleep(100);
                Controller.oos.close();
                Controller.ois.close();
                Controller.socket.close();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            Platform.exit();
            stage.close();
        });
    }

}
