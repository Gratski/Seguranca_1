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
			System.out.println("aqui: " + parsedInput.get("port"));
			Connection connection = new Connection(new Socket(parsedInput.get("ip"), Integer.parseInt(parsedInput.get("port"))));
			
			System.out.println("Socket criado");
			
			Request request = RequestBuilder.make(parsedInput);
			
			//Envia request
			if( request.getType().equals("-f") )
			{
				System.out.println("Going to send a file");
				FileInputStream finput = new FileInputStream(request.getFile());
				System.out.println("Opened stream: " + request.getFile());
				BufferedInputStream bf = new BufferedInputStream(finput);
				System.out.println("Opened buffered stream: " + request.getFile());
				byte[] arr = new byte[(int) request.getFile().length()];
				System.out.println("Created byte array");
				bf.read(arr, 0, (int) request.getFile().length());
				System.out.println("Bytes copied " + request.getFile().length());
				
				//send request
				connection.getOutputStream().writeObject(request);
				System.out.println("sent request");
				//send file size
				connection.getOutputStream().writeInt((int) request.getFile().length());
				System.out.println("sent filesize");
				//send file
				System.out.println("Is connected: "+connection.isConnected());
				connection.getOutputStream().write(arr, 0, (int) request.getFile().length());
				System.out.println("sent file");
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
