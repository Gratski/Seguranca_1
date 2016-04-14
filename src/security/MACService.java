package security;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

import helpers.FilesHandler;
import proxies.Proxy;

public class MACService {

	private static final String MAC_ALGORITHM = "HmacSHA256";
	
	/**
	 * Obtem uma instancia de mac
	 * @return Instancia de Mac
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException 
	 */
	public static Mac getMacInstance(SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException{
		Mac mac = Mac.getInstance(MAC_ALGORITHM); 
		mac.init(key);
		return mac;
	}
	
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
		
		Mac mac = Mac.getInstance("HmacSHA256");
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
	public static byte[] readMACFile(File f) throws IOException{
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

	
	/**
	 * Valida o MAC de um dado ficheiro
	 * @param baseURL, path base de ficheiro
	 * @param key, chave simetrica a utilizar
	 * @return true se valido, false caso contrario
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static boolean validateMAC(String baseURL, SecretKey key) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		
		byte[] genMAC = generateFileMac(new File(baseURL), key);
		byte[] curMAC = readMACFile(new File(baseURL+Proxy.getMacFileExtension()));
		return MessageDigest.isEqual(curMAC, genMAC);
	}

	/**
	 * Actualiza o MAC de um dado ficheiro
	 * @param baseURL, path a conseiderar
	 * @param key, chave simetrica a utilizar
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void updateMAC(String baseURL, SecretKey key) throws InvalidKeyException, NoSuchAlgorithmException, IOException
	{
		//FILES
		File f = new File(baseURL);
		File fmac = new File(baseURL+""+Proxy.getMacFileExtension());
		
		//gera mac de users file
		byte[] newMac = MACService.generateFileMac(f, key);
		String macHexStr = SecUtils.getHex(newMac);
		BufferedWriter bw = new FilesHandler().getWriter(fmac);
		bw.write(macHexStr);
		bw.close();
	}
	
}
