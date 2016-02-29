package common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {

	private String name;
	private User owner;
	private Map<String, User> members;
	private List<Message> msgs;
	
	public Group(String name, User owner){
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
	public boolean addMember(String user){
		if(this.members.containsKey(user))
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
	public boolean hasMember(String user){
		System.out.println(user + "eh membro de " + this.name + "? " + this.members.containsKey(user));
		System.out.println("JOAO eh membro de " + this.name + "? " + this.members.containsKey("JOAO"));
		System.out.println("JOAO,JOSE eh membro de " + this.name + "? " + this.members.containsKey("JOAO,JOSE"));
		System.out.println(this.name + "size eh " + this.members.size());
		return this.members.containsKey(user);
	}
	
	/**
	 * Remove o dado utilizador do grupo caso exista
	 * @param user
	 * 		User a ser adicionado
	 */
	public boolean removeMember(String user){
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
