package security;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignedObject;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.security.cert.Certificate;

public class PGP {

	private SignedObject signedObject;
	private Key sharedKey;
	private PrivateKey privateKey;
	private byte[] wrappedKey;
	
	public PGP(PrivateKey key ) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException{
		this.privateKey = key;
		this.generateKey();
		this.wrapKey();
	}
	
	private void generateKey() throws NoSuchAlgorithmException{
		KeyGenerator kg = KeyGenerator.getInstance("DESede");
		this.sharedKey = kg.generateKey();
	}
	
	private void wrapKey() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException{
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		c.init(Cipher.WRAP_MODE, this.privateKey);
		this.wrappedKey = c.wrap(this.sharedKey);
	}
	
	public byte[] getWrappedKey(){
		return this.wrappedKey;
	}
	
}
