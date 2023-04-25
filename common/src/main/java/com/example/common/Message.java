package com.example.common;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private String type;

    private Date timestamp;

    private String sentBy;

    private String sendTo;

    private String data;
    private int roomId;

    public Message(){}
    public Message(String type, Date timestamp, String sentBy, String sendTo, String data, int id) {
        this.type = type;
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
        roomId = id;
    }

    public Message(String type, Date timestamp, String sentBy, String sendTo, String data) {
        this.type = type;
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
    }

    public Message(Date timestamp, String sentBy, String sendTo, String data) {
        this.type = "";
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
    }
    @Override
    public String toString(){
        return "Msg: type= " + type + " sentBy= " + sentBy + " sendTo= " + sendTo + " \ndata= " + data;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getSentBy() {
        return sentBy;
    }

    public String getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
