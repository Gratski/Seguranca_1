package client;

import java.net.Socket;

import builders.UserBuilder;
import common.Message;
import common.Reply;
import common.Request;
import common.User;
import helpers.Connection;

public class MyWhats {

	public static void main(String[]args){
		
		//criar uma ligação basica
		
		try{
			UserBuilder userFactory = new UserBuilder();
			User user = userFactory.make(args);

			Connection connection = new Connection(new Socket("127.0.0.1", 8080));
			Request request = new Request();
			request.setType("-m");
			request.setMessage(new Message("Joao", "Simao", "Hey you! :P"));
			request.setUser(user);
			connection.getOutputStream().writeObject(request);
			Reply reply = (Reply) connection.getInputStream().readObject();
			System.out.println(reply.getStatus());
			connection.destroy();
			
		}catch(Exception e){
			System.out.println("Erro ao criar socket");
		}
		
	}
	
}
