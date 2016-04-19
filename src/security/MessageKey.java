package security;

import java.io.Serializable;
import java.security.Key;
import java.security.cert.Certificate;

public class MessageKey implements Serializable{

	private String name;
	private byte[] key;
	
	public MessageKey(String name, byte[] key){
		this.key = key;
		this.name = name;
	}
	
	/**
	 * 
	 * @return
	 */
	public byte[] getKey(){
		return this.key;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getName(){
		return this.name;
	}
	
}
