package security;

import java.io.Serializable;

public class CipheredMessage implements Serializable {

	private String from;
	private byte[] msg;
	
	public CipheredMessage(String from, byte[] msg){
		this.from = from;
		this.msg = msg;
	}
	
	public String getFrom(){
		return this.from;
	}
	
	public byte[] getMsg(){
		return this.msg;
	}
	
}
