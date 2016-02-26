package server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import common.Message;
import common.Reply;
import common.Request;
import common.User;
import helpers.Connection;
import proxies.MessagesProxy;
import proxies.UsersProxy;

public class RequestHandler extends Thread{
	
	private Connection connection;

	public RequestHandler(Socket clientSocket) throws IOException{
		this.connection = new Connection(clientSocket);
		System.out.println("New Thread created!");
	}
	
	public void run() {
		
		Request clientRequest = null;
		
		Reply reply = new Reply();
		reply.setStatus(200);
		
		String errorMessage = null;
		
		//Proxies
		UsersProxy userProxy = null;
		MessagesProxy msgProxy = null;
		
		try {
			System.out.println("receiving request");
			clientRequest = (Request) this.connection.getInputStream().readObject();
		} catch (ClassNotFoundException e2) {
			this.interrupt();
			e2.printStackTrace();
		} catch (IOException e2) {
			this.interrupt();
			e2.printStackTrace();
		}
		
		//autenticar user aqui
		User user = clientRequest.getUser();
		System.out.println("Server user is " + user.getName());
		System.out.println("Server user password is " + user.getPassword());
		try{
			userProxy = UsersProxy.getInstance();
			System.out.println("UsersProxy opened: " + (userProxy != null));
			if( userProxy.exists(user) )
			{
				if( !userProxy.autheticate(user) ){
					reply.setStatus(401);
					reply.setMessage("Erro de autenticacao");
				}else{
					System.out.println("User autenticado");
				}
			}else{
				if( !userProxy.insert(user) ){
					reply.setStatus(402);
					reply.setMessage("Erro ao criar o novo utilizador");
				}
			}
			 
		}catch(Exception e){
			reply.setStatus(400);
			reply.setMessage("Erro ao aceder a utilizadores");
			e.printStackTrace();
		}
		
		
		/*
		//request type
		switch (clientRequest.getType()) {
		case "-m":
			System.out.println("Handle send message");
			Message msg = clientRequest.getMessage();
			
			//verifica se o user existe
			if( !userProxy.exists(new User(msg.getTo())) )
			{
				reply.setStatus(401);
				reply.setMessage("User de destino not found");
				break;
			}
			
			try{
				
				msgProxy = MessagesProxy.getInstance();
				
			}catch(IOException e){
				reply.setMessage("Erro ao obter instance de MsgsProxy");
				reply.setStatus(400);
				break;
			}
			try{
				if(!msgProxy.addMessage(msg.getFrom(), msg.getTo(), "-u", msg.getBody()))
				{
					reply.setStatus(402);
					reply.setMessage("Erro ao enviar mensagem");
				}
			}catch(Exception e){
				reply.setStatus(403);
				reply.setMessage("Erro ao aceder a instance de mensagens");
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
				reply.setStatus(400);
				reply.setMessage("Erro ao enviar files");
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
		*/
			
		
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
