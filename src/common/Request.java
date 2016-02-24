package common;

import java.io.File;
import java.io.Serializable;

public class Request extends NetworkMessage implements Serializable{
	
	private Message message;
	private User contact;
	private File file;
	private String group;

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}
	public void setFile(File f){
		this.file = f;
	}
	public File getFile(){
		return this.file;
	}

	public User getContact() {
		return contact;
	}

	public void setContact(User contact) {
		this.contact = contact;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
	
	
	
}
