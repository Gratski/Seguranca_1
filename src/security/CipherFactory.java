package security;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class CipherFactory {

	public static Cipher getStandardCipher() throws NoSuchAlgorithmException, NoSuchPaddingException{
		return Cipher.getInstance("AES");
	}
	
}
