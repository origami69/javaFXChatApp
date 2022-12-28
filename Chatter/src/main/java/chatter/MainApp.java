package chatter;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.socket.client.IO;
import io.socket.client.IO.Options;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import okhttp3.OkHttpClient;


public class MainApp {

	public static void main(String[] args) {
		Application.launch(RealApp.class);
	}

	public static class RealApp extends Application {
	    private Socket mSocket;
	    private String userN;
	    private Label serverStatus = new Label();;
	    private TextArea makeChat;
	    private TextArea showChat;
	    private Button sendTo;

	@Override
	public void start(Stage primaryStage) throws Exception{
		HostnameVerifier hostnameVerifier = new HostnameVerifier() {
		    @Override
			public boolean verify(String hostname, SSLSession sslSession) {
		        return hostname.equals("localhost");
		    }
		};

		KeyStore ks = KeyStore.getInstance("JKS");
		File file = new File("put the file path to your own keystore");
		ks.load(new FileInputStream(file), "keystore password".toCharArray());

		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, "kestore password here".toCharArray());

		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ks);

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

		OkHttpClient okHttpClient = new OkHttpClient.Builder()
		        .hostnameVerifier(hostnameVerifier)
		        .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) tmf.getTrustManagers()[0])
		        .readTimeout(1, TimeUnit.MINUTES) 
		        .build();
		Options options = new Options();
		options.callFactory = okHttpClient;
		options.webSocketFactory = okHttpClient;
		options.transports = new String[]{WebSocket.NAME};
		URI uri = URI.create("http://localhost:8081");
		mSocket = IO.socket(uri, options);
		mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {  
			@Override
		    public void call(Object... args) { 
				Platform.runLater(new Runnable() {
				    @Override
				    public void run() {
				    	serverStatus.setText("Connected");
						serverStatus.setTextFill(Color.GREEN);
				    }
				});
				
				}});
		mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {  
			@Override
		    public void call(Object... args) { 
				Platform.runLater(new Runnable() {
				    @Override
				    public void run() {
				    	serverStatus.setText("Disconnected");
						serverStatus.setTextFill(Color.RED);		 
				    }
				});
				       
				}});
		mSocket.connect();
		if(mSocket.connected()) {
			serverStatus.setText("Connected");
			serverStatus.setTextFill(Color.GREEN);
		}else {
			serverStatus.setText("Disconnected");
			serverStatus.setTextFill(Color.RED);
		}
		Label tit = new Label("Welcome to Orgamis Chat Application");
		Label descrip = new Label("Set Username with atleast 4-12 characters");
		TextArea loginT = new TextArea();
		Button login = new Button("Click To Log In");
		VBox loginHold = new VBox(5);
		loginHold.getChildren().addAll(descrip,loginT,login);
		loginHold.setAlignment(Pos.CENTER);
		HBox holdBoth = new HBox();
		holdBoth.getChildren().addAll(loginHold);
		holdBoth.setAlignment(Pos.CENTER);
		VBox afix = new VBox(6);
		afix.getChildren().addAll(tit, holdBoth);
		afix.setAlignment(Pos.CENTER);
		primaryStage.setTitle("origamiChat");
		afix.setPadding(new Insets(0,0,3,0));
		// source for image: https://www.pixiv.net/en/artworks/87112224
		URL k = RealApp.class.getClassLoader().getResource("cute.jpg");
		Image image = new Image(k.toString());
		primaryStage.getIcons().add(image);
		StackPane layout =  new StackPane();
		layout.getChildren().add(afix);
		Scene scene = new Scene(layout, 700, 550);
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e->{
			stop();
		});

		login.setOnAction(e->{
			if(userN == null && loginT.getText().length() > 3 && loginT.getText().length() < 13) {
			userN = loginT.getText();
			VBox holdLeft = new VBox(10);
			HBox seperate = new HBox(20);
			URL getCss = RealApp.class.getClassLoader().getResource("help.css");
			Button refresher = new Button("Refresh Text Board");
			Label showName = new Label("Hey " + userN + " Character Limit is 75");
			Label showNum = new Label("Characters: 0");
			sendTo =  new Button("Click To Send Message");
			makeChat = new TextArea();
			showChat = new TextArea();
			showChat.setEditable(false);
			showChat.getStylesheets().add(getCss.toString());
		    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
			showChat.setMaxHeight((screenBounds.getHeight()*8)/10);
			seperate.setAlignment(Pos.CENTER);
			showName.setPadding(new Insets(10,0,0,0));
			holdLeft.getChildren().addAll(showName, serverStatus, makeChat, showNum, sendTo, refresher);
			seperate.getChildren().addAll(holdLeft, showChat);
			makeChat.setWrapText(true);
			showChat.setWrapText(true);
			primaryStage.setScene(new Scene(seperate));
			primaryStage.setMaximized(true);
			sendTo.setOnAction(j->{
				sendTo.setDisable(true);
				if(makeChat.getText().length()<76 && mSocket.connected() && makeChat.getText().length() > 1) {
					HashMap<String, String> obj = new HashMap<String, String>();
					obj.put("user", userN);
					obj.put("mess", makeChat.getText());
					mSocket.emit("message", obj);
					makeChat.setText("");
				}else {
					showChat.setText( showChat.getText() + userN + ": " + "Not Connected or conetent doesnt meet 2-75 character limit!" + " \n" );
					makeChat.setText("");
				}
				sendTo.setDisable(false);
			});
			refresher.setOnAction(g->{
				showChat.setText("");
			});
			makeChat.textProperty().addListener(o->{
				int getNum = makeChat.getText().length();
				showNum.setText("Characters: " + getNum);
			});
			mSocket.on("message", new Emitter.Listener() {  
				@Override
			    public void call(Object... args) { 
					String getter =args[0].toString();
					JsonObject mesag = (JsonObject) JsonParser.parseString(getter);
					String getU = mesag.get("user").toString();
					String getM = mesag.get("mess").toString();
					showChat.setText(showChat.getText() + getU + ": " + getM + " \n" );
					}});
			}
		});
		
	}

	 @Override
	public void stop() {
		 	mSocket.close();
	        Platform.exit();
	    }
}

}
