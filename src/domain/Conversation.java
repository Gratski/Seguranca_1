package domain;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import proxies.MessagesProxy;

/**
 * Esta classe representa a entidade Conversacao
 * Contempla conversacoes entre 2 ou mais intervenientes
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class Conversation implements Serializable {

	private String filename;
	private ArrayList<Message> msgs;
	private Group group;
	private ArrayList<User> users;

	/**
	 * Constructor
	 *
	 * @param u1 User interveniente
	 * @param u2 User interveniente
	 * @param filename Nome do ficheiro de Conversation
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
	 * @param u1 User interveniente
	 * @param u2 User interveniente
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
	 * @param group Group de Conversation
	 * @require group != null
     */
	public Conversation(Group group) {
		this.filename = null;
		this.group = group;
		this.users = new ArrayList<>(group.getMembers());
		this.msgs = new ArrayList<>();
	}

	/**
	 * Obter group
	 *
	 * @return Group de Conversation
     */
	public Group getGroup() {
		return this.group;
	}

	/**
	 * Adiciona mensagem a msgs
	 *
	 * @param msg Message a adicionar a msgs
	 * @return true se adicionou, false caso contrario
     */
	public boolean addMessage(Message msg) {
		return this.msgs.add(msg);
	}

	/**
	 * Obter filename
	 *
	 * @return String
     */
	public String getFilename(){
		return this.filename;
	}

	/**
	 * Obter Messages
	 *
	 * @return ArrayList<Message>
     */
	public ArrayList<Message> getMessages() {
		return this.msgs;
	}

	/**
	 * Obter users intervenientes
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
