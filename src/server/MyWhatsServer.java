package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import common.Request;


public class MyWhatsServer {
	
	public static void main(String[] args) throws NumberFormatException, IOException, ClassNotFoundException {

		String port = args[0];
		
		// validate port
		
		ServerSocket server = new ServerSocket(Integer.parseInt(port));
		server.setReuseAddress(true);
		System.out.println("Waiting for connections...");
		
		while(true) {
			Socket clientSocket = server.accept();
			System.out.println("Connection accepted!");
			
			ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			
			Request clientRequest = (Request) in.readObject();
			in.close();
			RequestHandler requestHandler = new RequestHandler(clientSocket, clientRequest);
			requestHandler.run();
			
			
		}
		
		

	}

}
