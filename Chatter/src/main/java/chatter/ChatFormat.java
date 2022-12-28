package chatter;


public class ChatFormat {
	private String user;
	private String mess;

	public ChatFormat() {}

	public String getUser(){
		return this.user;
	}
	public String getMess() {
		return this.mess;
	}
	public void setUser(String userName) {
		this.user=userName;
	}
	public void setMess(String mess) {
		this.mess=mess;
	}

}
