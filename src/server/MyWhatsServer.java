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

import handlers.RequestHandler;
import helpers.DatabaseBuilder;
import helpers.FilesHandler;
import proxies.ConversationsProxy;
import proxies.GroupsProxy;
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


		// TODO: ALTERAR NO FIM
		//valida seguranca de sistema
		SecretKey key;
		String pass = "segredo";
//		do {
//			System.out.println("Insert your password:");
//			Scanner sc = new Scanner(System.in);
//			pass = sc.nextLine();
//			if (pass.equals(""))
//				System.out.println("Invalid password format!");
//		} while (pass.equals(""));

		key = PBEService.getKeyByString(pass);
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
		
		File f = new File("DATABASE/USERS.mac");

		//se nao tem mac criado, cria
		if (!f.exists()) {
			System.out.println("System is not secure yet.");
			System.out.println("Do you want to make it secure? Y/n");
			
			Scanner sc = new Scanner(System.in);
			String answer = sc.nextLine();
			if (!answer.equals("Y"))
				return false;

			System.out.println("Using your password to calculate new MACs..");
			//criar todas os .mac files necessarios
			generateAllMacFiles(key);
			System.out.println("Done!");
		}
		//se jah existe
		else {
			
			byte[] curMac = MACService.readHashFromFile(f);
			return MACService.validateFileMac(new File("DATABASE/USERS"), key, curMac);
			
		}
		return true;
	}
	
	private static void generateAllMacFiles(SecretKey key) throws InvalidKeyException, NoSuchAlgorithmException, IOException{
		
		//FILES
		File f = new File("DATABASE/USERS");
		File fmac = new File("DATABASE/USERS.mac");
		
		//gera mac de users file
		byte[] mac = MACService.generateFileMac(f, key);
		String macHexStr = SecUtils.getHex(mac);
		BufferedWriter bw = new FilesHandler().getWriter(fmac);
		bw.write(macHexStr);
		bw.close();
		
		//gera mac de groups file
		f = new File("DATABASE/GROUPS");
		fmac = new File("DATABASE/GROUPS.mac");
		mac = MACService.generateFileMac(f, key);
		macHexStr = SecUtils.getHex(mac);
		bw = new FilesHandler().getWriter(fmac);
		bw.write(macHexStr);
		bw.close();
		
		//gera mac de public keys
		
	}
}