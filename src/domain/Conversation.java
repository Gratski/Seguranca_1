package domain;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import proxies.MessagesProxy;

public class Conversation implements Serializable {

	private String directory;
	private String filename;
	private ArrayList<Message> msgs;
	private Group group;
	private ArrayList<User> users;

	public Conversation(User u1, User u2, String filename) {
		this.filename = filename;
		this.users = new ArrayList<>();
		this.msgs = new ArrayList<>();
		this.users.add(u1);
		this.users.add(u2);
		this.group = null;
	}

	public Group getGroup() {
		return this.group;
	}

	public Conversation(User u1, User u2) {
		this.filename = null;
		this.users = new ArrayList<>();
		this.users.add(u1);
		this.users.add(u2);
		this.msgs = new ArrayList<>();
		this.group = null;
	}

	public Conversation(Group group) {
		this.filename = null;
		this.group = group;
		this.users = new ArrayList<>(group.getMembers());
		this.msgs = new ArrayList<>();
	}

	public boolean addMessage(Message msg) {
		return this.msgs.add(msg);
	}

	public String getFilename(){
		return this.filename;
	}
	
	public String getDirectory(){
		return this.directory;
	}

	public ArrayList<Message> getMsgs() {
		return this.msgs;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Conversation{");
		sb.append("directory='").append(directory).append('\'');
		sb.append(", filename='").append(filename).append('\'');
		sb.append(", msgs=").append(msgs);
		sb.append(", group=").append(group);
		sb.append(", users=").append(users);
		sb.append('}');
		return sb.toString();
	}

	//	public ArrayList<Message> getMessages() throws IOException {
//		this.msgs = MessagesProxy.getInstance().getMessages(this.directory + "/" + this.filename);
//		return this.msgs;
//	}
	
//	public Message getLastMessage() throws IOException {
//		return MessagesProxy.getInstance().getLastMessage(this.directory + "/" + this.filename);
//	}
	
//	public User getUser1(){
//		return this.user1;
//	}
//
//	public User getUser2(){
//		return this.user2;
//	}


}
