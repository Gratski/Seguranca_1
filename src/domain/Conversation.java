package domain;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class represents an entity: Conversation entity
 * It contemplates between 2 or more users
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class Conversation implements Serializable {

	/**
	 * conversation filename
	 */
	private String filename;
	
	/**
	 * conversation messages
	 */
	private ArrayList<Message> msgs;
	
	/**
	 * conversation group if exists
	 */
	private Group group;
	
	/**
	 * conversation members
	 */
	private ArrayList<User> users;

	/**
	 * Constructor
	 *
	 * @param u1 User talker
	 * @param u2 User talker
	 * @param filename conversation filename
     */
	public Conversation(User u1, User u2, String filename) {
		this.filename = filename;
		this.users = new ArrayList<>();
		this.msgs = new ArrayList<>();
		this.users.add(u1);
		this.users.add(u2);
		this.group = null;
	}

	/**
	 * Constructor
	 *
	 * @param u1 User talker
	 * @param u2 User talker
     */
	public Conversation(User u1, User u2) {
		this.filename = null;
		this.users = new ArrayList<>();
		this.users.add(u1);
		this.users.add(u2);
		this.msgs = new ArrayList<>();
		this.group = null;
	}

	/**
	 * Constructor
	 *
	 * @param group conversation group
	 * @require group != null
     */
	public Conversation(Group group) {
		this.filename = null;
		this.group = group;
		this.users = new ArrayList<>(group.getMembersAndOwner());
		this.msgs = new ArrayList<>();
	}

	/**
	 * Get group
	 *
	 * @return Conversation group
     */
	public Group getGroup() {
		return this.group;
	}

	/**
	 * Adds a new message to messages list
	 *
	 * @param msg Message to be added
	 * @return true if added, false otherwise
     */
	public boolean addMessage(Message msg) {
		return this.msgs.add(msg);
	}

	/**
	 * Get filename
	 *
	 * @return String
     */
	public String getFilename(){
		return this.filename;
	}

	/**
	 * Get messages
	 *
	 * @return ArrayList<Message>
     */
	public ArrayList<Message> getMessages() {
		return this.msgs;
	}

	/**
	 * Get conversation members
	 *
	 * @return ArrayList<User>
     */
	public ArrayList<User> getUsers() {
		return users;
	}


	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Conversation{");
		sb.append(", filename='").append(filename).append('\'');
		sb.append(", msgs=").append(msgs);
		sb.append(", group=").append(group);
		sb.append(", users=").append(users);
		sb.append('}');
		return sb.toString();
	}

}
