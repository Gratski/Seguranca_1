package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;



public class MyWhatsServer {
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		
		//init fs
		prepareFileStructure();
		
		@SuppressWarnings("resource")
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

	//Metodos utilizados para garantir a existencia
	//de todos os directorios necessarios para
	//a execucao do programa
	private static void prepareFileStructure(){
		makeDatabase();
		makeDatabaseUsers();
		makeDatabaseGroups();
		makeDatabaseConversations();
		makeDatabaseMessages();
	}
	private static void makeDatabaseConversations() {
		try{
			File file = new File("DATABASE/CONVERSATIONS");
			if(!file.isDirectory())
				if(!file.mkdir()){
					new Exception();
				}
		}catch(Exception e){
			System.out.println("Erro ao criar estrutura de pastas inicial");
			System.exit(-1);
		}
	}
	private static void makeDatabaseMessages() {
		try{
			File file = new File("DATABASE/MESSAGES");
			if(!file.isDirectory())
				if(!file.mkdir()){
					new Exception();
				}
		}catch(Exception e){
			System.out.println("Erro ao criar estrutura de pastas inicial");
			System.exit(-1);
		}
	}
	private static void makeDatabaseGroups() {
		try{
			File file = new File("DATABASE/GROUPS");
			if(!file.isDirectory())
				if(!file.mkdir()){
					new Exception();
				}
		}catch(Exception e){
			System.out.println("Erro ao criar estrutura de pastas inicial");
			System.exit(-1);
		}
	}
	private static void makeDatabaseUsers() {
		try{
			File file = new File("DATABASE/USERS");
			if(!file.isDirectory())
				if(!file.mkdir()){
					new Exception();
				}
		}catch(Exception e){
			System.out.println("Erro ao criar estrutura de pastas inicial");
			System.exit(-1);
		}
	}
	private static void makeDatabase() {
		try{
			File file = new File("DATABASE");
			if(!file.isDirectory())
				if(!file.mkdir()){
					new Exception();
				}
		}catch(Exception e){
			System.out.println("Erro ao criar estrutura de pastas inicial");
			System.exit(-1);
		}
	}
	
}
