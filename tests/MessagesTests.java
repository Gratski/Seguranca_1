import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.net.Socket;

import org.junit.Test;

import client.MyWhats;
import common.Message;
import common.Request;
import common.User;
import helpers.Connection;

public class MessagesTests {
	
	private static User validUser = new User("Joao", "myPass");
	private static User invalidUserPass = new User("Joao", "myPasse");
	private static User invalidUserName = new User("Simao", "myPass");
	
	private static Message msg = new Message(null, null, null);
	
	
	/*
	@Test
	public void sendMessageWithInvalidCredentialsName() throws Exception{
		Connection conn = setConnection();
		Request req = new Request();
		req.setUser(invalidUserName);
		req.setType("-m");
		req
		MyWhats.sendRequest(conn, req);
		assertNotEquals(MyWhats.receiveReply(conn).getStatus(), 200);
		
	}
	
	@Test
	public void sendMessageWithInvalidCredentialsPassword(){
		
	}
	
	@Test
	public void sendMessageToExistingUser(){
		
	}
	
	@Test
	public void sendMessageToNonExistingUser(){
		
	}
	
	
	//obtaining messages
	//private conversation
	@Test
	public void obtainMessageFromPrivateUser(){
		
	}
	
	@Test
	public void obtainMessageFromNonExistingPrivateUser(){
		
	}
	
	@Test
	public void obtainMessageFromExistingUserButNonExistingConversation(){
		
	}
	
	//group conversation
	@Test
	public void obtainMessageFromGroup(){
		
	}
	
	@Test
	public void obtainMessageFromNonExistingGroup(){
		
	}
	
	@Test
	public void obtainMessageFromExistingGroupButNotMember(){
		
	}
	
	*/
	
	private Connection setConnection(){
		Connection conn = null;
		try{
			conn = new Connection(new Socket("127.0.0.1", 8080));
		}catch(IOException e){
			System.out.println("Erro ao estabelecer ligacao");
			System.exit(-1);
		}
		return conn;
	}
	
}
