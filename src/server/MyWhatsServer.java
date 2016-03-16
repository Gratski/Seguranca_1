package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import handlers.RequestHandler;
import helpers.DatabaseBuilder;
import proxies.ConversationsProxy;
import proxies.GroupsProxy;
import proxies.UsersProxy;
import validators.InputValidator;

public class MyWhatsServer {
	
	public static void main(String[] args) throws NumberFormatException, IOException {

		if (!InputValidator.validServerInput(args)) {
			System.out.println("Parâmetros mal formados");
			System.exit(-1);
		}

		//prepare file structure
		DatabaseBuilder dbBuilder = new DatabaseBuilder();
		boolean createdFolders = dbBuilder.make();
		if(!createdFolders)
			System.exit(-1);

		//obtain singletons
		GroupsProxy groups = GroupsProxy.getInstance();
		UsersProxy users = UsersProxy.getInstance();
		ConversationsProxy conversations = ConversationsProxy.getInstance();

		@SuppressWarnings("resource")
		int port = Integer.parseInt(args[0]);
		ServerSocket server = new ServerSocket(port);
		server.setReuseAddress(true);

		ExecutorService executor = Executors.newFixedThreadPool(4);
		
		while (true) {
			System.out.println("===============================");
			System.out.println("Waiting for connections...");
			Socket clientSocket = server.accept();
			System.out.println("Connection accepted!");
			RequestHandler requestHandler = new RequestHandler(clientSocket, users, groups, conversations);
			executor.execute(requestHandler);
		}
	}
	
}
