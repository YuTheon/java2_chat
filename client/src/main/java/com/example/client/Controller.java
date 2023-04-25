package com.example.client;

import com.example.common.Message;
import com.example.common.Room;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    @FXML
    ListView<Message> chatContentList;
    @FXML
    Label currentUsername;
    @FXML
    Label currentOnlineCnt;
    @FXML
    ListView<Room> chatList;

//    上面是两人聊天，通过人可以唯一确定；下面是群聊，参加人之外需要一点特殊标记-组名
    static Map<String, Room> chatRoom;
    static Map<String, Room> chatRooms;
//  这里是？？
    static Map<String, List<Message>> chatWith;

    static String username;
    static String sendTo;
    static Socket socket;
    static ObjectInputStream ois;
    static ObjectOutputStream oos;
    ClientThread clientThread;
    final int PORT = 8895;
    static boolean QUIT = false;

    /**
     * 这里检查了用户输入的名字是否已经在线
     * @param url
     * @param resourceBundle
     */
    @lombok.SneakyThrows
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        socket = Main.socket;
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Login");
        dialog.setHeaderText(null);
        dialog.setContentText("Username:");
        Optional<String> input = dialog.showAndWait();

        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        while(true) {
            if(input.isPresent()) {
                if (!input.get().isEmpty()) {
                    oos.writeObject(new Message("GET", new Date(), input.get(), "SERVER", "join"));
                    oos.flush();
                    Object obj = ois.readObject();
                    Message rtn = (Message) obj;
                    if (rtn.getType().equals("FAIL")) {
                        System.out.println("change your name, there has been the name!");
                        input = dialog.showAndWait();
                        continue;
                    }
                    List<String> online = Arrays.asList(rtn.getData().split(","));
                    currentOnlineCnt.setText("Online: " + online.size());
                    username = input.get();
                    break;
                }else {
                    QUIT = true;
                }
            }
            else {
                QUIT = true;
                Platform.exit();
                break;
            }
        }
        if(QUIT){
            return;
//            Stage stage = (Stage)chatList.getScene().getWindow();
////            Stage stage = (Stage)chatContentList.getScene().getWindow();
//            stage.close();
        }
        chatContentList.setCellFactory(new MessageCellFactory());
        chatList.setCellFactory(new ChatCellFactory());
        currentUsername.setText("Current User: "+username);
        chatWith = new HashMap<>();
        chatRoom = new HashMap<>();
        chatRooms = new HashMap<>();
//        开启新线程，不断监听server发过来的消息
        clientThread = new ClientThread(username, ois, oos, chatContentList, chatList);
        Thread thread = new Thread(clientThread);
        thread.start();
    }


    /**
     * 接下来就是对不同的按键定义内容了，首先是发送消息，其次是接收消息
     * 发送消息：
     * 1. 选择聊天对象
     * 2. 将页面切换到对应内容
     * 3. 恢复聊天对象的记录
     * 存在问题
     * 1. 两个客户端同时打开私聊对象选择框，有一个的会无法响应
     */
