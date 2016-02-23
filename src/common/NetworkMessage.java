package common;

import java.io.Serializable;

public class NetworkMessage implements Serializable{

	private String ip;
	private int port;
	private String type;
	private User user;
	
	
	public NetworkMessage(){
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

}
