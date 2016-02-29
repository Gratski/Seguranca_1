package common;

import java.io.File;
import java.io.Serializable;

public class Request extends NetworkMessage implements Serializable{
	
	private Message message;
	private String contact;
	private File file;
	private String group;

	public Message getMessage() {
		return this.message;
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

	public String getContact() {
		return this.contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("=== REQUEST ===\n");
		sb.append("User: " + this.getUser().toString() + "\n");
		sb.append("Type: " + this.getType() + "\n");
		sb.append("Message: " + this.getMessage() + "\n");
		sb.append("Contact: " + this.getContact() + "\n");
		sb.append("Group: " + this.getGroup() + "\n");
		return sb.toString();
	}
	
}
