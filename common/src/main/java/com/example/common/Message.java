package com.example.common;

import java.io.Serializable;

public class Message implements Serializable {
    private String type;

    private Long timestamp;

    private String sentBy;

    private String sendTo;

    private String data;

    public Message(){}

    public Message(String type, Long timestamp, String sentBy, String sendTo, String data) {
        this.type = type;
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
    }

    public Message(Long timestamp, String sentBy, String sendTo, String data) {
        this.type = "";
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
    }
    @Override
    public String toString(){
        return "Msg [sentBy= " + sentBy + ", sendTo= " + sendTo + ", \ndata= " + data + "]";
    }

    public Long getTimestamp() {
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
