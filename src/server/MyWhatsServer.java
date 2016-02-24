package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import common.Request;


public class MyWhatsServer {
	
	public static void main(String[] args) throws NumberFormatException, IOException {

		
		
		// validate port
		
		ServerSocket server = new ServerSocket(8080);
		server.setReuseAddress(true);
		
		System.out.println("Waiting for connections...");
		
		while(true) {
			Socket clientSocket = server.accept();
			System.out.println("Connection accepted!");
			
			RequestHandler requestHandler = new RequestHandler(clientSocket);
			requestHandler.run();
			
			
		}
		
		

	}

}
