package security;

import java.io.Serializable;

/**
 * Class that represents a key that has been ciphered and the name of the
 * user whoose private key must be used to decipher it
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class CipheredKey implements Serializable{

	/**
	 * The name of the user that must know how to decipher
	 * the key
	 */
	private String to;

	/**
	 * Byte array that represents the ciphered key
	 */
	private byte[] cipheredKey;

	/**
	 * Constructor
	 *
	 * @param to The name of the user
	 * @param key The key itself, ciphered
     */
	public CipheredKey(String to, byte[] key){
		this.to = to;
		this.cipheredKey = key;
	}

	/**
	 * Getter for the ciphered key
	 *
	 * @return Byte array that represents the key
     */
	public byte[] getKey(){
		return this.cipheredKey;
	}

	/**
	 * Getter for the name of the user who will decipher the key
	 *
	 * @return The String which is the name of the user
     */
	public String getTo(){
		return this.to;
	}
}
