package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import common.Reply;
import common.Request;
import helpers.Connection;

public class RequestHandler extends Thread{
	
	private Connection connection;

	public RequestHandler(Socket clientSocket) throws IOException{
		this.connection = new Connection(clientSocket);
		System.out.println("New Thread created!");
	}
	
	public void run() {
		
		Request clientRequest = null;
		try {
			clientRequest = (Request) this.connection.getInputStream().readObject();
			
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		
		switch (clientRequest.getType()) {
		case "-m":
			System.out.println("Handle send message");
			break;
			
		case "-f":
			System.out.println("Handle send file");
			break;
			
		case "-r":
			System.out.println("Handle send convos");
			break;
			
		case "-a":
			System.out.println("Handle add user to group");
			break;
			
		case "-d":
			System.out.println("Handle delete user from group");
			break;
			
		default:
			break;
		}
		
		
		
		Reply reply = new Reply();
		reply.setMessage("Reply: Message was handled");
		
		try {
			this.connection.getOutputStream().writeObject(reply);
		} catch (IOException e) {	
			e.printStackTrace();
		}
		
		try {
			this.connection.destroy();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
	}
}
