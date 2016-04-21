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

	private static UsersProxy instance = null;
	private File file;
	private FileWriter fw;
	private BufferedWriter bw;
	private Map<String, User> users;

	/**
	 * Constructor para usersProxy
	 * Cria um Map e popula-o através do ficheiro de users (se existir)
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
	 * Função para obter a instância de UsersProxy
	 *
	 * @return usersProxy para ser usado para persistir Users
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
	 * Popula o map de users
	 * Abre streams de escrita em USERS
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
		
		//reading users file
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] arr = line.split(":");
			if (arr.length < 3)
				continue;
			byte[] salt = SecUtils.getStringHex(arr[1]);
			byte[] password = SecUtils.getStringHex(arr[2]);
			User u = new User(arr[0], password, salt );
			
			//add to users
			this.users.put(u.getName(), u);
		}
		
		//close readers
		fr.close();
		br.close();
		
		//open writing streams
		this.file = new File(USERS_INDEX);
		this.fw = new FileWriter(this.file, true);
		this.bw = new BufferedWriter(this.fw);
	}
	
	/**
	 * Verifica se um user ja existe
	 * @param user
	 * 		User a ser considerado na comparacao
	 * @return
	 * 		true se ja existe, false caso contrario
	 */
	public boolean exists(User user){
		return this.users.containsKey(user.getName());
	}
	
	/**
	 * Autentica determinado utilizador
	 * @param user
	 * 		User a ser autenticado
	 * @return
	 * 		true se valid, false caso contrario
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @require
	 * 		exists(user)
	 */
	public boolean autheticate(User user) throws InvalidKeyException, NoSuchAlgorithmException {
		User register = this.users.get(user.getName());
		byte[] salt = register.getSalt();
		
		MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
		md.update(user.getPassword());
		md.update(":".getBytes());
		md.update(salt);
		byte[] genHash = md.digest();
		
		return MessageDigest.isEqual(register.getPassword(), genHash);
	}
	
	/**
	 * Insere um novo utilizador no ficheiro e em memoria
	 * @param user
	 * 		User a ser inserido
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws IOException 
	 * @require
	 * 		!exists(user) && user != null
	 */
	public String insert(User user) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
		if (this.users.containsKey(user.getName()))
			return null;

		
		user.setSalt(SecUtils.generateRandomSalt(8));
		
		//gera hash de password
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(user.getPassword());
		md.update(":".getBytes());
		md.update(user.getSalt());
		
		user.setPassword(md.digest());
		
		//converte para bytes
		String passStr = SecUtils.getHexString(user.getPassword());
		String saltStr = SecUtils.getHexString(user.getSalt());
		
		System.out.println("Registo de novo User: " + user.toString());
		StringBuilder sb = new StringBuilder();
		sb.append(user.getName());
		sb.append(":");
		sb.append(saltStr);
		sb.append(":");
		sb.append(passStr);
		sb.append("\n");
		try {
			//escreve em ficheiro de users
			this.bw.write(sb.toString());
			this.bw.flush();
			this.users.put(user.getName(), user);
		} catch(Exception e) {
			System.out.println("Erro ao escrever no ficheiro USERS");
			System.out.println(e.fillInStackTrace());
		}
		return this.users.containsKey(user.getName()) ? passStr : null;
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
	 * Method used for tests, emptys the Map in memory and
	 * reloads from the Users file
	 *
	 * @throws IOException
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyStoreException 
     */
	public void reload() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		this.users = new HashMap<>();
		init();
	}
	
	/**
	 * Valid MAC de users file
	 * @param key, chave simetrica a ser utilizada
	 * @return true se nao comprometido, false caso contrario
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 */
	private boolean validMac(SecretKey key) throws IOException, InvalidKeyException, NoSuchAlgorithmException{
		return MACService.validateMAC(Proxy.getUsersIndex(), key);
	}
	
	/**
	 * Actualiza MAC de users file
	 * @param key, chave simetrica a ser utilizada
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
 	private void updateMac(SecretKey key) throws InvalidKeyException, NoSuchAlgorithmException, IOException{
		MACService.updateMAC(Proxy.getUsersIndex(), key);
	}
}