package proxies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import builders.FileStreamBuilder;
import common.Group;
import common.User;
import enums.Filenames;

public class GroupsProxy implements Proxy {

	private static GroupsProxy instance = null;
	private File file;
	private FileWriter fw;
	private BufferedWriter bw;
	private Map<String, Group> groups;
	
	private GroupsProxy() throws IOException {
		this.groups = new HashMap<>();
		this.init();
	}
	
	public static GroupsProxy getInstance() throws IOException {
		if (instance == null)
			instance = new GroupsProxy();
		return instance;
	}

	public void init() throws IOException {
		BufferedReader br = FileStreamBuilder.makeReader("DATABASE/" + Filenames.GROUPS.toString());
		
		//reading users file
		String line = null;
		while ((line = br.readLine()) != null) {
			
			//read group
			String[] lineSplit = line.split(" ");
			String owner = lineSplit[0];
			String name = lineSplit[1];
			Group group = new Group(name, new User(owner));
			
			//adiciona membros ao group
			if (lineSplit.length > 2) {
				String[] members = lineSplit[2].split(",");
				for (String member : members)
					group.addMember(member);
			}	
			//adiciona aos groups
			this.groups.put(name, group);
		}
		br.close();
	}
	
	public Map<String, Group> getGroupsWhereMember(String name) {
		return this.groups;
	}
	
	/**
	 * Find a group by name if exists
	 * @param groupName
	 * 		GroupName to be searchedBy
	 * @return
	 * 		Group if exists, null if not
	 * @throws IOException
	 */
	public Group find(String groupName) throws IOException {
		return this.groups.containsKey(groupName) ? this.groups.get(groupName) : null;
	} 
	
	/**
	 * Adiciona um novo group a groups
	 * @param groupName
	 * 		Nome do novo group
	 * @param owner
	 * 		Owner do novo group
	 */
	public void create(String groupName, User owner) throws IOException {
		this.groups.put(groupName, new Group(groupName, owner));
		
		//persistencia em ficheiro
		BufferedWriter bw = FileStreamBuilder.makeWriter("DATABASE/" + Filenames.GROUPS.toString(), true);
		StringBuilder sb = new StringBuilder();
		sb.append(owner.getName() + " " + groupName);
		sb.append("\n");
		
		bw.write(sb.toString());
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
	public boolean isOwner(String groupName, String username) {
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
	 * Verifica se um determinado group tem um
	 * determinado member
	 * @param groupName
	 * 		Nome do group a considerar
	 * @param user
	 * 		User a considerar
	 * @return
	 * 		true se eh membro, false caso contrario
	 */
	public boolean hasMember(String groupName, String user){
		return this.groups.get(groupName).hasMember(user);
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
	public boolean addMember(String groupName, String member) throws IOException {
		if ( !this.groups.get(groupName).addMember(member) )
			return false;
		
		updateFile();
		return true; 
	}
	
	/**
	 * Remove um dado utilizador de um dado group
	 * @param groupName
	 * 		Nome do group a considerar
	 * @param member
	 * 		Member a ser retirado do grupo
	 * @return
	 * 		true se ok, false caso contrario
	 * @throws IOException
	 */
	public boolean removeMember(String groupName, String member) throws IOException {
		if (!this.groups.get(groupName).removeMember(member))
			return false;
		
		updateFile();
		return true;
	}
	
	
	/**
	 * Actualiza o ficheiro de groups
	 * @throws IOException
	 */
	private void updateFile() throws IOException {
		//persistencia em ficheiro
		StringBuilder sb = new StringBuilder();
		Collection<Group> list = this.groups.values();

		for (Group g : list) {
			sb.append(g.getOwner());
			sb.append(" " + g.getName());
			
			Collection<User> members = g.getMembers();
			int i = 0;
			for ( User m : members ) {
				//se eh o primeiro dos membros
				if ( i == 0 )
					sb.append(" " + m.getName());
				else
					sb.append("," + m.getName());
				
				i++;
			}
			sb.append("\n");
		}
		//reescreve ficheiro
		BufferedWriter writer = FileStreamBuilder.makeWriter("DATABASE/" + Filenames.GROUPS.toString(), false);
		writer.write(sb.toString());
		writer.close();
	}
	
	@Override
	public void destroy() throws IOException {
		
	}
}
