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

/**
 * Class the represents a Service to be used in the application with functions and utilities
 * regarding MACs (Message Authentication Code)
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class MACService {

	/**
	 * Hashing algorithm used in the MACs creation
	 */
	private static final String MAC_ALGORITHM = "HmacSHA256";

	/**
	 * Gera um MAC do ficheiro
	 *
	 * @param f, ficheiro a considerar
	 * @param k, chave secreta utilizada no hash
	 * @return hash de ficheiro
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public static byte[] generateFileMac(File f, SecretKey k) throws IOException, NoSuchAlgorithmException, InvalidKeyException{
		Mac mac = Mac.getInstance(MAC_ALGORITHM);
		mac.init(k);
		
		BufferedReader br = new FilesHandler().getReader(f);
		String line = null;
		while ((line = br.readLine()) != null) {
			mac.update(line.getBytes());
		}
		return mac.doFinal();
	}
	
	/**
	 * Obtem o hash de um ficheiro .mac
	 *
	 * @param f, file a considerar
	 * @return array de bytes com hash
	 * @throws IOException
	 */
	public static byte[] readMACFile(File f) throws IOException{
		BufferedReader br = new FilesHandler().getReader(f);
		return SecUtils.getStringHex(br.readLine());
	}

	/**
	 * Valida o MAC de um dado ficheiro
	 *
	 * @param baseURL, path base de ficheiro
	 * @param key, chave simetrica a utilizar
	 * @return true se valido, false caso contrario
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static boolean validateMAC(String baseURL, SecretKey key) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		
		byte[] genMAC = generateFileMac(new File(baseURL), key);
		byte[] curMAC = readMACFile(new File(baseURL + Proxy.getMacFileExtension()));
		return MessageDigest.isEqual(curMAC, genMAC);
	}

	/**
	 * Actualiza o MAC de um dado ficheiro
	 *
	 * @param basePath, path a conseiderar
	 * @param key, chave simetrica a utilizar
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void updateMAC(String basePath, SecretKey key) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		//FILES
		File f = new File(basePath);
		File fmac = new File(basePath + Proxy.getMacFileExtension());
		
		//gera mac de users file
		byte[] newMac = MACService.generateFileMac(f, key);
		String macHexStr = SecUtils.getHexString(newMac);
		BufferedWriter bw = new FilesHandler().getWriter(fmac);
		bw.write(macHexStr);
		bw.close();
	}

	/**
	 * Generates MAC files for the users and groups index files
	 *
	 * @param key Secret key used to produce the MAC
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void generateAllMacFiles(SecretKey key) throws InvalidKeyException, NoSuchAlgorithmException, IOException {

		// FILES
		File f = new File(Proxy.getUsersIndex());
		File fmac = new File(Proxy.getUsersIndex() + Proxy.getMacFileExtension());

		// gera mac de users file
		byte[] mac = MACService.generateFileMac(f, key);
		String macHexStr = SecUtils.getHexString(mac);
		BufferedWriter bw = new FilesHandler().getWriter(fmac);
		bw.write(macHexStr);
		bw.close();

		// gera mac de groups file
		f = new File(Proxy.getGroupsIndex());
		fmac = new File(Proxy.getGroupsIndex() + Proxy.getMacFileExtension());
		mac = MACService.generateFileMac(f, key);
		macHexStr = SecUtils.getHexString(mac);
		bw = new FilesHandler().getWriter(fmac);
		bw.write(macHexStr);
		bw.close();
	}
}