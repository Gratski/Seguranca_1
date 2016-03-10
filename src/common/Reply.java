package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Reply extends NetworkMessage implements Serializable {
	
	private List<Conversation> conversations;
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

	public void setConversations(List<Conversation> conversations) {
		this.conversations = conversations;
	}

	public void addConversation(Conversation conversation) {
		List<Conversation> list = new ArrayList<>();
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

	/**
	 * Verifica se este reply tem error status
	 * @return
	 * 	true se tem, false caso contrario
	 */
	public boolean hasError(){
		return !(this.status == 200 || this.status == 0 );
	}
	
}
