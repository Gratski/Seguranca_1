package common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import proxies.MessagesProxy;

public class Conversation {

	private String directory;
	private String filename;
	private List<Message> msgs;
	private Group group;
	private List<User> users;


	public Conversation(User u1, User u2, String filename) {
		this.filename = filename;
		this.users = new ArrayList<>();
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

		this.group = null;
	}

	public Conversation(Group group) {
		this.filename = null;
		this.group = group;
		this.users = new ArrayList<>(group.getMembers());
	}
	
//	@Override
//	public boolean equals(Object object) {
//		if ( object instanceof Conversation ) {
//			Conversation c = (Conversation) object;
//			return ( this.user1.getName().equals(c.getUser1().getName())
//						&& this.user2.getName().equals(c.getUser2().getName()))
//					||
//					( this.user1.getName().equals(c.getUser2().getName())
//						&& this.user2.getName().equals(c.getUser1().getName()) );
//		} else
//			return false;
//	}

	public boolean addMessage(Message msg) {
		return this.msgs.add(msg);
	}

	public String getFilename(){
		return this.filename;
	}
	
	public String getDirectory(){
		return this.directory;
	}
	
//	public ArrayList<Message> getMessages() throws IOException {
//		this.msgs = MessagesProxy.getInstance().getMessages(this.directory + "/" + this.filename);
//		return this.msgs;
//	}
	
	public Message getLastMessage() throws IOException {
		return MessagesProxy.getInstance().getLastMessage(this.directory + "/" + this.filename);
	}
	
//	public User getUser1(){
//		return this.user1;
//	}
//
//	public User getUser2(){
//		return this.user2;
//	}


}
