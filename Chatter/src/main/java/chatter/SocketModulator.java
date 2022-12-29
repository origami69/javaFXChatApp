package chatter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;

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
	        server.addEventListener("login", LoginFormat.class, onLogin());
	        server.addEventListener("create", LoginFormat.class, onCreate());
	    }


	    private DataListener<ChatFormat> onChatReceived() {
	        return (senderClient, data, ackSender) -> {
	            log.info(data.getMess() + " " + data.getUser());
	            senderClient.getNamespace().getBroadcastOperations().sendEvent("message", data);
	        };
	    }
	    private DataListener<LoginFormat> onLogin() {
	        return (senderClient, data, ackSender) -> {
	        	if(data.getUser().length()>2 || data.getUser().length()<12 && !data.getUser().contains(";") && !data.getUser().contains(">") && !data.getUser().contains("<") && !data.getUser().contains("/") && !data.getUser().contains("\\")) {
	        		if(data.getPassword().length()>5 || data.getPassword().length()<13 && !data.getPassword().contains(";") && !data.getPassword().contains(">") && !data.getPassword().contains("<") && !data.getPassword().contains("/") && !data.getPassword().contains("\\")) {
	        			String sqlSelectAllPersons = "SELECT * FROM login";
	        			String connectionUrl = "jdbc:mysql://localhost:3306/userdata";
	        			try {
	        				Connection conn = DriverManager.getConnection(connectionUrl, "root", "mysql password"); 
	        			        PreparedStatement ps = conn.prepareStatement(sqlSelectAllPersons);        				
	        			        ResultSet rs = ps.executeQuery(); 
	        			        while (rs.next()) { 
	        			            String userName = rs.getString("username");
	        			            String password = rs.getString("password");
	        			            if(userName.equals(data.getUser())) {
	        			            	if(BCrypt.checkpw(data.getPassword(), password)) {
	        			            		 log.info("User has logged in");
	        			            		 senderClient.sendEvent("logged");
	        			            		 break;
	        			            	}     			            
	        			           }      			           
	        			        }
	        			        conn.close();
	        			
	        			}catch (SQLException e) {
	        				log.info(e.getLocalizedMessage());
	        			}
		        	}	
	        	}		
	        };
	    }
	    private DataListener<LoginFormat> onCreate() {
	        return (senderClient, data, ackSender) -> {  
	        	if(data.getUser().length()>2 || data.getUser().length()<13 && !data.getUser().contains(";") && !data.getUser().contains(">") && !data.getUser().contains("<") && !data.getUser().contains("/") && !data.getUser().contains("\\")) {
	        		if(data.getPassword().length()>5 || data.getPassword().length()<13 && !data.getPassword().contains(";") && !data.getPassword().contains(">") && !data.getPassword().contains("<") && !data.getPassword().contains("/") && !data.getPassword().contains("\\")) {
	        			String connectionUrl = "jdbc:mysql://localhost:3306/userdata";
	        			try {
	        				Connection conn = DriverManager.getConnection(connectionUrl, "root", "mysql password");	
	        				PreparedStatement ps = conn.prepareStatement("SELECT * FROM login");        				
        			        ResultSet rs = ps.executeQuery(); 
        			        while (rs.next()) {
        			            String userName = rs.getString("username");
        			            if(userName.equals(data.getUser())) {
        			            	conn.close();	  
        	        			    break;
        			            }      			           
        			        }
        			        if(!conn.isClosed()) {
        			        	log.info("User Was Created");
        			        	String hashedP = BCrypt.hashpw(data.getPassword(), BCrypt.gensalt());
        			        	String query = "insert into login (username, password)" + " values (?, ?)";
    	        			    PreparedStatement preparedStmt = conn.prepareStatement(query);
    	        			    preparedStmt.setString (1, data.getUser());
    	        			    preparedStmt.setString (2, hashedP);
    	        			    preparedStmt.execute();
    	        			    senderClient.sendEvent("logged");
        			        }      
	        		} catch (SQLException e) {
	        				log.info(e.getLocalizedMessage());
	        			}
		        	}	
	        	}	
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