//    TODO 这里ROOM注意，在之前聊天的时候都要在这里面加入消息，前面的list最好改成Map<String, Room>;另外String好像也不是唯一标识码，但是这里暂时这么设定

    @FXML
    public void createPrivateChat() throws IOException, ClassNotFoundException {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        clientThread.setGetting(false);
        oos.writeObject(new Message("GET", new Date(), username, "SERVER", "onlineUsers"));
        oos.flush();
//        TODO 这里判断是否接收到了不好搞，万一处理的是上一个的怎么办，把动作放到另一个类也不好搞
        Message rtn = new Message();
        while(!clientThread.isGetting()) {
            rtn = clientThread.getRes();
        }
        userSel.getItems().addAll(rtn.getData().split(","));

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            if(user.get()!=null) {
                sendTo = user.get();
//            如果这个人已经聊过天，就显示之前的聊天记录，
                if (!chatWith.containsKey(sendTo)) {
                    chatWith.put(sendTo, new ArrayList<>());
                    chatRoom.put(sendTo, new Room(username, sendTo));
                }
                changeChatContentList(chatContentList, sendTo);
//                如果没有聊过，就新建Room
//                FIXME 这一步没有删掉还是？
//                System.out.println("time to delete");
//                System.out.println("sendTo: "+sendTo);
//                System.out.println("chatList : "+ chatRoom.keySet().stream().collect(Collectors.joining(", ")));
//                System.out.println(chatRoom.get(sendTo).getData());
                chatList.getItems().remove(chatRoom.get(sendTo));
                chatList.getItems().add(0, chatRoom.get(sendTo));

                try {
                    oos.writeObject(new Message("CHAT", new Date(), username, sendTo, ""));
                    oos.flush();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            stage.close();
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
        // 如果选择的已经在聊天，就切换
    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() throws IOException {
        AtomicReference<Set<String>> users = new AtomicReference<>();
        users.set(new HashSet<>());

        Stage stage = new Stage();

        clientThread.setGetting(false);
        oos.writeObject(new Message("GET", new Date(), username, "SERVER", "onlineUsers"));
        oos.flush();
//        TODO 这里判断是否接收到了不好搞，万一处理的是上一个的怎么办，把动作放到另一个类也不好搞
        Message rtn = new Message();
        while(!clientThread.isGetting()) {
            rtn = clientThread.getRes();
        }
        List<String> online = Arrays.stream(rtn.getData().split(",")).toList();

        ObservableList<String> items = FXCollections.observableArrayList(online);
        ListView<String> listView = new ListView<>(items);
        listView.setCellFactory(CheckBoxListCell.forListView(new Callback<String, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(String item) {
                BooleanProperty observable = new SimpleBooleanProperty();
                observable.addListener((obs, wasSelected, isNowSelected) -> {
//                    System.out.println("Check box for " + item + " changed from " + wasSelected + " to " + isNowSelected);
                    if(isNowSelected){
                        users.get().add(item);
                    }else{
                        users.get().remove(item);
                    }
                });
                return observable;
            }
        }));


        Button okBtn = new Button("OK");
        okBtn.setOnAction(e->{
            try {
                oos.writeObject(new Message("GROUP", new Date(), username, "SERVER", users.get().stream().collect(Collectors.joining(","))));
                oos.flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            System.out.println(users.get());

//            右边聊天框以及左边消息集，这里对于群聊就是新建，(也不能新建，因为名字是唯一标识符)，怎么定加入chatWith的set呢
            users.get().add(username);
            List<String> groupMem = users.get().stream().sorted().toList();
            String groupName = String.join(",", groupMem);
            sendTo = groupName;
            if(!chatWith.containsKey(groupName)) {
                chatWith.put(groupName, new ArrayList<>());
                chatRoom.put(groupName, new Room(username, groupMem));
            }
            System.out.println("create group " + groupName);
            chatRoom.get(groupName).setShowOnChatList("server: no message");
            chatList.getItems().add(0, chatRoom.get(groupName));
            chatContentList.getItems().clear();
            chatContentList.getItems().addAll(chatWith.get(groupName));

            stage.close();
        });



        HBox box = new HBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(listView, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();
    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    TextArea inputArea;
    @FXML
    public void doSendMessage() throws InterruptedException, IOException {
//        FIXME alert点击确认关不掉，而且最好还是有个不阻塞的小提示框
//        下面是关于输入的检查
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setOnCloseRequest(e->{
            e.consume();
            alert.close();
        });
        if(sendTo == null){
            alert.setContentText("choose someone to chat!");
            alert.show();
            return;
        }
        if (inputArea.getText().trim().isEmpty()){
            alert.setContentText("Blank messages are not allowed.");
            alert.showAndWait();
        }else{
            Message msg = new Message("POST",new Date(), username, sendTo, inputArea.getText().trim());
            /**
             * 发送的时候
             * 1. 更新右边显示
             * 2. TODO 没有太记得chatWith是干嘛，不同人之间的对话会弄混吗这样
             * 3. 更新聊天的room以及chatList(左边的显示框) TODO 注意chatList的更新需要将item插入到最上面
             * 4. 发送消息
             */
            chatContentList.getItems().add(msg);
            chatWith.get(sendTo).add(msg);
            System.out.println("in do send");
            System.out.println(chatRoom.get(sendTo).getData());
            System.out.println("in send msg, sendTo: " + sendTo);

            System.out.println("in chatRoom" + chatRoom.keySet().toString());
            chatList.getItems().remove(chatRoom.get(sendTo));
            chatRoom.get(sendTo).addMsg(msg);
            chatList.getItems().add(0, chatRoom.get(sendTo));

            oos.writeObject(msg);
            oos.flush();
        }
        inputArea.setText("");
    }

    /**
     * 增加消息
     * @param msg
     */
    public static void updateChatContentList(ListView<Message> chatContentList, Message msg){
        chatContentList.getItems().add(msg);
    }

    /**
     * 更换显示内容
     * @param sendTo
     */
    public static void changeChatContentList(ListView<Message> chatContentList, String sendTo){
        chatContentList.getItems().clear();
        chatContentList.getItems().addAll(chatWith.get(sendTo));
    }

    /**
     * 这里是接收到消息之后的全部吗
     * @param chatList
     * @param msg
     */
    public static void updateChatList(ListView<Room> chatList, Message msg){
//        if(chatWith.containsKey(msg.getSentBy())){
//            chatWith.get(msg.getSentBy()).add(msg);
//        }else{
//            chatWith.put(msg.getSentBy(), new ArrayList<>());
//            chatWith.get(msg.getSentBy()).add(msg);
//        }
//        System.out.println("updateChatList:280");
        List<String> sendTos = Arrays.stream(msg.getSendTo().split(",")).toList();
        String msgName;
        if(sendTos.size()==1){
            msgName = msg.getSentBy();
        }else{
            msgName = msg.getSendTo();
        }
        if(!chatRoom.containsKey(msgName)){
            System.out.println("contain no name: "+msgName);
            chatRoom.put(msgName, new Room(msg.getSendTo(), msg.getSentBy()));
            chatRoom.get(msgName).addMsg(msg);
            chatList.getItems().add(0, chatRoom.get(msgName));
        }else {
            System.out.println("contain name");
            chatList.getItems().remove(chatRoom.get(msgName));
            chatRoom.get(msgName).addMsg(msg);
//            chatRoom.get(msg.getSentBy()).getData().get(msg.getSentBy()).add(msg);
            chatList.getItems().add(0, chatRoom.get(msgName));
        }
    }

    public void updateChatContentClick(Room room){
        sendTo = room.getName();
        chatContentList.getItems().clear();
        chatContentList.getItems().addAll(chatWith.get(sendTo));
    }

    @FXML
    public void sendEmoij(){
        AtomicReference<String> emoij = new AtomicReference<>();
        Stage stage = new Stage();
        ComboBox<String> emoijSel = new ComboBox<>();
        emoijSel.getItems().addAll("🤣", "😊","😂","😘","😁","👍","🙌","😜");
        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            emoij.set(emoijSel.getSelectionModel().getSelectedItem());
            inputArea.setText(emoij.get());
            stage.close();
        });
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(emoijSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();
    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
//                        two lines below fix bug of showing msg repeated
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }

    private class ChatCellFactory implements Callback<ListView<Room>, ListCell<Room>> {
        @Override
        public ListCell<Room> call(ListView<Room> param){
            return new ListCell<>(){
                @Override
                public void updateItem(Room room, boolean empty){
                    super.updateItem(room, empty);
                    if (empty || Objects.isNull(room)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    VBox wrapper = new VBox();
                    Label nameLabel = new Label(room.getName());
//                    System.out.println("update chatlist, getName "+room.getName());
//                    System.out.println("room data");
//                    System.out.println(room.getData());
                    Label msgLabel = new Label(room.getShowOnChatList());

                    nameLabel.setPrefSize(100, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: gray;");

                    wrapper.setAlignment(Pos.TOP_LEFT);
                    wrapper.getChildren().addAll(nameLabel, msgLabel);
                    msgLabel.setPadding(new Insets(5, 0, 0, 0));
                    wrapper.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            updateChatContentClick(room);//TODO 还需要chatContentList才能更新
//                            nameLabel.setStyle("-fx-border-color: gray;");
                        }
                    });

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);

                }
            };
        }
    }
}
