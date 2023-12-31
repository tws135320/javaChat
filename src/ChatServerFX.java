import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServerFX extends Application {

    private final TextArea chatArea = new TextArea();

    private ServerSocket serverSocket;
    private final List<DataOutputStream> clientOutputStreams = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox(chatArea);
        Scene scene = new Scene(vbox, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();

        try {
            serverSocket = new ServerSocket(8888);

            new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                        clientOutputStreams.add(out);

                        new Thread(() -> {
                            try {
                                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                                while (true) {
                                    String message = in.readUTF();
                                    chatArea.appendText("Client said: " + message + "\n");
                                    for (DataOutputStream clientOut : clientOutputStreams) {
                                        clientOut.writeUTF(message);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).start();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}