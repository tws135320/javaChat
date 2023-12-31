import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ServerFrame {
    // UI 组件
    private TextField txtServerName;
    private TextField txtIP;
    private TextField txtPort;
    public TextField txtNumber;
    private Label labelServerName;
    private Label labelIP;
    private Label labelPort;
    private Label labelNumber;
    private HBox hBoxServerName;
    private HBox hBoxIP;
    private HBox hBoxPort;
    private VBox vBox;
    public ListView<String> lstUser;

    public ServerFrame() {
        // 初始化 UI 组件
        initializeComponents();
        // 设置布局
        setUpLayout();
    }

    private void initializeComponents() {
        txtServerName = new TextField();
        txtIP = new TextField();
        txtPort = new TextField();
        txtNumber = new TextField();
        labelServerName = new Label("服务器名称：");
        labelIP = new Label("服务器IP：");
        labelPort = new Label("服务器端口：");
        labelNumber = new Label("在线人数：");
        txtPort.setText("8888");
        txtIP.setText("127.0.0.1");
        txtServerName.setText("服务器");
        txtNumber.setText("0");
        hBoxServerName = new HBox(labelServerName, txtServerName);
        hBoxIP = new HBox(labelIP, txtIP);
        hBoxPort = new HBox(labelPort, txtPort);
        lstUser = new ListView<>();
        vBox = new VBox(hBoxServerName, hBoxIP, hBoxPort, labelNumber, txtNumber, lstUser);
    }
    private void setUpLayout() {
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));
    }
    public void setTxtIP(String strIP) {
        this.txtIP.setText(strIP);
    }
    public VBox getUI() {
        return vBox;
    }
}
