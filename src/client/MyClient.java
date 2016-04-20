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
		
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(new FileInputStream("joao.keyStore"), "yoyoyo".toCharArray());
		Certificate cert = ks.getCertificate("joao");
		
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		kg.init(128);
		Key key = kg.generateKey();
		
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, cert.getPublicKey());
		byte[] cKey = c.doFinal(key.getEncoded());
		System.out.println(SecUtils.getHexString(cKey));
	}
	
}
