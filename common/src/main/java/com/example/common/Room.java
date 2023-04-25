package com.example.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Room {
    private int id;
    private String name;
    private String host;
    private int memberNum;
    private int memNumLim;
    private RoomStatus status;
    private Map<String, List<Message>> data;
    private String showOnChatList;
    private int getInfo = 0;

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
        host = name1;
        name = name2;
        data = new HashMap<>();
        data.put(name1, new ArrayList<>());
        data.put(name2, new ArrayList<>());
    }
    public Room(String host, List<String> cus){
        memNumLim = 100;
        status = RoomStatus.using;
        this.host = host;
        data = new HashMap<>();
        data.put(host, new ArrayList<>());
        cus.stream().sorted().forEach(s-> data.put(s, new ArrayList<>()));
        memberNum = data.keySet().size();
        if(cus.size() > 2) {
            name = cus.stream().sorted().limit(3).collect(Collectors.joining(",")) + "..." + "(" + memberNum + ")";
        }else{
            name = cus.stream().sorted().collect(Collectors.joining(","))+","+host;
        }
    }

    public int getGetInfo() {
        return getInfo;
    }

    public void setGetInfo(int getInfo) {
        this.getInfo = getInfo;
    }

    public void clean(){
        data = new HashMap<>();
    }
    public void addMsg(Message msg){
        if(!data.containsKey(msg.getSentBy())){
            data.put(msg.getSentBy(), new ArrayList<>());
        }
        data.get(msg.getSentBy()).add(msg);
        showOnChatList = msg.getSentBy() + ": " + msg.getData();
    }
    public String getName(){
        memberNum = data.size();
        if(name == null) {
            if (memberNum > 3) {
                name = data.keySet().stream().limit(3).
                        collect(Collectors.joining(",")) + "...(" + memberNum + ")";
            } else {
                name = String.join(",", data.keySet());
            }
        }
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
