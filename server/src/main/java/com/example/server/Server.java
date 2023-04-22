package com.example.server;

import com.example.common.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server  {
    public static List<Room> availableRooms = new ArrayList<>();
    public static List<Room> usingRooms = new ArrayList<>();
    public static Map<String, Socket> onlineUsers = new HashMap<>();
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        /**
         * 限制房间数，设定serverSocket，每监听到一个，就从堆里找一个可用的房间供两个人聊天
         * 房间：存人物，对话，port，状态
         * @onlineUsers 当前在线的用户，每一个新建的用户就会在Map里面存入名字以及对应的联系方式
         */
        final int ACCOUNTS_LIMITS = 20;
        final int PORT = 8895;

        ServerSocket serverSocket = new ServerSocket(PORT);
        serverSocket.setReuseAddress(true);
        System.out.println("set connect, before room");
        while (true){
            /**
             * 连接建立的时候：
             * - 分配房间
             * - 找到对象，进行连接【server和子thread该怎么通信？】
             */
            Socket socket = serverSocket.accept();
            /**
             * 下面进行直接连接
             */
//            System.out.println(new Date());
//            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
//            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//            Message msg = (Message) in.readObject();
//            System.out.println("msg type " + msg.getType());
//            out.writeObject(new Message("POST", 1L, "yy", "yxy", "successful"));
//            out.flush();
//            放进去的应该是一个单独的房间，

            Room room = chooseRoom(availableRooms, usingRooms);
//            TODO 实现server和thread之间的通信，来确定在线人数
            ServerThread serverThread = new ServerThread(socket, room, onlineUsers);
            Thread thread = new Thread(serverThread);
            thread.start();
        }

    }
    public static Room chooseRoom(List<Room> aRooms, List<Room> uRooms){
        if(aRooms.size()==0){
            Room room = new Room();
            uRooms.add(room);
            return room;
        }
        Room room = aRooms.get(0);
        aRooms.remove(room);
        uRooms.add(room);
        return room;
    }
}
