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
		
		
		try{
			
			UsersProxy userProxy = UsersProxy.getInstance();
			
			switch(clientRequest.getType()){
			case "-regUser":
				System.out.println("Registar novo user");
				reply = insertNewUser(clientRequest.getUser(), userProxy);
				break;
			default:
				reply = new Reply();
				reply.setStatus(400);
				reply.setMessage("Comando invalido");
				break;
			}
				
		}catch(Exception e){
			System.out.println("Erro ao registar users");
			e.printStackTrace();
		}
		
			
		//ENVIA RESPOSTA
		try {
			this.connection.getOutputStream().writeObject(reply);
			System.out.println("Replied!");
		} catch (IOException e) {	
			e.printStackTrace();
		}
		
		//FECHA LIGACAO
		try {
			this.connection.destroy();
		} catch (IOException e1) {
			e1.printStackTrace();
		}	
	}

	/**
	 * Adiciona um novo utilizador
	 * @param user
	 * 		User a ser adicionado
	 * @param proxy
	 * 		UsersProxy a ser utilizado
	 * @return
	 * 		reply a ser enviada ao user
	 */
	private Reply insertNewUser(User user, UsersProxy proxy) {
		Reply reply = new Reply();
		System.out.println("USER NULL: " + (user == null));
		System.out.println("PROXY NULL: " + (proxy == null));
		if(proxy.exists(user) || !proxy.insert(user)){
			reply.setStatus(404);
			reply.setMessage("Erro ao adicionar novo utilizador");
		}
		else
			reply.setStatus(200);
			
		return reply;
	}
}
