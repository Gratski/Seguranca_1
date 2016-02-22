package common;

import java.io.Serializable;

public class User implements Serializable{

	private String name;
	private String password;
	
	public User(String name) {
		this.name = name;
	}

	public void setPassword(String pwd) {
		this.password = pwd;
	}
	
	public String getName(){
		return this.name;
	}

}
