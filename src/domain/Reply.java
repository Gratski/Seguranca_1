package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import client.MyWhats;

public class Reply extends NetworkMessage implements Serializable {
	
	private ArrayList<Conversation> conversations;
	private int status;
	private String message;

	public Reply() {
		this.conversations = null;
		this.status = 0;
		this.message = null;
	}

	public Reply(int status, String message) {
		this.conversations = null;
		this.status = status;
		this.message = message;
	}

	public Reply(int status) {
		this.status = status;
		this.message = null;
		this.conversations = null;
	}

	public ArrayList<Conversation> getConversations() {
		return this.conversations;
	}

	public void setConversations(ArrayList<Conversation> conversations) {
		this.conversations = conversations;
	}

	public void addConversation(Conversation conversation) {
		ArrayList<Conversation> list = new ArrayList<>();
		list.add(conversation);
		this.conversations = list;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getMessage(){
		return this.message;
	}

	public int getStatus(){
		return this.status;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Reply{");
		sb.append("conversations=").append(conversations);
		sb.append(", status=").append(status);
		sb.append(", message='").append(message).append('\'');
		sb.append('}');
		return sb.toString();
	}
	
	public void prettyPrint(User user){
		
		//se erro
		if(this.hasError()){
			System.out.println("Error");
			System.out.println("Description: " + this.message);
		}
		//se ok
		else{
			//se eh -r
			if(this.conversations != null)
			{
				
				//se eh apenas de contact
				if(conversations.size() == 1)
				{
					ArrayList<Message> messages = conversations.get(0).getMessages();
					Collections.sort(messages);
					for (Message message : messages) {
						MyWhats.printMessage(user.getName(), message);
					}
				}
				//se eh last de todos os contact
				else{
					for (Conversation conversation : conversations) {
						if (conversation.getGroup() != null)
							System.out.println("Contact: " + conversation.getGroup().getName());
						else {
							ArrayList<User> users = conversation.getUsers();
							String finalContact = null;
							for (User u : users) {
								if (u.getName().equals(user.getName()))
									continue;
								finalContact = u.getName();
							}
							System.out.println("Contact: " + finalContact);
						}
						ArrayList<Message> messages = conversation.getMessages();
						if (messages != null && messages.size() == 1) {
							MyWhats.printMessage(user.getName(), messages.get(0));
						} else {

						}
					}
				}
				
			}
		}
	}

	/**
	 * Verifica se este reply tem error status
	 * @return
	 * 	true se tem, false caso contrario
	 */
	public boolean hasError(){
		return !(this.status == 200 || this.status == 0 );
	}
	
}
