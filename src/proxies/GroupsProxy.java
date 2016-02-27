package proxies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import common.Group;
import common.User;
import enums.Filenames;

public class GroupsProxy implements Proxy{

	private static GroupsProxy instance = null;
	private File file;
	private FileWriter fw;
	private BufferedWriter bw;
	private Map<String, Group> groups;
	
	private GroupsProxy() throws IOException{
		this.groups = new HashMap<>();
		this.init();
	}
	
	public static GroupsProxy getInstance() throws IOException{
		if(instance == null)
			instance = new GroupsProxy();
		return instance;
	}

	public void init() throws IOException{
		
		BufferedReader br = createReader();
		
		//reading users file
		String line = null;
		while((line = br.readLine()) != null){
			
			System.out.println("INSIDE WHILE");
			
			//read group
			String[] lineSplit = line.split(" ");
			String dateStr = lineSplit[0];
			String owner = lineSplit[1];
			String name = lineSplit[2];
			Group group = new Group(name, new User(owner));
			
			//adiciona membros ao group
			for(int i = 3; i < lineSplit.length; i++)
				group.addMember(new User(lineSplit[i]));
			
			//adiciona aos groups
			this.groups.put(name, group);
			
		}
		
		br.close();
	}
	
	/**
	 * Adiciona um novo group a groups
	 * @param groupName
	 * 		Nome do novo group
	 * @param owner
	 * 		Owner do novo group
	 */
	public void create(String groupName, User owner) throws IOException{
		
		this.groups.put(groupName, new Group(groupName, owner));
		
		//persistencia em ficheiro
		BufferedWriter bw = createWriter();
		bw.write("data " + owner.getName() + " " + groupName);
		bw.flush();
		bw.close();
	}
	
	/**
	 * Verifica se um user eh owner de um group
	 * @param groupName
	 * 		Nome do grupo em questao
	 * @param username
	 * 		Username a ser considerado
	 * @return
	 * 		true se eh owner, false caso contrario
	 * @require
	 * 		exists(groupName)
	 */
	public boolean isOwner(String groupName, String username){
		return this.groups.get(groupName).getOwner().equals(username);
	}
	
	/**
	 * Verifica se um determinado group existe
	 * @param name
	 * 		Nome do group a ser considerado
	 * @return
	 * 		true se sim, false caso contrario
	 */
	public boolean exists(String name){
		return this.groups.containsKey(name);
	}
	
	/**
	 * Adiciona um novo membro ao group
	 * @param groupName
	 * 		Nome do group ao qual o novo membro vai ser adicionado
	 * @param member
	 * 		novo membro a ser adicionado ao group
	 * @return
	 * 		true se adicionou, false caso contrario
	 * @require
	 * 		exists(groupName)
	 */
	public boolean addMember(String groupName, User member) throws IOException{
		
		if( !this.groups.get(groupName).addMember(member) )
			return false;
		
		//persistencia em ficheiro
		BufferedReader reader = createReader();
		StringBuilder sb = new StringBuilder();
		String line = null;
		
		while( (line = reader.readLine()) != null ){
			
			//split
			String[] lineSplit = line.split(" ");
			if( lineSplit.length < 3 )
				continue;
			
			sb.append(lineSplit[0]);
			sb.append(" "+lineSplit[1]);
			sb.append(" "+lineSplit[2]);
			
			//verifica se tem membros already
			String[]members = new String[0];
			if( lineSplit.length > 3 ){
				members = lineSplit[3].split(",");
			}
			
			//adiciona cada membro ao SB
			for(int i = 0; i < members.length; i++)
			{
				if(i== 0)
					sb.append(" ");
				
				if(i == 0)
					sb.append(members[i]);
				else
					sb.append(","+members[i]);
			}
			
			//se eh o group pretendido
			if( lineSplit[2].equals(groupName))
				//se ja tem membros
				if( members.length >= 1 )
					sb.append(","+member.getName());
				else
					sb.append(" " + member.getName());
		}
		
		//reescreve ficheiro
		BufferedWriter writer = createWriter();
		writer.write(sb.toString());
		writer.close();
		
		//reabre canais de escrita da classe
		this.file = new File("DATABASE/GROUPS/"+Filenames.GROUPS.toString());
		this.fw = new FileWriter(file, true);
		this.bw = new BufferedWriter(this.fw);
		
		return true; 
	}
	
	@Override
	public void destroy() throws IOException {
		
	}
	
	
	
	
	//////AUX
	private BufferedReader createReader() throws FileNotFoundException{
		File file = new File("DATABASE/GROUPS/"+Filenames.GROUPS.toString());
		FileReader reader = new FileReader(file);
		return new BufferedReader(reader);
	}
	private BufferedWriter createWriter() throws IOException{
		File file = new File("DATABASE/GROUPS/"+Filenames.GROUPS.toString());
		FileWriter writer = new FileWriter(file);
		return new BufferedWriter(writer);
	}
	
}
