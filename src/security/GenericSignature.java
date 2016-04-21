package security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;

public class GenericSignature implements Serializable{

	private final static String ALGORITHM = "SHA-256";
	
	private final byte[] signature;
	private final PublicKey publicKey;
	
	public GenericSignature(byte[] hash){
		this.signature = hash;
		this.publicKey = null;
	}
	public GenericSignature(byte[] hash, PublicKey key){
		this.signature = hash;
		this.publicKey = key;
	}
	
	public byte[] getSignature(){
		return this.signature;
	}
	
	public PublicKey getPublicKey(){
		return this.publicKey;
	}
	
	public static GenericSignature createGenericMessageSignature(PrivateKey pkey, byte[] content) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, SignatureException {

		//gera sintese da mensagem
		MessageDigest md = getMessageDigest();
		byte[] hash = md.digest(content);
		
		//assina cifra da mensagem
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(pkey);
		signature.update(hash);
		return new GenericSignature(signature.sign());
	}
	
	public static GenericSignature createGenericFileSignature(PrivateKey privateKey, File file) throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		//gerar sintese de ficheiro
		MessageDigest md = getMessageDigest();
		byte[] hash = new byte[16];
		int sent = 0;
		FileInputStream fis = new FileInputStream(file);
		while ((sent = fis.read(hash)) > 0) {
			md.update(hash, 0, sent);
		}
		hash = md.digest();
		
		//assinar sintese de ficheiro
		Cipher c = Cipher.getInstance(ALGORITHM);
		c.init(Cipher.ENCRYPT_MODE, privateKey);
		c.update(hash);
		return new GenericSignature(c.doFinal());
	}
	
	private static MessageDigest getMessageDigest() throws NoSuchAlgorithmException{
		return MessageDigest.getInstance(ALGORITHM);
	}
}
