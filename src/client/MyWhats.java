package client;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import builders.RequestBuilder;
import common.Reply;
import common.Request;
import helpers.Connection;
import helpers.FilesHandler;
import validators.InputValidator;

public class MyWhats {

	public static void main(String[]args){
		
		try{
			
			//input validation
			if (!InputValidator.validInput(args)){
				System.out.println("Parametros mal formed");
				System.exit(-1);
			}	
			
			//parse input
			HashMap<String, String> parsedInput = InputValidator.parseInput(args);
			
			//estabelece ligacao
			Connection connection = new Connection(new Socket(parsedInput.get("ip"), Integer.parseInt(parsedInput.get("port"))));
			
			//create request obj
			Request request = RequestBuilder.make(parsedInput);
			
			//send request
			sendRequest(connection, request);
			//get reply
			Reply reply = (Reply) connection.getInputStream().readObject();
			
			//if error
			if(reply.getStatus() != 200)
				System.out.println(reply.getMessage());
			
			
			connection.destroy();
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Erro ao criar socket");
		}
		
	}
	
	/**
	 * This method handles a request sending
	 * @param conn
	 * 		Connection to be considered
	 * @param req
	 * 		Request to be considered
	 * @throws IOException
	 * @require
	 * 		conn != null && req != null && conn.getOutputStream != null
	 */
	private static void sendRequest(Connection conn, Request req) throws IOException{
		//send base request
		conn.getOutputStream().writeObject(req);
		System.out.println("FILENAME: " + req.getFile().getFullPath());
		//file type request handler
		switch(req.getType()){
		case "-f":	
				FilesHandler fHandler = new FilesHandler();
				try{
					System.out.println("Sending file...");
					fHandler.send(conn, new File(req.getFile().getFullPath()));
				}catch(Exception e){
					e.printStackTrace();
				}
			break;
		}
		
		
	}
	
}
