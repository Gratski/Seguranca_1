package security;

import java.io.Serializable;

public class CipheredKey implements Serializable{

	private String to;
	private byte[] cipheredKey;
	
	public CipheredKey(String to, byte[] key){
		this.to = to;
		this.cipheredKey = key;
	}
	
	public byte[] getKey(){
		return this.cipheredKey;
	}
	
	public String getTo(){
		return this.to;
	}
}
