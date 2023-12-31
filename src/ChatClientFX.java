import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClientFX extends Application {

    private final TextField inputField = new TextField();
    private final TextArea chatArea = new TextArea();
    private final Button sendButton = new Button("Send");

    private Socket socket;
    private DataOutputStream out;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox(chatArea);
        Label chatLabel = new Label("发送消息:");
        HBox hbox = new HBox(chatLabel,inputField, sendButton);
        vbox.getChildren().add(hbox);
        Scene scene = new Scene(vbox, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chat Client界面");
        primaryStage.show();

        try {
            socket = new Socket("localhost", 8888);
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    while (true) {
                        String message = in.readUTF();
                        Platform.runLater(() -> chatArea.appendText("Client said:"+message + "\n"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

        sendButton.setOnAction(event -> {
            String message = inputField.getText();
            try {
                out.writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputField.clear();
        });
    }
}