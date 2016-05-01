package proxies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import domain.User;
import security.MACService;
import security.SecUtils;

/**
 * Esta classe é responsável por persistir os Users num ficheiro
 * e funciona como base de dados em memória com um Map
 * Class é Singleton
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class UsersProxy extends Proxy {

	/**
	 * salt size constant
	 */
	private static final int SALT_SIZE = 6;
	
	/**
	 * users proxy singleton
	 */
	private static UsersProxy instance = null;
	
	/**
	 * users file
	 */
	private File file;
	
	/**
	 * file writer to be used
	 */
	private FileWriter fw;
	
	/**
	 * buffered writer to be used
	 */
	private BufferedWriter bw;
	
	/**
	 * users map to have O(1) when searching for a user
	 * when in central memory
	 */
	private Map<String, User> users;
	

	/**
	 * Constructor
	 * Initializes the users map based on users file
	 *
	 * @throws IOException
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyStoreException 
     */
	private UsersProxy() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		this.users = new HashMap<>();
		this.init();
	}

	/**
	 * Gets the instance of the users proxy
	 *
	 * @return usersProxy users proxy instance
	 * 
	 * @throws IOException
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyStoreException 
     */
	public static UsersProxy getInstance() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		if (instance == null)
			instance = new UsersProxy();
		return instance;
	}
	
	/**
	 * Populate users map
	 * Open needed streams
	 *
	 * @throws IOException
	 * @throws KeyStoreException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 */
	private void init() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		File file = new File(USERS_INDEX);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		
		// reading users file
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] arr = line.split(":");
			if (arr.length < 3)
				continue;
			int salt = new Integer(arr[1]);
			byte[] password = SecUtils.getStringHex(arr[2]);
			User u = new User(arr[0], password, salt );
			
			// add to users
			this.users.put(u.getName(), u);
		}
		
		// close readers
		fr.close();
		br.close();
		
		// open writing streams
		this.file = new File(USERS_INDEX);
		this.fw = new FileWriter(this.file, true);
		this.bw = new BufferedWriter(this.fw);
	}
	
	/**
	 * Checks if the given users already exists
	 * @param user
	 * 		User to be considered when checking
	 * @return
	 * 		true if exists, false otherwise
	 */
	public boolean exists(User user){
		return this.users.containsKey(user.getName());
	}
	
	/**
	 * Authenticates the given user
	 * @param user
	 * 		User to be authenticated
	 * @return
	 * 		true if authorized, false otherwise
	 * 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @require
	 * 		exists(user)
	 */
	public boolean autheticate(User user) throws InvalidKeyException, NoSuchAlgorithmException {
		User register = this.users.get(user.getName());
		int salt = register.getSalt();
		
		MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
		md.update(user.getPassword());
		md.update(":".getBytes());
		md.update(intToByteArray(salt));
		byte[] genHash = md.digest();
		
		return MessageDigest.isEqual(register.getPassword(), genHash);
	}
	
	/**
	 * Inserts a new user into memory and file
	 * 
	 * @param user
	 * 		User to be inserted
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws IOException 
	 * @require
	 * 		!exists(user) && user != null
	 */
	public boolean insert(User user, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
		if (this.users.containsKey(user.getName()))
			return false;
		
		if(!MACService.validateMAC(Proxy.getUsersIndex(), key))
			return false;
		
		user.setSalt(SecUtils.generateRandomSalt(SALT_SIZE));
		
		// creates a password hash
		MessageDigest md = MessageDigest.getInstance("SHA-256");				
		md.update(user.getPassword());
		md.update(":".getBytes());
		md.update(intToByteArray(user.getSalt()));
		user.setPassword(md.digest());
		
		// converts it to an hex represented string
		String passStr = SecUtils.getHexString(user.getPassword());
		
		System.out.println("Registo de novo User: " + user.toString());
		StringBuilder sb = new StringBuilder();
		sb.append(user.getName());
		sb.append(":");
		sb.append(user.getSalt());
		sb.append(":");
		sb.append(passStr);
		sb.append("\n");
		try {
			// writes new user into users file
			this.bw.write(sb.toString());
			this.bw.flush();
			this.users.put(user.getName(), user);
		} catch(Exception e) {
			System.out.println("Erro ao escrever no ficheiro USERS");
			System.out.println(e.fillInStackTrace());
		}
		
		// update mac
		if(this.users.containsKey(user.getName()))
			MACService.updateMAC(Proxy.getUsersIndex(), key);
		
		return MACService.validateMAC(Proxy.getUsersIndex(), key);
	}
	
	/**
	 * Finds a user by username
	 * @param name
	 * 		name of the user to be considered
	 * @return
	 * 		User if exists, null if it doesnt
	 */
	public User find(String name){
		return this.users.containsKey(name) ? this.users.get(name) : null;
	}
	
	/**
	 * Converts an integer into a byte array
	 * 
	 * @param value, integer to be converted
	 * @return integer byte array representation
	 */
	public static final byte[] intToByteArray(int value) {
	    return new byte[] {
	            (byte)(value >>> 24),
	            (byte)(value >>> 16),
	            (byte)(value >>> 8),
	            (byte)value};
	}
}