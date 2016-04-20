package security;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class KeyWrapper {

	private static final String ALG = "DES";
	private static final String CIPHER_ALG = "PBEWithMD5AndDES";
	
	private Key key;
	private byte[]wrappedKey;
	
	public KeyWrapper(Key key){
		this.key = key;
	}
	public KeyWrapper(byte[] key){
		this.wrappedKey = key;
	}
	
	public byte[] getWrappedKey(){
		return this.wrappedKey;
	}
	public Key getKey(){
		return this.key;
	}
	
	public void wrap(Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException{
		Cipher c = Cipher.getInstance(CIPHER_ALG);
		c.init(Cipher.WRAP_MODE, this.key);
		this.wrappedKey = c.wrap(key);
	}
	
	public void unwrap(Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException{
		Cipher c = Cipher.getInstance(CIPHER_ALG);
		c.init(Cipher.UNWRAP_MODE, key);
		this.key = c.unwrap(this.wrappedKey, ALG, Cipher.PRIVATE_KEY);
	}
	
}
