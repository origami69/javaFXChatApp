package chatter;

import com.corundumstudio.socketio.SocketIOServer;

public class ServerLauncher {
	public static void main(String[] args) throws InterruptedException {
		 SocketConfig getCon=new SocketConfig();
		 SocketIOServer server =getCon.socketIOServer();
		 new SocketModulator(server);
	     server.start();
	     Thread.sleep(Integer.MAX_VALUE);
	     server.stop();
	}
}