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
		
		//validacao de input
		//formato <localUser> <ip:port> -p <password> -m <contact> <body>
		//possiveis flags -m, -f, -r, -a, -d
		//minimum length 3 <localUser> <ip:port> -r
		
		try{
			
			//User
			UserBuilder userFactory = new UserBuilder();
			User user = userFactory.make(args);
			
			//Estabelece ligacao
			Connection connection = new Connection(new Socket("127.0.0.1", 8080));
			
			//Formula request
			Request request = new Request();
			request.setType("-m");
			request.setMessage(new Message("Joao", "Simao", "Hey you! :P"));
			request.setUser(user);
			
			//Envia request
			connection.getOutputStream().writeObject(request);
			
			//Obtem reply
			Reply reply = (Reply) connection.getInputStream().readObject();
			
			//Se erro
			if(reply.getStatus() != 200)
				System.out.println(reply.getMessage());
			
			//Fecha connection
			connection.destroy();
			
		}catch(Exception e){
			System.out.println("Erro ao criar socket");
		}
		
	}
	
}
