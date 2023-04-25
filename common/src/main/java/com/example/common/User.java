package com.example.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private int status;
    private String name;
    private List<Message> messageList;

    public User() {
    }

    public User(String name) {
        this.name = name;
        messageList = new ArrayList<>();
    }

    public User(int status, String name) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void addMsg(Message message) {
        messageList.add(message);
    }

    public void setMessageList(List<Message> messages) {
        this.messageList = messages;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

}
