package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
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
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
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
import security.CipherFactory;
import security.CipheredKey;
import security.GenericSignature;
import security.HashService;
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
		
		// get base reply
		Reply reply = (Reply) conn.getInputStream().readObject();
		if (reply.hasError())
			return reply;

		// parse de casos que necessitam operacoes adicionais
		switch (req.getType()) {
		//upload mensagem
		case "-m":
			reply = executeSendMessage(conn, req, reply);
			break;
		//upload ficheiro
		case "-f":
			reply = executeSendFile(conn, req, reply);
			break;
		//download
		case "-r":
			//se eh para download de ficheiro
			if (req.getSpecs().equals("download")) {
				reply = executeReceiveFile(conn, req, reply);
			} 
			//se eh para download de mensagens
			else {
				reply = executeReceiveMessages(reply, req);
			}
			
			break;
		}
		return reply;
	}
	
	
	/**
	 * Recebe mensagens do servidor de um dado utilizador ou grupo
	 * @param reply, reply de request inicial
	 * @param req, request a ser feito
	 * @return reply de processo de envio de recepcao de mensagens
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws CertificateException
	 * @throws KeyStoreException
	 * @throws SignatureException
	 */
	private static Reply executeReceiveMessages(Reply reply, Request req) throws ClassNotFoundException, IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, CertificateException, KeyStoreException, SignatureException {
		
		//obtem private key de user
		PrivateKey privateKey = req.getUser().getPrivateKey();
		
		//obtem a lista de conversation
		ArrayList<Conversation> convs = reply.getConversations();
		int i = 0;
		int j = 0;

		//para cada conversation
//		System.out.println("Numero de conversas: " + convs.size());
		for (Conversation conv : convs)
		{
//			System.out.println("Numero de mensagens: " + conv.getMessages().size());
			for (Message msg : conv.getMessages())
			{
				//obtem key
				KeyWrapper kw = new KeyWrapper(msg.getKey());
				kw.unwrap(privateKey);
				Key curKey = kw.getKey();
				
//				System.out.println("*******************************");
				byte[] cipheredBytes = SecUtils.getStringHex(msg.getBody());
//				System.out.println("Ciphered Bytes: " + cipheredBytes.length);

				//decifra mensagem
				Cipher c = Cipher.getInstance("AES");
				c.init(Cipher.DECRYPT_MODE, curKey);
				String body = new String(c.doFinal(cipheredBytes));

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
//					System.out.println("SINTESE BOA!");
				}
				else {
//					convs.get(i).getMessages().remove(j);
//					System.out.println("SINTESE INVALIDA!");
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
	 * @throws ShortBufferException 
	 */
	private static Reply executeSendMessage(Connection conn, Request req, Reply reply) 
			throws ClassNotFoundException, IOException, IllegalBlockSizeException, 
			NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, 
			InvalidKeyException, SignatureException, CertificateException, 
			KeyStoreException, ShortBufferException {
		
		//receber contact list de server ou error 
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
		
		String originalMsg = req.getMessage().getBody();
		byte[] originalBytes = originalMsg.getBytes();
		System.out.println("==========================================");
		System.out.println("Original Message: " + originalMsg);
		System.out.println("Original Bytes: " + originalBytes.length);
		byte[] ciphered = c.doFinal(originalBytes);
		String cipheredStr = SecUtils.getHexString(ciphered);
		System.out.println("Ciphered Bytes: " + ciphered.length);
		
		System.out.println("Decifrar para confirmar");
		c.init(Cipher.DECRYPT_MODE, key);
		//byte[] deciphered = c.doFinal(ciphered);
		byte[] deciphered = c.doFinal(SecUtils.getStringHex(cipheredStr)); 
		System.out.println("Deciphered Bytes: " + deciphered.length);
		System.out.println("Deciphered Message: " + new String(deciphered));
		System.out.println("==========================================");
		
		Message cm = new Message(req.getUser().getName(), req.getContact(), cipheredStr);
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
	 * Enviar ficheiro para servidor
	 * @param conn, connection a ser utilizada
	 * @param req, request a ser feito
	 * @param reply, reply de request inicial ao server
	 * @return reply do processo de envio de ficheiro para server
	 * 
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws SignatureException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws KeyStoreException
	 * @throws ClassNotFoundException
	 * @throws ShortBufferException 
	 */
	private static Reply executeSendFile(Connection conn, Request req, Reply reply) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, SignatureException, IOException, CertificateException, KeyStoreException, ClassNotFoundException, ShortBufferException{
		
		// obter chave privada
		PrivateKey privateKey = req.getUser().getPrivateKey();
		
		// obter lista de membros de conversa, obtem seus certificados
		ArrayList<String> members = reply.getNames();
		Map<String, Certificate> certs = getCertificates(members, req.getUser());
		
		// gerar assinatura digital, enviar e receber resposta
		File file = new File(req.getFile().getFullPath());
		GenericSignature gs = GenericSignature.createGenericFileSignature(privateKey, file);
		conn.getOutputStream().writeObject(gs);
		
		// gerar chave simetrica K
		Key key = SecUtils.generateSymetricKey();
		
		// preparar file size de acordo com cipher
		long fileSize = file.length();
	
		// envia size ja com padding
		conn.getOutputStream().writeLong(fileSize);
		
		// cifra ficheiro, envia
		Cipher c = Cipher.getInstance("AES/CFB8/NoPadding");
		c.init(Cipher.ENCRYPT_MODE, key);
		
		// envia IV
		Cipher cIV = CipherFactory.getStandardCipher();
		cIV.init(Cipher.ENCRYPT_MODE, key);
		byte[] IV = c.getIV();
		byte[] IVCiphered = cIV.doFinal(IV);
		conn.getOutputStream().writeObject(SecUtils.getHexString(IVCiphered));
		
		// envia ficheiro
		byte[] buf = new byte[16];
		int sent = 0;
		long totalSent = 0;
		FileInputStream fis = new FileInputStream(file);
		CipherOutputStream cos = new CipherOutputStream(conn.getOutputStream(), c);
		
		while((sent = fis.read(buf))!=-1){
			cos.write(buf, 0, sent);
			System.out.println("ENVIOU BYTES: " + sent);
			totalSent += sent;
		}
		cos.flush();
		System.out.println("ENVIOU: " + totalSent);
		
		//cifrar K com chaves publicas dos membros, enviar e receber resposta
		Map<String, CipheredKey> keys = cipherAllKeys(certs, key);
		conn.getOutputStream().writeObject(keys);
		
		reply = (Reply) conn.getInputStream().readObject();
		return reply;
	}

	/**
	 * Receber ficheiro de servidor
	 * @param conn, connection a utilizar
	 * @param req, request a ser feito ao servidor
	 * @param reply, reply inicial de servidor
	 * @return reply de processo de download
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws ClassNotFoundException
	 * @throws SignatureException
	 * @throws CertificateException
	 * @throws KeyStoreException
	 * @throws InvalidAlgorithmParameterException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 */
	private static Reply executeReceiveFile(Connection conn, Request req, Reply reply) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, ClassNotFoundException, SignatureException, CertificateException, KeyStoreException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
		
		// obtem autor de upload de ficheiro
		String uploader = reply.getUploader();
		ArrayList<String> names = new ArrayList<>();
		names.add(uploader);
		
		// chave publica de author de upload para utilizar em decifrar assinatura
		Map<String, Certificate> certs = getCertificates(names, req.getUser());
		PublicKey publicKey = certs.get(uploader).getPublicKey();
		
		// chave privada de utilizador de session
		PrivateKey privateKey = req.getUser().getPrivateKey();
		
		// obtem size de ficheiro
		long fileSize = conn.getInputStream().readLong();
		
		// se ficheiro nao existe, cria novo
		File file = new File("DOWNLOADS/"+req.getFile().getFullPath());
		if(!file.exists())
			file.createNewFile();
		
		// obtem chave K
		CipheredKey cipheredKey = (CipheredKey) conn.getInputStream().readObject();
		KeyWrapper kw = new KeyWrapper(cipheredKey.getKey());
		kw.unwrap(privateKey);
		Key key = kw.getKey();
		
		// recebe IV
		String IVHex = (String) conn.getInputStream().readObject();
		Cipher cIV = Cipher.getInstance("AES");
		cIV.init(Cipher.DECRYPT_MODE, key);
		byte[] clearIV = cIV.doFinal(SecUtils.getStringHex(IVHex));
		
		// recebe ficheiro cifrado e decifra
		FileOutputStream fos = new FileOutputStream(file);
		Cipher c = Cipher.getInstance("AES/CFB8/NoPadding");
		c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(clearIV));
		CipherInputStream cis = new CipherInputStream(conn.getInputStream(), c);
		
		int read = 0;
		byte[] buf = new byte[16];
		while((read = cis.read(buf)) != -1)
			fos.write(buf, 0, read);
		fos.close();
		
		// recebe assinatura de ficheiro	
		GenericSignature gs = (GenericSignature) conn.getInputStream().readObject();
		
		//gera sintese de ficheiro
		byte[] receivedHash = HashService.createFileHash(file);
		
		//verifica assinatura do ficheiro
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initVerify(publicKey);
		signature.update(receivedHash);
		boolean valid = signature.verify(gs.getSignature());
		
		if(valid){
			reply = new Reply(200);
			System.out.println("RECEBIDO OK!");
		}
		else{
			reply = new Reply(400);
			System.out.println("RECEBIDO NOT OK!");
		}
		
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