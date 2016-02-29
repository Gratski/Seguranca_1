package client;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.HashMap;

import builders.RequestBuilder;
import common.Reply;
import common.Request;
import helpers.Connection;
import validators.InputValidator;

public class MyWhats {

	public static void main(String[]args){
		
		try{
			
			// Verifica input
			if (!InputValidator.validInput(args)){
				System.out.println("Parametros mal formed");
				System.exit(-1);
			}	
			
			//Estabelece ligacao
			HashMap<String, String> parsedInput = InputValidator.parseInput(args);
			Connection connection = new Connection(new Socket(parsedInput.get("ip"), Integer.parseInt(parsedInput.get("port"))));
			
			Request request = RequestBuilder.make(parsedInput);
			
			//Envia request
			if ( request.getType().equals("-f") )
			{
				
			}
			else
				connection.getOutputStream().writeObject(request);
			//Obtem reply
			Reply reply = (Reply) connection.getInputStream().readObject();
			
			//Se erro
			if(reply.getStatus() != 200)
				System.out.println(reply.getMessage());
			
			//Fecha connection
			connection.destroy();
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Erro ao criar socket");
		}
		
	}
	
}
