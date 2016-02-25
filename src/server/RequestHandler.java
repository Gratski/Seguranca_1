package server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import common.Message;
import common.Reply;
import common.Request;
import helpers.Connection;
import proxies.MessagesProxy;

public class RequestHandler extends Thread{
	
	private Connection connection;

	public RequestHandler(Socket clientSocket) throws IOException{
		this.connection = new Connection(clientSocket);
		System.out.println("New Thread created!");
	}
	
	public void run() {
		
		Request clientRequest = null;
		try {
			System.out.println("receiving request");
			clientRequest = (Request) this.connection.getInputStream().readObject();
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		//autenticar user aqui
		
		//request type
		boolean valid = true;
		String errorMessage = null;
		switch (clientRequest.getType()) {
		case "-m":
			System.out.println("Handle send message");
			Message msg = clientRequest.getMessage();
			MessagesProxy msgProxy = null;
			try{
				msgProxy = MessagesProxy.getInstance();
			}catch(IOException e){
				errorMessage = "Erro ao obter instance de MsgsProxy";
				valid = false;
				break;
			}
			try{
				valid = msgProxy.addMessage(msg.getFrom(), msg.getTo(), "-u", msg.getBody());
			}catch(Exception e){
				errorMessage = "Erro ao gravar mensagem";
				valid = false;
				break;
			}			
			break;
			
		case "-f":
			System.out.println("Handle send file");
			try{
				int filesize = this.connection.getInputStream().readInt();
				System.out.println("Server: File size is " + filesize);
				byte[] arr = new byte[filesize];
				this.connection.getInputStream().read(arr, 0, filesize);
				System.out.println("Got the file!");
				String[] fileNameArr = clientRequest.getFile().getName().split("/");
				String fileName = fileNameArr[fileNameArr.length - 1];
				
				File file = new File(fileName);
				file.createNewFile();
				FileOutputStream writer = new FileOutputStream(file);
				writer.write(arr);
				writer.flush();
				writer.close();
				
			}catch(Exception e){
				valid = false;
				errorMessage = "Error getting file";
				System.out.println("Error");
				break;
			}
			
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
			System.out.println("Request invalido");
			break;
		}
		
		Reply reply = new Reply();
		if(valid){
			reply.setStatus(200);
		}else{
			reply.setStatus(500);
			reply.setMessage(errorMessage);
		}
		
		try {
			this.connection.getOutputStream().writeObject(reply);
			System.out.println("Replied!");
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
