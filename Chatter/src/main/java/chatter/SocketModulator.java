package chatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;



public class SocketModulator {
		private Logger log = LoggerFactory.getLogger(SocketModulator.class);
	    private final SocketIOServer server;
	    public SocketModulator(SocketIOServer server) {
	        this.server = server;
	        server.addConnectListener(onConnected());
	        server.addDisconnectListener(onDisconnected());
	        server.addEventListener("message", ChatFormat.class, onChatReceived());
	    }


	    private DataListener<ChatFormat> onChatReceived() {
	        return (senderClient, data, ackSender) -> {
	            log.info(data.getMess() + data.getUser());
	            senderClient.getNamespace().getBroadcastOperations().sendEvent("message", data);
	        };
	    }


	    private ConnectListener onConnected() {
	        return (client) -> {
	            log.info("Socket ID[{}]  Connected to socket", client.getSessionId().toString());
	        };

	    }

	    private DisconnectListener onDisconnected() {
	        return client -> {
	            log.info("Client[{}] - Disconnected from socket", client.getSessionId().toString());
	        };
	    }

}
