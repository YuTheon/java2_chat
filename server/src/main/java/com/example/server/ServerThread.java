package com.example.server;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
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
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Room room;
    private Map<String, Socket> onlineUsers;

    public ServerThread(Socket socket, Room room, Map<String, Socket> onlineUsers){
        this.socket = socket;
        this.room = room;
        this.onlineUsers = onlineUsers;
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
//                in = new Scanner(socket.getInputStream());
//                out = new PrintWriter(socket.getOutputStream());
//                OutputStream os = socket.getOutputStream();
//                oos = new ObjectOutputStream(os);
//                oos.writeObject(new User(1, "yy"));


//                InputStream is = socket.getInputStream();
//                br = new BufferedReader(new InputStreamReader(is));
                doService();
        }catch (IOException | ClassNotFoundException exception){
            exception.printStackTrace();
        }
    }

    public void doService() throws IOException, ClassNotFoundException {
        ois = new ObjectInputStream(socket.getInputStream());
        Object obj = ois.readObject();
        Message msg = (Message) obj;
        if(msg != null){
            if ("GET".equals(msg.getType())) {
                doGet(msg);
            }
        }
        oos = new ObjectOutputStream(socket.getOutputStream());
        while(true){
            obj = ois.readObject();
            msg = (Message) obj;
            if(msg != null){
                switch (msg.getType()){
                    case "GET":doGet(msg);
                }
            }
        }
    }

    public void doGet(Message message) throws IOException {
        if(message.getData().equals("onlineUsers")){
//            NOTE 要求人名不能有逗号
            String online = String.join(",", Server.onlineUsers.keySet());
            Message rtn = new Message("POST", 1L, "SERVER", message.getSentBy(), online);
            oos.writeObject(rtn);
            oos.flush();
        }
    }
    public void executeCommand(String command) throws IOException {
        /**
         * 暂时只有MSG，QUIT这两个命令
         */
        String[] cmdCnt = command.split("\r\n");
        String[] cmd = cmdCnt[0].split(" ");
        String content = cmdCnt[1];
        switch (cmd[0]){
            case "MSG":
                String sendTo = cmd[1], sendBy = cmd[2];
                Socket socketTo = onlineUsers.get(sendTo);
                out = new PrintWriter(socketTo.getOutputStream());
                String backCnt = "MSG "+sendBy+" "+sendTo+"\r\n"+content;
                out.println(backCnt);
                out.flush();
                return;
            default:
                out.println("QUIT");
                out.flush();
                return;
        }
    }
}
