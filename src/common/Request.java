package common;

import java.io.Serializable;

public class Request implements Serializable{

	private String ip;
	private int port;
	private String type;
	private User user;
	private Message message;
	
	public Request(){
		this.message = null;
		this.user = null;
	}
	
	public void setIP(String ip) {
		this.ip = ip;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setMessage(String to, String body){
		this.message = new Message(this.user.getName(), to, body);
	}

}
