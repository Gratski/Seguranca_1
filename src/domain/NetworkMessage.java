package domain;

import java.io.Serializable;

public class NetworkMessage implements Serializable {

	private String type;
	private String specification;
	private User user;
	
	public NetworkMessage(){
		this.user = null;
	}

	public void setSpecification(String spec){
		this.specification = spec;
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
	
	public User getUser(){
		return this.user;
	}
	
	public String getSpecs(){
		return this.specification;
	}

}
