package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import builders.RequestBuilder;
import domain.Reply;
import domain.Request;
import helpers.Connection;
import helpers.FilesHandler;
import security.CipheredMessage;
import security.KeyWrapper;
import security.MessageKey;
import security.SecUtils;
import validators.InputValidator;

/**
 * Esta classe representa o client
 * O modo de uso e as suas operacoes sao em mais detalhe descritas
 * no ficheiro README.info
 *
 * @author Joao Rodrigues && Simao Neves
 */
public class MyWhats {

	/**
	 * flag utilizada para identifica erro em operacao
	 * download e upload respectivamente
	 */
	private static boolean download_error = false;
	private static boolean upload_error = false;

	public static void main(String[] args) {

		try {
			// input validation
			if (!InputValidator.validInput(args)){
				System.out.println("Parametros mal formed");
				System.exit(-1);
			}
			
			Signature sig = Signature.getInstance("MD5WithRSA");
			
			// parse input
			HashMap<String, String> parsedInput = InputValidator.parseInput(args);

			//create keyStore if needed
			
			File ksFile = new File(parsedInput.get("username") +".keyStore");
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			Certificate cert = null;
			PrivateKey privateKey = null;
			if(!ksFile.exists())
			{
				//criar
				ks.load(null, parsedInput.get("password").toCharArray());
				KeyPair keyPair = SecUtils.generateKeyPair();
				cert = SecUtils.generateCertificate(parsedInput.get("username"), keyPair.getPublic(), keyPair.getPrivate());
				Certificate[] chain = new Certificate[1];
				chain[0] = cert;
				ks.setKeyEntry(parsedInput.get("username"), keyPair.getPrivate(), parsedInput.get("password").toCharArray(), chain);
				FileOutputStream fos = new FileOutputStream(parsedInput.get("username")+".keyStore");
				ks.store(fos, parsedInput.get("password").toCharArray());
				fos.close();
				
			}else{
				
				FileInputStream fis = new FileInputStream(ksFile);
				ks.load(fis, parsedInput.get("password").toCharArray());
				cert = ks.getCertificate(parsedInput.get("username"));
				privateKey = (PrivateKey) ks.getKey(parsedInput.get("username"), parsedInput.get("password").toCharArray());
			}
			
			// create request obj
			Request request = null;
			try {
				request = RequestBuilder.make(parsedInput);
				request.getUser().setCertificate(cert);
				request.getUser().setPrivateKey(privateKey);
			} catch (FileNotFoundException e) {
				System.out.println("Ficheiro não encontrado");
				System.exit(-1);
			}

			// validate request before send
			if (request.getUser().getName().equals(request.getContact()) && !request.getType().equals("-d")) {
				if (request.getType().equals("-m") || request.getType().equals("-f")) {
					System.out.println("O destinatário nao pode ser o remetente.");
					System.out.println("Aplicacao terminada.");
					System.exit(-1);
				} else if (request.getType().equals("-a")) {
					System.out.println("Não se pode adicionar a si próprio a um grupo.");
					System.out.println("Aplicacao terminada.");
					System.exit(-1);
				}
			}

			// estabelece ligacao
			System.setProperty("javax.net.ssl.trustStore", "certificates.trustStore");
			SocketFactory sf = SSLSocketFactory.getDefault();
			Socket socket = sf.createSocket(parsedInput.get("ip"), Integer.parseInt(parsedInput.get("port")));
			Connection connection = new Connection(socket);

			// send request
			sendRequest(connection, request);
			
			// get reply
			Reply reply = receiveReply(connection);
			
			// Imprime server reply
			reply.prettyPrint(request.getUser());

			// Fecha connection
			connection.destroy();

		} catch (Exception e) {
			System.out.println("Aplicação terminada com erro, tente de novo.");
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	/**
	 * This method handles a request sending
	 * 
	 * @param conn Connection to be considered
	 * @param req Request to be considered
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 * @throws NoSuchAlgorithmException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws InvalidKeyException 
	 * @throws BadPaddingException 
	 * @require conn != null && req != null && conn.getOutputStream != null
	 */
	public static void sendRequest(Connection conn, Request req) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		// send base request
		conn.getOutputStream().writeObject(req);
		
		//se eh para enviar message
		if(req.isMessage()){

			//Obtem certificados de users
			Reply reply = (Reply) conn.getInputStream().readObject();

			// Se tem erro
			if (reply.hasError()) {
				// mandar reply para cima
				// return reply;
			}

			Map<String, Certificate> members = reply.getCertificates();
			ArrayList<String> names =  new ArrayList<>(Arrays.asList((String[]) members.keySet().toArray()));
			ArrayList<Certificate> certificates = new ArrayList<>(Arrays.asList((Certificate[]) members.values().toArray()));

			//adiciona o certificado do current user e seu nome
			names.add(req.getUser().getName());
			certificates.add(req.getUser().getCertificate());

			//generate Simetric Key
			Key key = SecUtils.generateSymetricKey();

			//send key to all members including me
			for(int i = 0; i < names.size(); i++){
				KeyWrapper kw = new KeyWrapper(certificates.get(i).getPublicKey());
				kw.wrap(key);
				conn.getOutputStream()
				.writeObject(new MessageKey(names.get(i), kw.getWrappedKey()));
			}

			//send ciphered message
			Cipher c = Cipher.getInstance("DES");
			c.init(Cipher.ENCRYPT_MODE, key);
			byte[]msgStr = c.doFinal(req.getMessage().getBody().getBytes());
			CipheredMessage cm = new CipheredMessage(req.getContact(), msgStr);
			conn.getOutputStream().writeObject(cm);
		}

		//se a operacao inclui ficheiros
		if ( req.isFileOperation() ) {
			//obtem autorizacao para enviar/receber ficheiro
			try {
				Reply auth = (Reply) conn.getInputStream().readObject();
				//se nao autorizado
				if (auth.getStatus() != 200) {
					upload_error = true;
					return;
				}
			} catch (ClassNotFoundException e) {
				throw new IOException();
			}
		}

		//se a operacao inclui ficheiros e tem autorizacao
		switch (req.getType()) {
		//caso especial para upload
		case "-f":
			FilesHandler fHandler = new FilesHandler();
			try {
				//enviar file
				fHandler.send(conn, new File(req.getFile().getFullPath()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
			
		//caso especial para download
		case "-r":
			//se file download
			if (req.getSpecs().equals("download")) {
				try {
					File downloaded = new FilesHandler().receive(conn, ".", req.getFile().getFullPath());
					if (downloaded == null)
						System.out.println("Erro ao descarregar ficheiro");
					else
						System.out.println("Descarregamento efectuado");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Recebe um objecto Reply do servidor
	 * 
	 * @param conn Connection considerada na ligacao
	 * @return Reply com resposta
	 * @throws Exception
	 * @require conn != null
	 */
	public static Reply receiveReply(Connection conn) {
		//se ocorreu um erro ao executar -r contact file
		if (download_error)
			return new Reply(400, "Erro ao descarregar ficheiro");
		//se ocorreu um erro ao enviar file a contact
		else if (upload_error)
			return new Reply(400, "Erro ao enviar ficheiro");
		//caso contrario espera pela resposta
		else {
			try {
				return (Reply) conn.getInputStream().readObject();
			} catch (IOException e) {
				return new Reply(400, "");
			} catch (ClassNotFoundException e) {
				// e.printStackTrace();
				return new Reply(400, "");
			}
		}
	}
}