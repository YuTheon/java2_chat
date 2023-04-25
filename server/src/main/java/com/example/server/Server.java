package com.example.server;

import com.example.common.Message;
import com.example.common.Room;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.*;

/**
 * FIXME 关于同时使用oos导致的传输问题，如果在自己电脑上点点应该不会有这样的问题，暂时不处理；处理的话就是将写操作都放在Server里，对每一个oos加一个锁
 */
public class Server implements Runnable {
    public static int id=0;
    public static Map<Integer, Room> findRoom = new HashMap<>();
    public static List<Room> usingRooms = new ArrayList<>();
    public static Map<String, Socket> onlineUsers = new HashMap<>();
    public static Map<String, ObjectOutputStream> userOos = new HashMap<>();
    public static Map<String, ObjectInputStream> userOis = new HashMap<>();

    @Override
    public void run() {
        /**
         * 限制房间数，设定serverSocket，每监听到一个，就从堆里找一个可用的房间供两个人聊天
         * 房间：存人物，对话，port，状态
         * @onlineUsers 当前在线的用户，每一个新建的用户就会在Map里面存入名字以及对应的联系方式
         */
        final int ACCOUNTS_LIMITS = 20;
        final int PORT = 8895;

        ServerSocket serverSocket;
        try {
            System.out.println("serverSocket");
            serverSocket = new ServerSocket(PORT);
            serverSocket.setReuseAddress(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("set connect, before room");
        Socket socket = null;
        ObjectInputStream ois;
        ObjectOutputStream oos;
        System.out.println(serverSocket.getLocalPort());
        while (true){
            /**
             * 连接建立的时候：
             * - 分配房间
             * - 找到对象，进行连接【server和子thread该怎么通信？目前是利用static资源】
             */
            try {
                socket = serverSocket.accept();
                ois = new ObjectInputStream(socket.getInputStream());
                oos = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            /**
             * 下面进行直接连接
             */
            ServerThread serverThread = new ServerThread(socket, ois, oos);
            Thread thread = new Thread(serverThread);
            thread.start();
        }
    }


    public static List<String> checkOnline(){
        List<String> name = onlineUsers.keySet().stream().toList();
        List<String> res = new ArrayList<>();
        for(String n : name){
            if(onlineUsers.get(n).isConnected()){
                res.add(n);
//                System.out.println("online "+n);
            }else{
//                System.out.println("remove "+n);
                onlineUsers.remove(n);
                userOos.remove(n);
                userOis.remove(n);
            }
        }
//        System.out.println("------------------------------");
        return res;
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
