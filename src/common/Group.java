package common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {

	private String name;
	private User owner;
	private Map<String, User> members;
	private List<Message> msgs;
	
	public Group(String name, User owner) {
		this.name = name;
		this.owner = owner;
		this.members = new HashMap<>();
		this.msgs = new ArrayList<>();
	}
	
	public Map<String, User> getMembers(){
		return this.members;
	}
	
	/**
	 * Adiciona um user ao grupo
	 * @param user
	 * 		User a ser adicionado ao grupo
	 */
	public boolean addMember(String user) {
		if (this.members.containsKey(user))
			return false;
		
		this.members.put(user, new User(user));
		return this.members.containsKey(user);
	}
	
	/**
	 * Verifica se o user existe neste group
	 * @param user
	 * 		User a considerar
	 * @return
	 * 		true se existe, false caso contrario
	 */
	public boolean hasMember(String user) {
		Collection<User> members = this.members.values();
		User[] list = new User[members.size()];
		members.toArray(list);
		for(User u : list)
			System.out.println("Member: " + u.getName());
		return this.members.containsKey(user);
	}
	
	/**
	 * Remove o dado utilizador do grupo caso exista
	 * @param user
	 * 		User a ser adicionado
	 */
	public boolean removeMember(String user) {
		this.members.remove(user);
		return !this.members.containsKey(user);
	}
	
	public String getOwner(){
		return this.owner.getName();
	}
	
	public String getName(){
		return this.name;
	}
}
