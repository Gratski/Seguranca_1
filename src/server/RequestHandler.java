package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import common.Request;
import common.Reply;

public class RequestHandler extends Thread{
	
	private Socket clientSocket;

	public RequestHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
		
		System.out.println("New Thread created!");
	}
	
	public void run() {
		
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(clientSocket.getInputStream());
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		Request clientRequest = null;
		try {
			clientRequest = (Request) in.readObject();
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		try {
			in.close();
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
			ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
			out.writeObject(reply);
			out.close();
		} catch (IOException e) {	
			e.printStackTrace();
		}
		
		try {
			this.clientSocket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
	}
}
