package server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import common.Request;
import common.Reply;

public class RequestHandler extends Thread{
	
	private Socket clientSocket;
	private Request clientRequest;

	public RequestHandler(Socket clientSocket, Request clientRequest) {
		this.clientSocket = clientSocket;
		this.clientRequest = clientRequest;
	}
	
	public void run() {
		
		switch (clientRequest.getType()) {
		case "-a":
			
			break;

		default:
			break;
		}
		
		
		
		
		
		
		Reply reply = new Reply();
		
			
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
