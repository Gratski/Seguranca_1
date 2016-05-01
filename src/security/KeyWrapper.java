package security;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Class the wraps and unwraps keys with a specified algorithm
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class KeyWrapper {

	/**
	 * Algorithm to be used
	 */
	private static final String ALG = "RSA";

	/**
	 * Key to be used/created in the wrapping/unwrapping process
	 */
	private Key key;

	/**
	 * Key to be used/created in the wrapping/unwrapping process
	 */
	private byte[] wrappedKey;

	/**
	 * Constructor
	 *
	 * @param key Key to be used for wrapping/unwrapping
     */
	public KeyWrapper(Key key){
		this.key = key;
	}

	/**
	 * Constructor
	 *
	 * @param key Key in byte array format to be used for wrapping/unwrapping
     */
	public KeyWrapper(byte[] key){
		this.wrappedKey = key;
	}

	/**
	 * Getter for the wrappedKey
	 *
	 * @return wrapped key in Byte array format
     */
	public byte[] getWrappedKey(){
		return this.wrappedKey;
	}

	/**
	 * Getter for the Key
	 *
	 * @return Key to be used
     */
	public Key getKey(){
		return this.key;
	}

	/**
	 * Wraps a Key key into a byte array format key
	 *
	 * @param key
	 * 		Key to be used in the wrapping process
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
     */
	public void wrap(Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException{
		Cipher c = Cipher.getInstance(ALG);
		c.init(Cipher.WRAP_MODE, this.key);
		this.wrappedKey = c.wrap(key);
	}

	/**
	 * Unwraps a key
	 *
	 * @param key
	 * 		Private key to be used in the Unwrapping process
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
     */
	public void unwrap(PrivateKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException{
		Cipher c = Cipher.getInstance(ALG);
		c.init(Cipher.UNWRAP_MODE, key);
		
		this.key = c.unwrap(this.wrappedKey, "AES", Cipher.SECRET_KEY);
	}
}