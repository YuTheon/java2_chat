package com.example.client;

import com.example.common.Message;
import com.example.common.Room;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ClientThread implements Runnable{
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private ListView<Message> chatContentList;
    private ListView<Room> chatList;
    private String userName;
    private Message res;
    private boolean getting = false;
    public ClientThread(){}
    public ClientThread(String userName, ObjectInputStream ois, ObjectOutputStream oos, ListView<Message> chatContentList, ListView<Room> chatList){
        this.userName = userName;
        this.ois = ois;
        this.oos = oos;
        this.chatContentList = chatContentList;
        this.chatList = chatList;
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


    private volatile boolean quit = false;
    @Override
    public void run() {
        try {
            /**
             * 这里遇到的问题就是传输对象的话就无法实现异步，输入输出会出现阻塞（）
             * 但是用stream的话就不能传对象Msg
             */
            Object obj;
            Message msg;
            while (!quit){
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
                        case "GROUP":
                            doGROUP(msg);
                            break;
                        case "RGET":
                            doRGET(msg);
                            break;
                        case "RCHAT":
                            doRCHAT(msg);
                            break;
                        case "CHECKONLINE":
                            doCheckOnline(msg);
                            break;
                        case "QUIT":
                            System.out.println("QUIT");
                            quit = true;
                            break;
                        case "SERVERQUIT":
                            doSERQ();
                            quit = true;
                            break;
                    }
                }
            }
        }catch (IOException | ClassNotFoundException exception){
            exception.printStackTrace();
        }
    }

    /**
     * 这里也相当与接收信息，需要更新chatList
     * @param msg
     */
    public void doPOST(Message msg){
//        TODO 这里关键在于msg sendBy是
        List<String> sendTos = Arrays.stream(msg.getSendTo().split(",")).toList();
        String msgName;
        if(sendTos.size()==1){
            msgName = msg.getSentBy();
        }else{
            msgName = msg.getSendTo();
        }
        if(Controller.chatWith.containsKey(msgName)) {
            Controller.chatWith.get(msgName).add(msg);
        }else{
            Controller.chatWith.put(msgName, new ArrayList<>());
            Controller.chatWith.get(msgName).add(msg);
        }
        if(Controller.sendTo != null && Controller.sendTo.equals(msgName)) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    chatContentList.getItems().add(msg);
                }
            });
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Controller.updateChatList(chatList, msg);
            }
        });
    }

    /**
     * 收到建群邀请，
     * 1. 建立信息-chatWith, Room-chatRoom,（检查是否已经建群）
     * 2. 更新左侧消息框
     *
     * 这里的问题，就是发送方不能是群体，因为消息显示发送方，需要再加一个标识表示这是个群聊。回去得稍微整理一下
     * @param msg
     */
    public void doGROUP(Message msg){
        if(Controller.chatWith.containsKey(msg.getSentBy())) {
            Controller.chatWith.get(msg.getSentBy()).add(msg);
        }else{
            Controller.chatWith.put(msg.getSentBy(), new ArrayList<>());
            Controller.chatWith.get(msg.getSentBy()).add(msg);
        }
//        Message ms = new Message(msg.getType(), msg.getTimestamp(),  "SERVER", msg.getSendTo(), msg.getData(), msg.getRoomId());
        if(Controller.sendTo != null && Controller.sendTo.equals(msg.getSentBy())) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    chatContentList.getItems().add(msg);
                }
            });
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Controller.updateChatList(chatList, msg);
            }
        });
    }

    public void doRGET(Message msg){
        res = msg;
        getting = true;
    }
    public void doRCHAT(Message msg){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Controller.chatRoom.get(Controller.sendTo).setId(msg.getRoomId());
            }
        });

    }

    public void doCheckOnline(Message msg){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    oos.writeObject(new Message("ISONLINE", new Date(), msg.getSendTo(), msg.getSentBy(), ""));
                    oos.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    public void doPostFail(Message msg){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText(msg.getData());
                alert.show();
            }
        });
    }
    public void doSERQ(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("server closed");
                alert.show();
            }
        });
    }
}
