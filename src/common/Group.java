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
	public boolean addMember(User user){
		if(this.members.containsKey(user.getName()))
			return false;
		
		this.members.put(user.getName(), user);
		return this.members.containsKey(user.getName());
	}
	
	/**
	 * Verifica se o user existe neste group
	 * @param user
	 * 		User a considerar
	 * @return
	 * 		true se existe, false caso contrario
	 */
	public boolean hasMember(User user){
		return this.members.containsKey(user.getName());
	}
	
	/**
	 * Remove o dado utilizador do grupo caso exista
	 * @param user
	 * 		User a ser adicionado
	 */
	public void removeMember(User user){
		this.members.remove(user.getName());
	}
	
	public String getOwner(){
		return this.owner.getName();
	}
	
	
	public String getName(){
		return this.name;
	}
}
