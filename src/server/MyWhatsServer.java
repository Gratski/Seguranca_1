package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.SecretKey;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import handlers.RequestHandler;
import helpers.DatabaseBuilder;
import helpers.FilesHandler;
import proxies.ConversationsProxy;
import proxies.GroupsProxy;
import proxies.Proxy;
import proxies.UsersProxy;
import security.MACService;
import security.PBEService;
import security.SecUtils;
import validators.InputValidator;

/**
 * Classe que contém o Servidor
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class MyWhatsServer {
	
	public static void main(String[] args) throws NumberFormatException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {

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
		SecretKey key = PBEService.getKeyByString("segredo");
		boolean isSecured = secureSystem(key);
		if(!isSecured)
		{
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
		
		System.setProperty("javax.net.ssl.keyStore", "myServer.keyStore"); 
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
	 * @param key
	 * @return
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 */
	private static boolean secureSystem(SecretKey key) throws InvalidKeyException, NoSuchAlgorithmException, IOException, InvalidKeySpecException{
		
		File f = new File(Proxy.getUsersIndex()+""+Proxy.getMacFileExtension());

		//se nao tem mac criado, cria
		if(!f.exists())
		{
			System.out.println("System is not secure yet.");
			System.out.println("Do you want to make it secure? Y/n");
			
			Scanner sc = new Scanner(System.in);
			String answer = sc.nextLine();
			if(!answer.equals("Y"))
				return false;
			
			System.out.println("Insert your password:");
			key = PBEService.getKeyByString(sc.nextLine());
			
			//criar todas os .mac files necessarios
			generateAllMacFiles(key);
			
		}
		//se jah existe
		else{
			return MACService.validateMAC(Proxy.getUsersIndex(), key);
		}
		
		return true;
	}
	
	private static void generateAllMacFiles(SecretKey key) throws InvalidKeyException, NoSuchAlgorithmException, IOException{
		
		//FILES
		File f = new File(Proxy.getUsersIndex());
		File fmac = new File(Proxy.getUsersIndex()+""+Proxy.getMacFileExtension());
		
		//gera mac de users file
		byte[] mac = MACService.generateFileMac(f, key);
		String macHexStr = SecUtils.getHexString(mac);
		BufferedWriter bw = new FilesHandler().getWriter(fmac);
		bw.write(macHexStr);
		bw.close();
		
		//gera mac de groups file
		f = new File(Proxy.getGroupsIndex());
		fmac = new File(Proxy.getGroupsIndex()+""+Proxy.getMacFileExtension());
		mac = MACService.generateFileMac(f, key);
		macHexStr = SecUtils.getHexString(mac);
		bw = new FilesHandler().getWriter(fmac);
		bw.write(macHexStr);
		bw.close();
		
	}
}