package proxies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import domain.User;

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
     */
	private UsersProxy() throws IOException {
		this.users = new HashMap<>();
		this.init();
	}

	/**
	 * Função para obter a instância de UsersProxy
	 *
	 * @return usersProxy para ser usado para persistir Users
	 * @throws IOException
     */
	public static UsersProxy getInstance() throws IOException {
		if (instance == null)
			instance = new UsersProxy();
		return instance;
	}
	
	/**
	 * Popula o map de users
	 * Abre streams de escrita em USERS
	 *
	 * @throws IOException
	 */
	private void init() throws IOException {
		File file = new File(USERS_INDEX);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		
		//reading users file
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] arr = line.split(":");
			if (arr.length < 2)
				continue;
			User u = new User(arr[0], arr[1]);
			
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
	 * @require
	 * 		exists(user)
	 */
	public boolean autheticate(User user) {
		User register = this.users.get(user.getName());
		return register.getPassword().equals(user.getPassword());
	}
	
	/**
	 * Insere um novo utilizador no ficheiro e em memoria
	 * @param user
	 * 		User a ser inserido
	 * @require
	 * 		!exists(user) && user != null
	 */
	public boolean insert(User user) {
		if (this.users.containsKey(user.getName()))
			return false;

		System.out.println("Registo de novo User: " + user.toString());
		StringBuilder sb = new StringBuilder();
		sb.append(user.getName());
		sb.append(":");
		sb.append(user.getPassword());
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
		return this.users.containsKey(user.getName());
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
     */
	public void reload() throws IOException {
		this.users = new HashMap<>();
		init();
	}
}