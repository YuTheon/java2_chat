package com.example.client;

import com.example.common.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ClientThread implements Runnable{
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private ListView<Message> chatContentList;
    private String userName;
    private Message res;
    private boolean getting = false;
    public ClientThread(){}
    public ClientThread(String userName, ObjectInputStream ois, ObjectOutputStream oos, ListView<Message> chatContentList){
        this.userName = userName;
        this.ois = ois;
        this.oos = oos;
        this.chatContentList = chatContentList;
    }
    public Message getRes(){
        return res;
    }

    public boolean isGetting() {
        return getting;
    }

    public void setGetting(boolean getting) {
        this.getting = getting;
    }


    @Override
    public void run() {
        try {
            /**
             * 这里遇到的问题就是传输对象的话就无法实现异步，输入输出会出现阻塞（）
             * 但是用stream的话就不能传对象Msg
             */
            Object obj;
            Message msg;
            boolean quit = false;
            do {
                obj = ois.readObject();
                msg = (Message) obj;
                System.out.println(msg);
                if (msg != null) {
                    switch (msg.getType()) {
                        case "POST_FAIL":
                            doPostFail(msg);
                            break;
                        case "POST":
                            doPOST(msg);
                            break;
                        case "RGET":
                            doRGET(msg);
                            break;
                        case "QUIT":
                            quit = true;
                            break;
                    }
                }
            } while (!quit);
        }catch (IOException | ClassNotFoundException exception){
            exception.printStackTrace();
        }
    }

    public void doPOST(Message msg){
        if(Controller.chatWith.containsKey(msg.getSentBy())) {
            Controller.chatWith.get(msg.getSentBy()).add(msg);
        }else{
            Controller.chatWith.put(msg.getSentBy(), new ArrayList<>());
            Controller.chatWith.get(msg.getSentBy()).add(msg);
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                chatContentList.getItems().add(msg);
            }
        });
    }

    public void doRGET(Message msg){
        res = msg;
        getting = true;
    }
    public void doPostFail(Message msg){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg.getData());
        alert.show();
    }
}
