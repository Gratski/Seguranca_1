package security;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

import helpers.FilesHandler;

public class MACService {

	/**
	 * Gera um mac do ficheiro
	 * @param f, ficheiro a considerar
	 * @param k, chave secreta utilizada no hash
	 * @return hash de ficheiro
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public static byte[] generateFileMac(File f, SecretKey k) throws IOException, NoSuchAlgorithmException, InvalidKeyException{
		
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(k);
		
		BufferedReader br = new FilesHandler().getReader(f);
		String line = null;
		while((line=br.readLine())!=null){
			mac.update(line.getBytes());
		}
		
		return mac.doFinal();
	}
	
	/**
	 * Obtem o hash de um ficheiro .mac
	 * @param f, file a considerar
	 * @return array de bytes com hash
	 * @throws IOException
	 */
	public static byte[] readHashFromFile(File f) throws IOException{
		BufferedReader br = new FilesHandler().getReader(f);
		return SecUtils.hexStringToByteArray(br.readLine());
	}
	
	/**
	 * Verifica se o mac de um ficheiro eh igual a outro dado
	 * @param f, ficheiro a considerar
	 * @param key, chave secreta a utilizar no mac
	 * @param hash, mac a comparar
	 * @return true se iguais, false caso contrario
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static boolean validateFileMac(File f, SecretKey key, byte[]hash) throws InvalidKeyException, NoSuchAlgorithmException, IOException{
		byte[] calcHash = generateFileMac(f, key);
		return MessageDigest.isEqual(calcHash, hash);
	}
	
}
