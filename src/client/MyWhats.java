package client;

import java.net.Socket;

import common.Reply;
import common.Request;
import helpers.Connection;

public class MyWhats {

	public static void main(String[]args){
		
		//criar uma ligação basica
		
		try{
		
			Connection connection = new Connection(new Socket("127.0.0.1", 8080));
			Request request = new Request();
			request.setType("-m");
			connection.getOutputStream().writeObject("HI there!");
			Reply reply = (Reply) connection.getInputStream().readObject();
			System.out.println(reply.getMessage());
			connection.destroy();
			
		}catch(Exception e){
			System.out.println("Erro ao criar socket");
		}
		
	}
	
}
