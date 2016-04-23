package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import builders.RequestBuilder;
import domain.Conversation;
import domain.Message;
import domain.NetworkMessage;
import domain.Reply;
import domain.Request;
import domain.User;
import helpers.Connection;
import helpers.FilesHandler;
import security.CipheredKey;
import security.GenericSignature;
import security.KeyWrapper;
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

			//obter certificado e private key
			File ksFile = new File("keys/clients/" + parsedInput.get("username") + ".keyStore");
			FileInputStream fis = new FileInputStream(ksFile);
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(fis, parsedInput.get("password").toCharArray());
			Certificate cert = ks.getCertificate(parsedInput.get("username")); 
			PrivateKey privateKey = (PrivateKey) ks.getKey(parsedInput.get("username"), parsedInput.get("password").toCharArray());
			
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
			System.setProperty("javax.net.ssl.trustStore", "keys/certificates.trustStore");
			SocketFactory sf = SSLSocketFactory.getDefault();
			Socket socket = sf.createSocket(parsedInput.get("ip"), Integer.parseInt(parsedInput.get("port")));
			Connection connection = new Connection(socket);

			// send request
			Reply reply = sendRequest(connection, request);
			
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
	 * @throws Exception 
	 * @require conn != null && req != null && conn.getOutputStream != null
	 */
	public static Reply sendRequest(Connection conn, Request req) throws Exception {
		// send base request
		conn.getOutputStream().writeObject(req);

		Reply reply = (Reply) conn.getInputStream().readObject();
		if (reply.hasError())
			return reply;

		switch (req.getType()) {
		case "-m":
			reply = executeSendMessage(conn, req, reply);
			break;
		case "-f":
			FilesHandler fHandler = new FilesHandler();
			boolean sent = fHandler.send(conn, reply.getNames(), 
					req.getUser(), new File(req.getFile().getFullPath()));
			if(!sent)
				reply = new Reply(400, "Erro ao enviar ficheiro.");
			else
				reply = (Reply) conn.getInputStream().readObject();
			break;
		case "-r":
			if (req.getSpecs().equals("download")) {
				File downloaded = new FilesHandler().receive(conn, ".", req.getFile().getFullPath());
				if (downloaded == null)
					reply = new Reply(400, "Erro ao descarregar ficheiro.");
				else
					reply = new Reply(200, "Ficheiro descarregado.");
			} else {
				reply = executeReceiveMessages(reply, conn, req);
			}
			
			break;
		}
		return reply;
	}

	
	private static Reply executeReceiveMessages(Reply reply, Connection conn, Request req) throws ClassNotFoundException, IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, CertificateException, KeyStoreException, SignatureException {
		
		//obtem private key de user
		PrivateKey privateKey = req.getUser().getPrivateKey();
		
		//obtem a lista de conversation
		ArrayList<Conversation> convs = reply.getConversations();
		int i = 0;
		int j = 0;

		//para cada conversation
		System.out.println("Numero de conversas: " + convs.size());
		for (Conversation conv : convs)
		{
			System.out.println("Numero de mensagens: " + conv.getMessages().size());
			for (Message msg : conv.getMessages())
			{
				//obtem key
				KeyWrapper kw = new KeyWrapper(msg.getKey());
				kw.unwrap(privateKey);
				Key curKey = kw.getKey();
				
				//decifra mensagem
				Cipher c = Cipher.getInstance("AES");
				c.init(Cipher.DECRYPT_MODE, curKey);
				c.update(SecUtils.getStringHex(msg.getBody()));
				String body = new String(c.doFinal());

				//obtem public key de sender
				ArrayList<String> names = new ArrayList<>();
				names.add(msg.getFrom());
				PublicKey publicKey = getCertificates(names, req.getUser()).get(msg.getFrom()).getPublicKey();
				
				//valida sintese
				MessageDigest md = GenericSignature.getMessageDigest();
				byte[] receivedHash = md.digest(body.getBytes());
				Signature signature = Signature.getInstance("SHA256withRSA");
				signature.initVerify(publicKey);
				signature.update(receivedHash);
				boolean valid = signature.verify(msg.getSignature().getSignature());

				msg.setBody(body);
				if (valid) {
					System.out.println("SINTESE BOA!");
				}
				else {
//					convs.get(i).getMessages().remove(j);
					System.out.println("SINTESE INVALIDA!");
				}
				
				j++;
			}
			i++;
		}
		reply.setConversations(convs);
		return reply;
	}

	/**
	 * Envia uma mensagem para o servidor
	 * @param conn, conexao a base de dados
	 * @param req, request a considerar
	 * @param reply, reply que obteve inicialmente
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchPaddingException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws CertificateException
	 * @throws KeyStoreException
	 */
	private static Reply executeSendMessage(Connection conn, Request req, Reply reply) 
			throws ClassNotFoundException, IOException, IllegalBlockSizeException, 
			NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, 
			InvalidKeyException, SignatureException, CertificateException, 
			KeyStoreException {
		
		//receber contact list de server ou error
		//TODO -> Substituir por get 
		ArrayList<String> names = reply.getNames();
		Map<String, Certificate> members = getCertificates(names, req.getUser());
		
		//assinar e enviar assinatura de mensagem a enviar
		GenericSignature gs = GenericSignature.createGenericMessageSignature(
				req.getUser().getPrivateKey(), req.getMessage().getBody().getBytes());

		NetworkMessage netMessage = new NetworkMessage();
		netMessage.setSignature(gs);
		conn.getOutputStream().writeObject(netMessage);

		// Verifica se está tudo bem
		reply = (Reply) conn.getInputStream().readObject();
		if (reply.hasError())
			return reply;
		
		//enviar mensagem cifrada com K
		Key key = SecUtils.generateSymetricKey();
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, key);
		
		System.out.println("BYTES: " + req.getMessage().getBody().getBytes().length);
		
		////////////////////////////////////////////////////////////////
		// AQUI EH O PROB
		byte[] original = req.getMessage().getBody().getBytes();
		int reps = (int) original.length / 16;
		int extra = original.length % 16;
		byte[] cipheredMsg;
		if(extra != 0)
			cipheredMsg = new byte[original.length + 16];
		else
			cipheredMsg = new byte[16];
		int offset = 0;
		for(int i = 0; i < reps; i++)
		{
			System.arraycopy(c.doFinal(original, offset, 16), 0, cipheredMsg, offset, 16);
			offset += 16;
		}
		
		if(extra != 0)
			System.arraycopy(c.doFinal(original, offset, extra), 0, cipheredMsg, offset, extra);
		
		System.out.println("CIPHERED BYTES: " + cipheredMsg.length);
		// ATE AQUI
		///////////////////////////////////////////////////////////////////
		
		
		Message cm = new Message(req.getUser().getName(), req.getContact(), SecUtils.getHexString(cipheredMsg));
		cm.setType("-t");
		conn.getOutputStream().writeObject(cm);

		reply = (Reply) conn.getInputStream().readObject();
		if (reply.hasError())
			return reply;
		
		// Coloca-se a si também na lista
		members.put(req.getUser().getName(), req.getUser().getCertificate());

		//cifrar K para cada user com a sua publica
		Map<String, CipheredKey> keys = cipherAllKeys(members, key);

		//envia chave cifrada com com chaves publicas para o servidor
		conn.getOutputStream().writeObject(keys);

		reply = (Reply) conn.getInputStream().readObject();
		return reply;
	}
	
	/**
	 * Cifra uma chave com varias chaves publicas
	 * @param members, membros para quem a chave key eh cifrada
	 * @param key, chave a cifrar
	 * @return lista com chaves cifradas
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 */
	public static Map<String, CipheredKey> cipherAllKeys(Map<String, Certificate> members, Key key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException{
		Map<String, CipheredKey> keys = new HashMap<>();
		Set<String> set = members.keySet();
		String[] names = set.toArray(new String[set.size()]);

		KeyWrapper kw = null;
		for (int i = 0; i < names.length; i++) {
			System.out.println("Cifrar K com publica de: " + names[i]);
			String to = names[i];
			Certificate cert = members.get(to);

			kw = new KeyWrapper(cert.getPublicKey());
			kw.wrap(key);
			CipheredKey ck = new CipheredKey(to, kw.getWrappedKey());
			
			keys.put(names[i], ck);
		}
		return keys;
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
	
	/**
	 * Obtem o certificado de utilizadores dada uma lista de nomes
	 * @param aliases, nomes dos utilizadores
	 * @param user, user de session
	 * @return lista com nomes e certificados
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws KeyStoreException
	 */
	public static Map<String, Certificate> getCertificates(ArrayList<String> aliases, User user) throws NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, KeyStoreException{
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(new FileInputStream("keys/clients/" + user.getName() + ".keyStore"), new String(user.getPassword()).toCharArray());
		
		Map<String, Certificate> certs = new HashMap<String, Certificate>();
		for(int i = 0; i < aliases.size(); i++)
		{
			Certificate cert = ks.getCertificate(aliases.get(i));
			certs.put(aliases.get(i), cert);
		}
		
		Certificate cert = ks.getCertificate(user.getName());
		certs.put(user.getName(), cert);
		return certs;
	}
	
}