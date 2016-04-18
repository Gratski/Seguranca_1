package security;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PBEService {

	/**
	 * Gera uma chave secreta simetrica com base numa string
	 * @param str, string a considerar
	 * @return chave simetrica gerada
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static SecretKey getKeyByString(String str) throws NoSuchAlgorithmException, InvalidKeySpecException {
		PBEKeySpec param = new PBEKeySpec(str.toCharArray());
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		return skf.generateSecret(param);
	}
	
}
