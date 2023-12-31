import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;


public class ClientFX extends Application {

    Socket socket = null;
    BufferedReader in = null;
    PrintWriter out = null;
    TextField textFieldIP;
    ServerProcess serverProcess = new ServerProcess();

    public TextArea textAreaChat = new TextArea();
    UI ui = new UI();
    @Override
    public void start(Stage primaryStage) throws Exception {
        loginUI(primaryStage);
    }
    /*
     * 用户已经存在界面和两次密码不一致界面
     */
    private void userProblemUI(int i) {
        Stage stage = new Stage();
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        Label label = new Label();
        Button button = new Button("确定");
        vBox.getChildren().addAll(label, button);

        button.setOnAction(event -> stage.close());
        switch (i){
            case 1:
                label.setText("用户已经存在，请重新注册！");
                break;
            case 2:
                label.setText("两次密码不一致，请重新注册！");
                break;
            case 3:
                label.setText("密码错误，请重新输入！");
                break;
            case 4:
                label.setText("用户不存在，请先注册！");
                break;
            case 5:
                label.setText("用户名或密码不能为空！");
                break;
            case 6:
                label.setText("用户名不能为all！");
                break;
        }
        Scene scene = new Scene(vBox, 200, 100);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
    /**
     * 登录界面
     */
    private void loginUI(Stage primaryStage) {
        Label labelIP = new Label("服务器IP:");
        textFieldIP = new TextField();
        textFieldIP.setText("127.0.0.1");
        HBox hBoxIP = new HBox();
        hBoxIP.setAlignment(Pos.CENTER_RIGHT);
        hBoxIP.getChildren().addAll(labelIP, textFieldIP);
        ui.vBox.getChildren().add(hBoxIP);
        Button buttonLogin = new Button("登录");
        Button buttonRegister = new Button("注册");
        HBox hBoxButton = new HBox();
        hBoxButton.setAlignment(Pos.CENTER);
        hBoxButton.getChildren().addAll(buttonLogin, buttonRegister);
        ui.vBox.getChildren().add(hBoxButton);
        buttonLogin.setOnAction(event ->{
            String strName = ui.textFieldName.getText();
            String strPassword = ui.passwordField.getText();
            try {
                int i = serverProcess.login(strName, strPassword);

                if(i==0){//登录成功
                    primaryStage.close();
                    chatUI();
                } else if (i==3) {//密码错误，请重新输入！
                    userProblemUI(i);
                    ui.passwordField.setText("");
                } else if (i==4) {//用户不存在，请先注册！
                    userProblemUI(i);
                    ui.textFieldName.setText("");
                    ui.passwordField.setText("");
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        buttonRegister.setOnAction(event -> registerUI(primaryStage));
        primaryStage.setScene(ui.scene);
        primaryStage.setTitle("登录窗口");
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    /**
     * 注册界面
     */
    private void registerUI(Stage primaryStage) {
        UI ui = new UI();
        Label labelPasswordConfirm = new Label("确认密码:");
        PasswordField textFieldPasswordConfirm = new PasswordField();
        HBox HBoxPasswordConfirm = new HBox();
        HBoxPasswordConfirm.setAlignment(Pos.CENTER_RIGHT);
        HBoxPasswordConfirm.getChildren().addAll(labelPasswordConfirm, textFieldPasswordConfirm);
        ui.vBox.getChildren().add(HBoxPasswordConfirm);
        Button buttonRegister = new Button("注册");
        HBox hBoxButton = new HBox();
        hBoxButton.setAlignment(Pos.CENTER);
        hBoxButton.getChildren().add(buttonRegister);
        ui.vBox.getChildren().add(hBoxButton);
        buttonRegister.setOnAction(event ->{
            String strName = ui.textFieldName.getText();
            String strPassword = ui.passwordField.getText();
            String strPasswordConfirm = textFieldPasswordConfirm.getText();
            try {
                int i = serverProcess.register(strName, strPassword, strPasswordConfirm);
                if(i==0){
                    primaryStage.close();
                    loginUI(primaryStage);
                }else if(i==1){//用户已经存在，请重新注册！
                    userProblemUI(i);
                    ui.textFieldName.setText("");
                    ui.passwordField.setText("");
                    textFieldPasswordConfirm.setText("");
                } else if (i==2) {//两次密码不一致，请重新注册！
                    userProblemUI(i);
                    ui.passwordField.setText("");
                    textFieldPasswordConfirm.setText("");
                } else if (i==5) {//用户名或密码不能为空
                    userProblemUI(i);
                    ui.textFieldName.setText("");
                    ui.passwordField.setText("");
                    textFieldPasswordConfirm.setText("");
                } else if (i==6) {//用户名不能为all
                    userProblemUI(i);
                    ui.textFieldName.setText("");
                    ui.passwordField.setText("");
                    textFieldPasswordConfirm.setText("");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        primaryStage.setScene(ui.scene);
        primaryStage.setTitle("注册窗口");
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    /*
     * 聊天界面
     */
    public void chatUI(){
        connectServer();
        stageScene stageSceneChat = new stageScene("聊天窗口");
        Label labelChat = new Label("输入聊天内容:");
        TextField textFieldChat = new TextField();
        Button buttonSend = new Button("发送");
        Button buttonReceiver = new Button("选择接收者");

        HBox hBoxChat = new HBox();
        hBoxChat.setAlignment(Pos.CENTER);
        hBoxChat.getChildren().addAll(labelChat, textFieldChat, buttonReceiver , buttonSend);
        buttonReceiver.setOnAction(event -> {
            Stage stage = new Stage();
            VBox vBox = new VBox();
            ListView<String> listView = new ListView<>();
            ObservableList<String> users= FXCollections.observableArrayList("all");
            listView.setItems(users);
            listView.setOnMouseClicked(e->{
                String selectedUser = listView.getSelectionModel().getSelectedItem();
                textFieldChat.setText("@"+selectedUser+":");
                stage.close();
            });
            vBox.getChildren().addAll(listView);
            Scene scene = new Scene(vBox, 200, 100);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        });
        buttonSend.setOnAction(event -> {
            String strChat =ui.textFieldName.getText()+textFieldChat.getText();
            String strChat1="我"+"("+ui.textFieldName.getText()+")" +textFieldChat.getText();
            String[] parts1 = strChat.split("@",2);
            String[] parts2 = parts1[1].split(":",2);
            String I=parts1[0];
            String You=parts2[0];
            String message=parts2[1];
            if (!message.isEmpty() && !You.equals(I)) {
                textAreaChat.appendText(strChat1 + "\n");
            }

            out.println(strChat);
            textFieldChat.setText("");
        });

        stageSceneChat.flowPane.getChildren().addAll(textAreaChat, hBoxChat);
        stageSceneChat.stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
    /*
     * 建立与服务器的套接字
     */
    void connectServer() {
        try{
            System.out.println("正在连接服务器...");
            socket = new Socket(textFieldIP.getText(), 8888);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("服务器连接成功！");
            out.println("user:"+ui.textFieldName.getText());
            new ClientThread(socket, textAreaChat);

        }catch (IOException e){
            System.out.println("服务器连接失败！");
            throw new RuntimeException(e);
        }
    }
}
