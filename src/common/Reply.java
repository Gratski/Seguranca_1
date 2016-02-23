package common;

import java.util.List;

public class Reply extends NetworkMessage {
	
	private List<Conversation> conversations;

	public Reply(List<Conversation> conversations) {
		this.conversations = conversations;
	}
	
	

}
