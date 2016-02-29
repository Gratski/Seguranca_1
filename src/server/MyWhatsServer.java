package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import proxies.ConversationsProxy;
import proxies.GroupsProxy;
import proxies.UsersProxy;



public class MyWhatsServer {
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		
		//init fs
		prepareFileStructure();
		
		GroupsProxy groups = GroupsProxy.getInstance();
		UsersProxy users = UsersProxy.getInstance();
		ConversationsProxy conversations = ConversationsProxy.getInstance();
		
		@SuppressWarnings("resource")
		ServerSocket server = new ServerSocket(8080);
		server.setReuseAddress(true);
		
		
		while(true) {
			System.out.println("===============================");
			System.out.println("Waiting for connections...");
			Socket clientSocket = server.accept();
			System.out.println("Connection accepted!");
			
			RequestHandler requestHandler = new RequestHandler(clientSocket, users, groups, conversations);
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
		// makeDatabaseMessages();
	}
	private static void makeDatabaseConversations() {
		try{
			File file = new File("DATABASE/CONVERSATIONS");
			if(!file.isDirectory())
				if(!file.mkdir()){
					new Exception();
				}

			file = new File("DATABASE/CONVERSATIONS/PRIVATE");
			if (!file.isDirectory())
				if (!file.mkdir()){
					new Exception();
				}
		}catch(Exception e){
			System.out.println("Erro ao criar estrutura de pastas inicial para CONVERSATIONS");
			System.exit(-1);
		}
	}

	// Not in use probably
	private static void makeDatabaseMessages() {
		try{
			File file = new File("DATABASE/MESSAGES");
			if (!file.isDirectory())
				if (!file.mkdir())
					new Exception();
		} catch (Exception e) {
			System.out.println("Erro ao criar estrutura de pastas inicial");
			System.exit(-1);
		}
	}
	private static void makeDatabaseGroups() {
		try {
			File file = new File("DATABASE/GROUPS");
			file.createNewFile();
				
		} catch (Exception e) {
			System.out.println("Erro ao criar ficheiro de GROUPS");
			System.exit(-1);
		}
	}
	private static void makeDatabaseUsers() {
		try {
			File file = new File("DATABASE/USERS");
			file.createNewFile();
				
		} catch (Exception e) {
			System.out.println("Erro ao criar ficheiro de USERS");
			System.exit(-1);
		}
	}
	private static void makeDatabase() {
		try {
			File file = new File("DATABASE");
			if(!file.isDirectory())
				if(!file.mkdir()){
					new Exception();
				}
		} catch(Exception e) {
			System.out.println("Erro ao criar estrutura de pastas inicial");
			System.exit(-1);
		}
	}
	
}
