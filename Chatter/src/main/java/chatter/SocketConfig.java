package chatter;
import com.corundumstudio.socketio.SocketIOServer;
import java.io.InputStream;


public class SocketConfig {
    private String host = "localhost";

    private Integer port = 8081;

    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setKeyStorePassword("keystore password");
        InputStream stream = SocketConfig.class.getResourceAsStream("keystore path");
        config.setKeyStore(stream);
        return new SocketIOServer(config);
    }
}
