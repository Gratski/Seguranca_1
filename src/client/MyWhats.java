package client;

import java.net.Socket;

import common.Message;
import common.Reply;
import common.Request;
import helpers.Connection;
import validators.InputValidator;

public class MyWhats {

	public static void main(String[]args){
		
		try{
			
			//Formula request
			InputValidator valid = new InputValidator();
			Request request = valid.validInput(args);
			if(request == null){
				System.out.println("Parametros mal formed");
				System.exit(-1);
			}
				
			
			//Estabelece ligacao
			Connection connection = new Connection(new Socket("127.0.0.1", 8080));
			
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
