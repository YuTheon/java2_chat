package com.example.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Main extends Application {
//    TODO 不知道这里private怎么在Controller访问，暂时先改成public了
    public static Socket socket;
    public static Scanner in;
    public static PrintWriter out;
    public static void main(String[] args) throws IOException {
        final int PORT = 8894;
        try(Socket s = new Socket("localhost", PORT)){
            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();
            in = new Scanner(is);
            out = new PrintWriter(os);
            socket = s;
        }
        launch();
    }
//    进入launch之后到下面这个start，然后到setscene进入login，之后一直到show，关闭这个窗口launch才结束

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Chatting Client");
        stage.show();
    }
}
