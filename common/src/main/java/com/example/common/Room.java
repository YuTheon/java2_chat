package com.example.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Room {
    private int memberNum;
    private int memNumLim;
    private RoomStatus status;
    private Map<String, List<Message>> data;

    public Room(){
        memberNum = 0;
        memNumLim = 500;
        data = new HashMap<>();
        status = RoomStatus.available;
    }
    public Room(Map<String, List<Message>> data) {
        memberNum = data.size();
        memNumLim = 100;
        this.data = data;
        status = RoomStatus.using;
    }

    public void clean(){
        data = new HashMap<>();
    }

    public int getMemberNum() {
        return memberNum;
    }

    public void setMemberNum(int memberNum) {
        this.memberNum = memberNum;
    }

    public int getMemNumLim() {
        return memNumLim;
    }

    public void setMemNumLim(int memNumLim) {
        this.memNumLim = memNumLim;
    }

    public Map<String, List<Message>> getData() {
        return data;
    }

    public void setData(Map<String, List<Message>> data) {
        this.data = data;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }
}
