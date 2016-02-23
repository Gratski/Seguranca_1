package common;

import java.util.List;

public class Reply extends NetworkMessage {
	
	private List<Conversation> conversations;
	private int status;
	private String message;

	public Reply() {
		this.conversations = null;
		this.status = 0;
		this.message = null;
	}

	public void setConversations(List<Conversation> conversations) {
		this.conversations = conversations;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
