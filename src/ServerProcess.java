import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.*;
import java.util.*;
/**
 * 类名：ServerProcess
 * 描述：接收到客户端socket发来的信息后进行解析、处理、转发。
 */
public class ServerProcess extends Thread{
    private Socket socket = null;// 定义客户端套接字
    private BufferedReader in;// 定义输入流
    private PrintWriter out = new PrintWriter(System.out,true);// 定义输出流
    private static Vector onlineUser = new Vector(10, 5);// 定义向量存储所有在线用户
    private static Vector socketUser = new Vector(10, 5);// 定义向量存储所有在线用户的套接字
    private String strReceive;
    File file = new File("D:\\codefield\\CODE_java\\java_Single\\sever","sever_user.txt");
    private ServerFrame serverFrame = new ServerFrame();
    private String strTalkInfo; // 得到聊天内容
    private String strSender;// 得到发消息人
    private String strReceiver;// 得到接收人
    public ServerProcess() {
    }
    public ServerProcess(Socket client, ServerFrame frame) throws IOException {
        this.socket = client;
        this.serverFrame = frame;
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream())); // 从客户端接收
        this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream())), true);// 向客户端输出
        String strUser = this.in.readLine();
        String[] parts = strUser.split(":",2);
        String str1 = parts[0];
        if(str1.equals("user")){
            onlineUser.addElement(strUser);
            socketUser.addElement(this.socket);
            this.start();
        }
        freshClientsOnline();
    }
    public void run() {
        try {
            while (true) {
                this.strReceive = this.in.readLine();// 从服务器端接收一条信息后拆分、解析，并执行相应操作
                String[] parts = this.strReceive.split("@",2);
                this.strSender = parts[0];
                parts = parts[1].split(":",2);
                this.strReceiver = parts[0];
                this.strTalkInfo = parts[1];
                if(this.strTalkInfo.isEmpty()){
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("提示");
                        alert.setHeaderText(null);
                        alert.setContentText("不能发送空消息哦!");
                        alert.showAndWait();
                    });
                    continue;
                }
                if (strSender.equals(strReceiver)) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("提示");
                        alert.setHeaderText(null);
                        alert.setContentText("不能自言自语哦!");
                        alert.showAndWait();
                    });
                    continue;
                }
                talk();
            }
        } catch (IOException e) { // 用户关闭客户端造成此异常，关闭该用户套接字。
            String leaveUser = closeSocket();
            try {
                freshClientsOnline();
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            System.out.println("[SYSTEM] " + leaveUser + " leave chatroom!");
            sendAll("talk|>>>" + leaveUser + Constants.LEAVE_ROOM);
        }
    }
    /*
    *文件是否存在
    *  */
    void fileIsExists(){
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    /**
     * *判断用户是否存在
     */
    public boolean isUserExist(String strName) throws IOException {
        fileIsExists();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String str;
        while ((str = br.readLine()) != null) {//判断用户是否存在
            if (str.equals(strName)) {
                return true;
            }
        }
        br.close();
        return false;
    }
    /**
     * 判断用户密码是否正确
     */
    public boolean isPasswordCorrect(String strName, String strPassword) throws IOException {
        fileIsExists();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String str;
        while ((str = br.readLine()) != null) {//判断用户密码是否正确
            if (str.equals(strName)) {
                str = br.readLine();
                if (str.equals(strPassword)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        br.close();
        return false;
    }
    /**
     * 用户注册
     * */
    public int register(String strName, String strPassword,String strPasswordConfirm) throws IOException {
        fileIsExists();
        if(isUserExist(strName)){//判断用户是否已经存在
            out.println("warning|用户已经存在，请重新注册！");
            return 1;
        }
        if(!strPassword.equals(strPasswordConfirm)){//判断两次输入的密码是否一致
            out.println("warning|两次输入的密码不一致，请重新输入！");
            return 2;
        }
        if(strName.isEmpty()||strPassword.isEmpty()||strPasswordConfirm.isEmpty()){
            out.println("warning|用户名或密码不能为空！");
            return 5;
        }
        if(strName.equals("All")){
            out.println("warning|用户名不能为All！");
            return 6;
        }
        BufferedReader br = new BufferedReader(new FileReader(file));
        br.close();
        BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
        bw.write(strName + "\r\n");
        bw.write(strPassword + "\r\n");
        bw.flush();
        bw.close();
        out.println("warning|注册成功！");
        return 0;
    }
    /**
     * 用户登录
     * */
    public int login(String strName,String strPassword) throws IOException {
        fileIsExists();
        if (isUserExist(strName)) {//判断用户是否存在
            if (isPasswordCorrect(strName, strPassword)) {//判断用户密码是否正确
                out.println("success|登录成功！");
                return 0;
            } else {
                out.println("warning|密码错误，请重新输入！");
                return 3;
            }
        } else {
            out.println("warning|用户不存在，请先注册！");
            return 4;
        }
    }
    /**
     * 聊天信息处理
     */
    private void talk() throws IOException {
        Socket socketSend;
        PrintWriter outSend;
        String receiver ="user:"+strReceiver;
        // 得到当前时间
        GregorianCalendar calendar = new GregorianCalendar();
        String strTime = "(" + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + ")";
        strTalkInfo += strTime;
        if (strReceiver.equals("all")) {
            sendAll("'"+strSender + "'@all:" + strTalkInfo);
        } else {
            for (int i = 0; i < onlineUser.size(); i++) {
                if (receiver.equals(onlineUser.elementAt(i))) {
                    socketSend = (Socket) socketUser.elementAt(i);
                    outSend = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketSend.getOutputStream())), true);
                    outSend.println("'" + strSender + "'@你说:" + strTalkInfo);
                } else if (strSender.equals(onlineUser.elementAt(i))) {
                    socketSend = (Socket) socketUser.elementAt(i);
                    outSend = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketSend.getOutputStream())), true);
                    outSend.println("我("+strSender+")@" + strReceiver + "说:" + strTalkInfo);
                }
            }
        }
    }
    private void sendToAllClients(String message) {
        try {
            for (int i = 0; i < socketUser.size(); i++) {
                Socket socketSend = (Socket) socketUser.elementAt(i);
                PrintWriter outSend = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketSend.getOutputStream())), true);
                outSend.println(message);
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to send message to all clients");
        }
    }
    /**
     * 在线用户列表
     */
    private void freshClientsOnline() throws IOException {
        String strOnline = "online";
        for (int i = 0; i < onlineUser.size(); i++) {
            strOnline += "|" + onlineUser.elementAt(i);
        }
        Platform.runLater(() -> {
            ObservableList<String> userList = FXCollections.observableArrayList();
            for(Object user : onlineUser){
                String useName = " " + user;
                userList.add(useName);
            }
            serverFrame.txtNumber.setText("" + onlineUser.size());
            serverFrame.lstUser.setItems(userList);
        });
    }
    /**
     * 信息群发
     *
     */
    private void sendAll(String strSend) {
        try {
            for (int i = 0; i < socketUser.size(); i++) {
                Socket socketSend = (Socket) socketUser.elementAt(i);
                PrintWriter outSend = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketSend.getOutputStream())), true);
                outSend.println(strSend);
            }
        } catch (IOException e) {
            System.out.println("[ERROR] send all fail!");
        }
    }
    /**
     * 关闭套接字，并将用户信息从在线列表中删除
     */
    private String closeSocket() {
        String strUser = "";
        for (int i = 0; i < socketUser.size(); i++) {
            if (socket.equals((Socket) socketUser.elementAt(i))) {
                strUser = onlineUser.elementAt(i).toString();
                socketUser.removeElementAt(i);
                onlineUser.removeElementAt(i);
                try {
                    freshClientsOnline();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sendAll("remove|" + strUser);
            }
        }
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("[ERROR] " + e);
        }
        return strUser;
    }
}