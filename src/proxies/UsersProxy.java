package proxies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import common.User;
import enums.Filenames;

public class UsersProxy implements Proxy{

	private static UsersProxy instance = null;
	private File file;
	private FileWriter fw;
	private BufferedWriter bw;
	private Map<String, User> users;
	
	private UsersProxy() throws IOException{
		this.users = new HashMap<>();
		this.init();
	} 
	
	public static UsersProxy getInstance() throws IOException{
		if(instance == null)
			instance = new UsersProxy();
		return instance;
	}
	
	/**
	 * Popula o map de users
	 * Abre streams de escrita em USERS
	 * @throws IOException
	 */
	private void init() throws IOException{
		
		File file = new File(Filenames.USERS.toString());
		if( !file.exists() ){
			file.createNewFile();
		}else{
			System.out.println("Users file already exists");
		}
		
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		
		//reading users file
		String line = null;
		while((line = br.readLine()) != null){
			String[] arr = line.split(":");
			if(arr.length < 2)
				continue;
			User u = new User(arr[0], arr[1]);
			
			//add to users
			this.users.put(u.getName(), u);
		}
		
		//close readers
		fr.close();
		br.close();
		
		//open writing streams
		this.file = new File(Filenames.USERS.toString());
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
	public boolean autheticate(User user){
		User register = this.users.get(user.getName());
		return register.getPassword().equals(user.getPassword());
	}
	
	/**
	 * Insere um novo utilizador
	 * @param user
	 * 		User a ser inserido
	 * @require
	 * 		!exists(user)
	 */
	public boolean insert(User user){
		StringBuilder sb = new StringBuilder();
		sb.append(user.getName());
		sb.append(":");
		sb.append(user.getPassword());
		try{
			
			//escreve em ficheiro de users
			this.bw.write(sb.toString());
			this.bw.flush();
			this.users.put(user.getName(), user);
			
		}catch(Exception e){
			return false;
		}
		
		return this.users.containsKey(user.getName());
	}
	
	@Override
	public void destroy() throws IOException{
		this.fw.close();
		this.bw.close();
	}
	
}
