package common;

public class Request extends NetworkMessage {
	
	private Message message;
	private User contact;
	// private File file;
	private String group;
	
	public Request() {
		super();
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
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
