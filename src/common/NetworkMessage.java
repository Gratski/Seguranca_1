package common;

import java.io.Serializable;

public class NetworkMessage implements Serializable{

	private String type;
	private User user;
	
	public NetworkMessage(){
		this.user = null;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}

}
