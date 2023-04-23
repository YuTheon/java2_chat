package com.example.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Room {
    private String name;
    private int memberNum;
    private int memNumLim;
    private RoomStatus status;
    private Map<String, List<Message>> data;
    private String showOnChatList;

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
        name = getName();
    }
    public Room(String name1, String name2){
        memberNum = 2;
        memNumLim = 100;
        status = RoomStatus.using;
        name = name2;
        data = new HashMap<>();
        data.put(name1, new ArrayList<>());
        data.put(name2, new ArrayList<>());
    }

    public void clean(){
        data = new HashMap<>();
    }
    public String getName(){
        memberNum = data.size();
        if(memberNum > 3){
            name = data.keySet().stream().limit(3).
                    collect(Collectors.joining(","))+"...("+memberNum+")";
        }else{
            name = String.join(",", data.keySet());
        }
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getShowOnChatList(){
        if(showOnChatList == null){
            showOnChatList = "server:no message";
        }
        return showOnChatList;
    }
    public void setShowOnChatList(String showOnChatList){
        this.showOnChatList = showOnChatList;
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
