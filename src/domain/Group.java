package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Esta classe representa a entidade Group
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class Group implements Serializable {

	private String name;
	private User owner;
	private Map<String, User> members;

	/**
	 * Constructor
	 *
	 * @param name Nome de Group
	 * @param owner Owner de Group
     */
	public Group(String name, User owner) {
		this.name = name;
		this.owner = owner;
		this.members = new HashMap<>();
	}

	/**
	 * Obter membros de Group
	 *
	 * @return Collection<User> membros do Group
     */
	public Collection<User> getMembersAndOwner(){
		Collection<User> allMembers = new ArrayList<>(this.members.values());
		allMembers.add(owner);
		return allMembers;
	}
	
	/**
	 * Adiciona um user ao Group
	 *
	 * @param user User a ser adicionado ao Group
	 */
	public boolean addMember(String user) {
		if (this.members.containsKey(user))
			return false;
		
		this.members.put(user, new User(user));
		return this.members.containsKey(user);
	}
	
	/**
	 * Verifica se o user existe neste group
	 *
	 * @param user User a considerar
	 * @return true se existe, false caso contrario
	 */
	public boolean hasMember(String user) {
		return this.members.containsKey(user);
	}

	/**
	 * Verifca se um dado nome eh o nome do Group Owner
	 *
	 * @param user Nome de User a ser considerado
	 * @return true se eh Owner, false caso contrario
     */
	public boolean hasMemberOrOwner(String user) {
		return (hasMember(user) || this.owner.getName().equals(user));
	}
	
	/**
	 * Remove o dado utilizador do Group caso exista
	 *
	 * @param user User a ser adicionado
	 */
	public boolean removeMember(String user) {
		this.members.remove(user);
		return !this.members.containsKey(user);
	}

	/**
	 * Obtem o nome do Group Owner
	 *
	 * @return String
     */
	public String getOwner(){
		return this.owner.getName();
	}

	/**
	 * Obtem o nome do Group
	 *
	 * @return String
     */
	public String getName(){
		return this.name;
	}
}
