package com.example.server;

import java.io.*;
import java.net.Socket;
import java.util.*;

import com.example.common.*;

public class ServerThread implements Runnable{
    /**
     * 在房间中，找到用户，通过socket进行通信，这里server作为中转站，所以client端收到的信息需要包含谁发过来（single failure）
     * 但是一个用户脱线之后，需要提醒，【用户离线的情况没有处理】
     */
    private Socket socket;
    private Scanner in;
    BufferedReader br;
    private PrintWriter out;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private volatile boolean stop = false;


    private Room room;

    public ServerThread(Socket socket, ObjectInputStream ois, ObjectOutputStream oos){
        this.socket = socket;
        this.ois = ois;
        this.oos = oos;
    }

    /**
     * 对于接收的命令处理，相当于协议处理
     */
    @Override
    public void run() {
            try {
                /**
                 * 这里遇到的问题就是传输对象的话就无法实现异步，输入输出会出现阻塞（）
                 * 但是用stream的话就不能传对象Msg
                 */
                Object obj;
                Message msg;
                while(!stop){
                    obj = ois.readObject();
                    msg = (Message) obj;
                    if(msg != null){
                        switch (msg.getType()){
                            case "GET":doGet(msg);break;
                            case "CHAT":doCHAT(msg);break;
                            case "POST":doPOST(msg);break;
                            case "QUIT":doQUIT(msg);break;
                        }
                    }
                }
        }catch (IOException | ClassNotFoundException exception){
            exception.printStackTrace();
        }
    }


    public void doGet(Message msg) throws IOException {
        if(msg.getData().equals("join")){
//            NOTE 要求人名不能有逗号，如果发来信息的人名不在列表里，就加入列表
            Server.checkOnline();
            String sendBy = msg.getSentBy();
            if(!Server.onlineUsers.containsKey(sendBy)){
                Server.onlineUsers.put(sendBy, socket);
                Server.userOis.put(sendBy, ois);
                Server.userOos.put(sendBy, oos);
                oos.writeObject(new Message("RGET", new Date(), "SERVER", msg.getSentBy(), String.join(",", Server.onlineUsers.keySet())));
            }else{
                oos.writeObject(new Message("FAIL", new Date(), "SERVER", msg.getSentBy(), ""));
            }
        }else if(msg.getData().equals("onlineUsers")){
//            oos.writeObject(new Message("RGET", new Date(), "SERVER", msg.getSentBy(),
//                    String.join(",", Server.checkOnline())));
            Server.checkOnline();
            oos.writeObject(new Message("RGET", new Date(), "SERVER", msg.getSentBy(),
                    String.join(",", Server.onlineUsers.keySet().stream().filter(s -> !s.equals(msg.getSentBy())).toList())));
        }
        oos.flush();
    }

    /**
     * 这里遇到的问题就是 在确定聊天对象之后，room切换到对应的room，然后判断里面是否保存着原来的内容
     * 1. 房间切换，所以主线程里room需要是static（还是别的实现方式）
     * 2. 对于空房间
     * @param msg
     */
    public void doCHAT(Message msg){
        String sendBy = msg.getSentBy(), sendTo = msg.getSendTo();
//        切换房间
        if(Server.usingRooms.size() == 0){
            room = new Room();
            Server.usingRooms.add(room);
        }else{
//            FIXME 如果存在多个怎么办，比如两个同时发起聊天，因为没有synchronization所以同时加入list，首先是目前的使用环境可能没有，其次是需要哪个地方过滤
            Optional<Room> findRoom = Server.usingRooms.stream().filter(s->s.getMemberNum()==2 && s.getData().containsKey(sendBy) && s.getData().containsKey(sendTo)).findFirst();
            if(findRoom.isEmpty()){
                room = new Room();
                Server.usingRooms.add(room);
            }else{
                room = findRoom.get();
            }
        }
//      下面就是在服务器端的线程，已经将用户放在了这个聊天框里
        if(room.getStatus() == RoomStatus.available){
//            完善基本信息
            room.getData().put(sendBy, new ArrayList<>());
            room.getData().put(sendTo, new ArrayList<>());
            room.setMemberNum(2);
            room.setStatus(RoomStatus.using);
        }

    }

    /**
     * CHAT POST 都可能对同一个房间同时进行操作，所以需要设定锁或者什么
     * NOTE 两个oos同时写入会出现问题吗，这里是需要new的oos还是我把oos都放在一个Map里？
     * @param msg
     * @throws IOException
     */
    public void doPOST(Message msg) throws IOException {
//        接下来的操作，就是在房间里记录，然后发送信息给对方（对方也有房间或者什么作为本地缓存）
        String sendBy = msg.getSentBy(), sendTo = msg.getSendTo();
        room.getData().get(sendBy).add(msg);
        room.getData().get(sendBy).add(msg);
        ObjectOutputStream os = Server.userOos.get(sendTo);
        if(os == null){
            oos.writeObject(new Message("POST_FAIL", new Date(), "SERVER", sendBy, "Sorry your friend is offline"));
        }else{
            os.writeObject(msg);
            os.flush();
        }
    }
    public void doQUIT(Message msg) throws IOException {
        String sendBy = msg.getSentBy();
        System.out.println("do quit");
        oos.writeObject(new Message("QUIT", new Date(), "SERVER", msg.getSentBy(), ""));
        oos.flush();
        oos.close();
        ois.close();
        socket.close();
        Server.onlineUsers.remove(sendBy);
        Server.userOos.remove(sendBy);
        Server.userOis.remove(sendBy);
        Server.checkOnline();
        stop = true;
//        System.out.println("STOP");
    }
}
