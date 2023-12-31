import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.*;

public class ChatSever extends Application {
    public boolean bServerIsRunning = false;
    ServerFrame serverFrame = null;
    ServerSocket serverSocket = null;
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        // 服务器界面
        serverFrame = new ServerFrame();
        FlowPane root = new FlowPane();
        root.getChildren().add(serverFrame.getUI());
        getServerIP();
        Scene scene = new Scene(root, 300, 500);
        primaryStage.setTitle("服务器");
        primaryStage.setScene(scene);
        primaryStage.show();
        startServer(serverFrame);
    }
    public void startServer(ServerFrame serverFrame) {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(Constants.SERVER_PORT); // 启动服务
                bServerIsRunning = true;
                while (bServerIsRunning) {
                    Socket socket = serverSocket.accept(); // 监听客户端的连接请求，并返回客户端socket
                    new ServerProcess(socket, serverFrame); // 创建一个新线程来处理与该客户的通讯
                }
            } catch (BindException e) {
                System.out.println("端口使用中....");
                System.out.println("请关掉相关程序并重新运行服务器！");
                System.exit(0);
            } catch (IOException e) {
                System.out.println("[ERROR] Cound not start server." + e);
                throw new RuntimeException(e);
            }
        }).start();
        // 启动线程
    }
    public void getServerIP() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            String ip = addr.getHostAddress();
            serverFrame.setTxtIP(ip);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
