package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.SecretKey;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import handlers.RequestHandler;
import helpers.DatabaseBuilder;
import proxies.ConversationsProxy;
import proxies.GroupsProxy;
import proxies.Proxy;
import proxies.UsersProxy;
import security.MACService;
import security.SecUtils;
import validators.InputValidator;

/**
 * Classe que contém o Servidor
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class MyWhatsServer {
	
	public static void main(String[] args) throws NumberFormatException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, KeyStoreException, CertificateException {

		if (!InputValidator.validServerInput(args)) {
			System.out.println("Parâmetros mal formados");
			System.exit(-1);
		}
		
		//prepare file structure
		DatabaseBuilder dbBuilder = new DatabaseBuilder();
		boolean createdFolders = dbBuilder.make();
		if (!createdFolders)
			System.exit(-1);
		
		//valida seguranca de sistema
		SecretKey key;
		String pass = "";
		do {
			System.out.println("Insert your password:");
			Scanner sc = new Scanner(System.in);
			pass = sc.nextLine();
			if (pass.equals(""))
				System.out.println("Invalid password format!");
		} while (pass.equals(""));
		key = SecUtils.getKeyByString(pass);
		
		boolean isSecured = secureSystem(key);
		if (!isSecured) {
			System.out.println("System is not secured.");
			System.out.println("Shutting down.");
			return;
		}
		//obtain singletons
		GroupsProxy groups = GroupsProxy.getInstance();
		UsersProxy users = UsersProxy.getInstance();
		ConversationsProxy conversations = ConversationsProxy.getInstance();

		@SuppressWarnings("resource")
		int port = Integer.parseInt(args[0]);

		System.setProperty("javax.net.ssl.keyStore", "keys/server/server.keyStore");
		System.setProperty("javax.net.ssl.keyStorePassword", "segredo");
		ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
		ServerSocket server = ssf.createServerSocket(port);
		server.setReuseAddress(true);

		ExecutorService executor = Executors.newFixedThreadPool(4);
		
		while (true) {
			System.out.println("===============================");
			System.out.println("Waiting for connections...");
			Socket clientSocket = server.accept();
			System.out.println("Connection accepted!");
			RequestHandler requestHandler = new RequestHandler(key, clientSocket, users, groups, conversations);
			executor.execute(requestHandler);
		}
	}
	
	/**
	 * Faz validacoes base de seguranca
	 *
	 * @param key Secret key usada para gerar os MACs se preciso
	 * @return True se o sistema for considerado não corrompido/hackeado
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 */
	private static boolean secureSystem(SecretKey key) throws InvalidKeyException, NoSuchAlgorithmException, IOException, InvalidKeySpecException{
		
		File f = new File(Proxy.getUsersIndex() + Proxy.getMacFileExtension());

		//se nao tem mac criado, cria
		if (!f.exists()) {
			System.out.println("System is not secure yet.");
			System.out.println("Do you want to make it secure? Y/n");
			
			Scanner sc = new Scanner(System.in);
			String answer = sc.nextLine();
			if (!answer.equals("Y"))
				return false;

			//criar todas os .mac files necessarios
			MACService.generateAllMacFiles(key);
			System.out.println("System is secure!");
		}
		//se jah existe
		else {
			return MACService.validateMAC(Proxy.getUsersIndex(), key)
					&& MACService.validateMAC(Proxy.getGroupsIndex(), key);
		}
		return true;
	}

}