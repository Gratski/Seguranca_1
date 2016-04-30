package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents an entity: Group
 * A group is a union of users
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class Group implements Serializable {

	/**
	 * group name
	 */
	private String name;
	
	/**
	 * group owner
	 */
	private User owner;
	
	/**
	 * group members
	 */
	private Map<String, User> members;

	/**
	 * Constructor
	 *
	 * @param name, group name
	 * @param owner, group owner
     */
	public Group(String name, User owner) {
		this.name = name;
		this.owner = owner;
		this.members = new HashMap<>();
	}

	/**
	 * Get group members
	 *
	 * @return Collection<User> group members
     */
	public Collection<User> getMembersAndOwner(){
		Collection<User> allMembers = new ArrayList<>(this.members.values());
		allMembers.add(owner);
		return allMembers;
	}
	
	/**
	 * Adds a new user to group
	 *
	 * @param user, User to be added
	 */
	public boolean addMember(String user) {
		if (this.members.containsKey(user))
			return false;
		
		this.members.put(user, new User(user));
		return this.members.containsKey(user);
	}
	
	/**
	 * Checks if a given user is member of group
	 *
	 * @param user, user to consider when searching
	 * @return true if exists, false otherwise
	 */
	public boolean hasMember(String user) {
		return this.members.containsKey(user);
	}

	/**
	 * Checks if a given user owns this group
	 *
	 * @param user, user name to be considered
	 * @return true if so, false otherwise
     */
	public boolean hasMemberOrOwner(String user) {
		return (hasMember(user) || this.owner.getName().equals(user));
	}
	
	/**
	 * Removes the given member from group
	 *
	 * @param user, user to be removed
	 */
	public boolean removeMember(String user) {
		this.members.remove(user);
		return !this.members.containsKey(user);
	}

	/**
	 * Get group owner name
	 *
	 * @return String, group owner name
     */
	public String getOwner(){
		return this.owner.getName();
	}

	/**
	 * Gets group name
	 *
	 * @return String, group name
     */
	public String getName(){
		return this.name;
	}
}
