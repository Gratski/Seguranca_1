import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;

import org.junit.Test;

import domain.Message;
import domain.User;
import proxies.ConversationsProxy;

public class MessagesTests {
	
	private static User validUser = new User("Joao", "myPass");
	private static User invalidUserPass = new User("Joao", "myPasse");
	private static User invalidUserName = new User("Simao", "myPass");

	private static Message msg = new Message("Joao", "Simao", "TestsSuite automatic message");
	private static Message msgGroup = new Message("Joao", "FCUL", "TestsSuite automatic message non good");
	
	@Test
	public void insertValidPrivateMessage() throws IOException{
		boolean sent = ConversationsProxy.getInstance().insertPrivateMessage(msg);
		assertEquals(sent, true);
	}
	
	@Test
	public void insertValidGroupMessage() throws IOException{		
		boolean sent = ConversationsProxy.getInstance().insertPrivateMessage(msgGroup);
		assertEquals(sent, true);
	}
	
}
