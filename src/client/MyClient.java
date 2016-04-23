package client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

import security.SecUtils;

public class MyClient {

	public static void main(String[]args) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		
		String data = "This a cipher";
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		Key k = kg.generateKey();
		
		//Cipher
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, k);
		byte[] original = data.getBytes();
		byte[]ciphered = c.doFinal(original);
		String cipheredString = SecUtils.getHexString(ciphered);
		
		//Decipher
		c.init(Cipher.DECRYPT_MODE, k);
		byte[] d = SecUtils.getStringHex(cipheredString);
		byte[] decripted = c.doFinal(d);
		System.out.println(new String(decripted));
		
	}
	
}
