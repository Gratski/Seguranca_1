package client;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.net.Socket;

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
			if( request.getType().equals("-f") )
			{
				FileInputStream finput = new FileInputStream(request.getFile());
				BufferedInputStream bf = new BufferedInputStream(finput);
				byte[] arr = new byte[(int) request.getFile().length()];
				bf.read(arr, 0, (int) request.getFile().length());
				
				//send request
				connection.getOutputStream().writeObject(request);
				//send file size
				connection.getOutputStream().writeInt((int) request.getFile().length());
				//send file
				connection.getOutputStream().write(arr, 0, (int) request.getFile().length());
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
			System.out.println("Erro ao criar socket");
		}
		
	}
	
}
