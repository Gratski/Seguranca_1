package common;

import java.io.IOException;
import java.util.ArrayList;

import proxies.MessagesProxy;

public class Conversation {

	private String directory;
	private String filename;
	private ArrayList<Message> msgs;
	private User user1, user2;
	
	public Conversation(User u1, User u2, String filename) {
		this.filename = filename;
		this.user1 = u1;
		this.user2 = u2;
	}
	
	@Override
	public boolean equals(Object object) {
		if ( object instanceof Conversation ) {
			Conversation c = (Conversation) object;
			return ( this.user1.getName().equals(c.getUser1().getName())
						&& this.user2.getName().equals(c.getUser2().getName()))
					||
					( this.user1.getName().equals(c.getUser2().getName())
						&& this.user2.getName().equals(c.getUser1().getName()) );
		} else
			return false;
	}

	public String getFilename(){
		return this.filename;
	}
	
	public String getDirectory(){
		return this.directory;
	}
	
	public ArrayList<Message> getMessages() throws IOException {
		this.msgs = MessagesProxy.getInstance().getMessages(this.directory + "/" + this.filename);
		return this.msgs;
	}
	
	public Message getLastMessage() throws IOException {
		return MessagesProxy.getInstance().getLastMessage(this.directory + "/" + this.filename);
	}
	
	public User getUser1(){
		return this.user1;
	}
	
	public User getUser2(){
		return this.user2;
	}
	
	
}
