package com.example.client;

import com.example.common.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {

    @FXML
    ListView<Message> chatContentList;
    @FXML
    Label currentUsername;
    @FXML
    Label currentOnlineCnt;

    String username;
    String sendTo;
    Socket socket;
    ObjectInputStream ois;
    ObjectOutputStream oos;
    final int PORT = 8895;

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
            if (input.isPresent() && !input.get().isEmpty()) {
                oos.writeObject(new Message("GET", new Date(), input.get(), "SERVER", "onlineUsers"));
                oos.flush();
                Object obj = ois.readObject();
                Message rtn = (Message) obj;
                if(rtn.getType().equals("FAIL")){
                    System.out.println("change your name, there has been the name!");
                    input = dialog.showAndWait();
                    continue;
                }
                List<String> online = Arrays.asList(rtn.getData().split(","));
                System.out.println(online);
                currentOnlineCnt.setText("Online: "+online.size());
                username = input.get();
                break;
            } else {
                System.out.println("Invalid username " + input + ", exiting");
                input = dialog.showAndWait();
            }
        }
        chatContentList.setCellFactory(new MessageCellFactory());
        currentUsername.setText("Current User: "+username);
    }


    /**
     * 接下来就是对不同的按键定义内容了，首先是发送消息，其次是接收消息
     */
    @FXML
    public void createPrivateChat() {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        // FIXME: get the user list from server, the current user's name should be filtered out 获得现有在线用户
        userSel.getItems().addAll("Item 1", "Item 2", "Item 3");

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            sendTo = "yy";
            user.set(userSel.getSelectionModel().getSelectedItem());
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
    public void createGroupChat() {
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
    public void doSendMessage() throws InterruptedException {
//        FIXME alert点击确认关不掉，而且最好还是有个不阻塞的小提示框
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
            chatContentList.getItems().add(new Message(new Date(), username, sendTo, inputArea.getText().trim()));
        }
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
//                        setText(null);
//                        setGraphic(null);
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
}
