package proxies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
		
		updateFile();
		return true; 
	}
	
	
	/**
	 * Actualiza o ficheiro de groups
	 * @throws IOException
	 */
	private void updateFile() throws IOException{
		
		//persistencia em ficheiro
		BufferedReader reader = createReader();
		StringBuilder sb = new StringBuilder();
		Collection<Group> list = this.groups.values();
	
		
		for(Group g : list)
		{
			
			sb.append("data");
			sb.append(" " + g.getOwner());
			sb.append(" " + g.getName());
			
			Collection<User> members = g.getMembers().values();
			int i = 0;
			for( User m : members )
			{
				//se eh o primeiro dos membros
				if( i == 0 )
					sb.append(" " + m.getName());
				else
					sb.append(","+m.getName());
				
				i++;
			}
			
			sb.append("\n");
			
		}
		
		//reescreve ficheiro
		BufferedWriter writer = createWriter();
		writer.write(sb.toString());
		writer.close();
		
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
