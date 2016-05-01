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
 * This class represents a simple client of MyWhat Application
 * It's usage is totally described on README.info file
 *
 * @author Joao Rodrigues && Simao Neves
 */
public class MyWhats {

	public static void main(String[] args) {

		try {
			// input validation
			if (!InputValidator.validInput(args)){
				System.out.println("Parametros mal formed");
				System.exit(-1);
			}
			
			// parse input
			HashMap<String, String> parsedInput = InputValidator.parseInput(args);

			// obtain session user's private and public key
			File ksFile = new File("keys/clients/" + parsedInput.get("username") + ".keyStore");
			FileInputStream fis = new FileInputStream(ksFile);
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			try {
				ks.load(fis, parsedInput.get("password").toCharArray());
			} catch (Exception e) {
				System.out.println("Password incorrecta para utilizador: " + parsedInput.get("username"));
				System.out.println("Impossível abrir keystore");
				System.exit(-1);
			}
			Certificate cert = ks.getCertificate(parsedInput.get("username"));
			PrivateKey privateKey = (PrivateKey) ks.getKey(parsedInput.get("username"), parsedInput.get("password").toCharArray());
			
			// create request object
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

			// establish connection
			System.setProperty("javax.net.ssl.trustStore", "keys/certificates.trustStore");
			SocketFactory sf = SSLSocketFactory.getDefault();
			Socket socket = sf.createSocket(parsedInput.get("ip"), Integer.parseInt(parsedInput.get("port")));
			Connection connection = new Connection(socket);

			// send request
			Reply reply = sendRequest(connection, request);
			
			// print server reply
			reply.prettyPrint(request.getUser());

			// close connection
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

		// parse aditional parameter options
		switch (req.getType()) {
		
		// plain message upload
		case "-m":
			reply = executeSendMessage(conn, req, reply);
			break;
			
		// file upload
		case "-f":
			reply = executeSendFile(conn, req, reply);
			break;
			
		// download
		case "-r":
			
			// if file download
			if (req.getSpecs().equals("download")) {
				reply = executeReceiveFile(conn, req, reply);
			} 
			// if message download
			else
				reply = executeReceiveMessages(reply, req);

			break;
		}
		return reply;
	}
	
	
	/**
	 * Receive conversation of a private or group conversation
	 *
	 * @param reply, initial request's reply
	 * @param req, request to be considered
	 * @return Reply with operation result
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
		
		// obtain session user private key
		PrivateKey privateKey = req.getUser().getPrivateKey();
		
		// obtain conversations list
		ArrayList<Conversation> convs = reply.getConversations();

		// parse each conversation
		for (Conversation conv : convs)
		{
			// parse each message
			for (Message msg : conv.getMessages())
			{
				// get symmetric key
				KeyWrapper kw = new KeyWrapper(msg.getKey());
				kw.unwrap(privateKey);
				Key curKey = kw.getKey();
				

				// decrypt message body
				byte[] cipheredBytes = SecUtils.getStringHex(msg.getBody());
				Cipher c = Cipher.getInstance("AES");
				c.init(Cipher.DECRYPT_MODE, curKey);
				String body = new String(c.doFinal(cipheredBytes));

				// get sender's public key
				ArrayList<String> names = new ArrayList<>();
				names.add(msg.getFrom());
				PublicKey publicKey = getCertificates(names, req.getUser()).get(msg.getFrom()).getPublicKey();
				
				// validate message hash
				MessageDigest md = GenericSignature.getMessageDigest();
				byte[] receivedHash = md.digest(body.getBytes());
				Signature signature = Signature.getInstance("SHA256withRSA");
				signature.initVerify(publicKey);
				signature.update(receivedHash);
				boolean valid = signature.verify(msg.getSignature().getSignature());

				if (valid) {
					// if hash is valid
					msg.setBody(body);
				} else {
					// if hash is not valid
					msg.setBody("{(Mensagem corrompida)}");
					System.out.println("Foi encontrada uma mensagem corrompida");
				}
			}
		}
		reply.setConversations(convs);
		return reply;
	}

	/**
	 * Sends a plain message to server
	 * 
	 * @param conn, server connection to be used
	 * @param req, Request object to be considered
	 * @param reply, Initially obtained reply
	 * @return Reply object with operation result
	 * 
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
		
		// get contact names list, because it can also be a group
		ArrayList<String> names = reply.getNames();
		Map<String, Certificate> members = getCertificates(names, req.getUser());
		
		// sign message and sends generated signature
		GenericSignature gs = GenericSignature.createGenericMessageSignature(
				req.getUser().getPrivateKey(), req.getMessage().getBody().getBytes());
		NetworkMessage netMessage = new NetworkMessage();
		netMessage.setSignature(gs);
		conn.getOutputStream().writeObject(netMessage);

		// check if server is doing alright
		reply = (Reply) conn.getInputStream().readObject();
		if (reply.hasError())
			return reply;
		
		// generate symmetric key K to be used when encrypting
		Key key = SecUtils.generateSymetricKey();
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, key);
		
		// encrypt message body
		String originalMsg = req.getMessage().getBody();
		byte[] originalBytes = originalMsg.getBytes();
		byte[] ciphered = c.doFinal(originalBytes);
		String cipheredStr = SecUtils.getHexString(ciphered);

		// prepare message to be sent with encrypted message
		Message cm = new Message(req.getUser().getName(), req.getContact(), cipheredStr);
		cm.setType("-t");
		conn.getOutputStream().writeObject(cm);
		
		// check if server is doing alright
		reply = (Reply) conn.getInputStream().readObject();
		if (reply.hasError())
			return reply;
		
		// put session user inside names list
		members.put(req.getUser().getName(), req.getUser().getCertificate());

		// encrypt K with all conversation members public key
		// so they are the only ones who can decrypt it correctly
		Map<String, CipheredKey> keys = cipherAllKeys(members, key);

		// send all encrypted keys to server
		conn.getOutputStream().writeObject(keys);
		reply = (Reply) conn.getInputStream().readObject();
		if(reply.hasError())
			return reply;
		
		// receive server's final reply
		return (Reply) conn.getInputStream().readObject();
	}

	/**
	 * Upload a file to the server
	 * 
	 * @param conn, server connection to be used
	 * @param req, Request to be done
	 * @param reply, Initial request reply
	 * @return Reply object with operation result
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
	private static Reply executeSendFile(Connection conn, Request req, Reply reply) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, SignatureException, IOException, CertificateException, KeyStoreException, ClassNotFoundException, ShortBufferException {

		// get session user private key
		PrivateKey privateKey = req.getUser().getPrivateKey();
		
		// get contact names list, because it can also be a group
		ArrayList<String> members = reply.getNames();
		Map<String, Certificate> certs = getCertificates(members, req.getUser());
		
		// send file signature
		File file = new File(req.getFile().getFullPath());
		GenericSignature gs = GenericSignature.createGenericFileSignature(privateKey, file);
		conn.getOutputStream().writeObject(gs);

		// generate symmetric key K
		Key key = SecUtils.generateSymetricKey();

		// initialize Cipher with AES and K key
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, key);

		// Gerar mensagem e enviar
		String originalMsg = req.getFile().getFullPath();
		byte[] originalBytes = originalMsg.getBytes();
		byte[] ciphered = c.doFinal(originalBytes);
		String cipheredStr = SecUtils.getHexString(ciphered);
		gs = GenericSignature.createGenericMessageSignature(
				req.getUser().getPrivateKey(), originalBytes);

		// sends message for presentation
		Message cm = new Message(req.getUser().getName(), req.getContact(), cipheredStr);
		cm.setSignature(gs);
		cm.setType("-f");
		conn.getOutputStream().writeObject(cm);
		
		// get file size
		long fileSize = file.length();
	
		// send file size to server
		conn.getOutputStream().writeLong(fileSize);
		
		// prepared Cipher object to file encryption
		c = Cipher.getInstance("AES/CFB8/NoPadding");
		c.init(Cipher.ENCRYPT_MODE, key);
		
		// send used encryption initialization vector (IV)
		Cipher cIV = CipherFactory.getStandardCipher();
		cIV.init(Cipher.ENCRYPT_MODE, key);
		byte[] IV = c.getIV();
		byte[] IVCiphered = cIV.doFinal(IV);
		conn.getOutputStream().writeObject(SecUtils.getHexString(IVCiphered));
		
		// encrypt file and send it to server
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
		
		// encrypt K with all conversation members public key
		// so they are the only ones who can decrypt it correctly
		Map<String, CipheredKey> keys = cipherAllKeys(certs, key);
		conn.getOutputStream().writeObject(keys);
		
		// get final server reply
		reply = (Reply) conn.getInputStream().readObject();
		return reply;
	}

	/**
	 * Receive file from server
	 * Acts like a regular secure download
	 * 
	 * @param conn, server connection to be used
	 * @param req, Request to be considered
	 * @param reply, initial server reply
	 * @return Reply object with operation result
	 * 
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
		
		// get upload author
		String uploader = reply.getUploader();
		ArrayList<String> names = new ArrayList<>();
		names.add(uploader);
		
		// get author's public key in order to verify signature
		Map<String, Certificate> certs = getCertificates(names, req.getUser());
		PublicKey publicKey = certs.get(uploader).getPublicKey();
		
		// get session user private key
		PrivateKey privateKey = req.getUser().getPrivateKey();
		
		// get file size
		long fileSize = conn.getInputStream().readLong();
		
		// create a downloads folder if doesn't exist
		File file = new File("DOWNLOADS/");
		if (!file.exists())
			file.mkdir();
		file = new File("DOWNLOADS/" + req.getFile().getFullPath());
		if (!file.exists())
				file.createNewFile();
		
		// get used symmetric key
		CipheredKey cipheredKey = (CipheredKey) conn.getInputStream().readObject();
		KeyWrapper kw = new KeyWrapper(cipheredKey.getKey());
		kw.unwrap(privateKey);
		Key key = kw.getKey();
		
		// get used initialization vector
		String IVHex = (String) conn.getInputStream().readObject();
		Cipher cIV = Cipher.getInstance("AES");
		cIV.init(Cipher.DECRYPT_MODE, key);
		byte[] clearIV = cIV.doFinal(SecUtils.getStringHex(IVHex));
		
		// initialize Cipher object in order to decrypt file
		FileOutputStream fos = new FileOutputStream(file);
		Cipher c = Cipher.getInstance("AES/CFB8/NoPadding");
		c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(clearIV));
		CipherInputStream cis = new CipherInputStream(conn.getInputStream(), c);
		
		int read = 0;
		byte[] buf = new byte[16];
		while((read = cis.read(buf)) != -1)
			fos.write(buf, 0, read);
		fos.close();
		
		// get file signature	
		GenericSignature gs = (GenericSignature) conn.getInputStream().readObject();
		
		// create file hash
		byte[] receivedHash = HashService.createFileHash(file);
		
		// validate file signature according to previously created hash
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initVerify(publicKey);
		signature.update(receivedHash);
		boolean valid = signature.verify(gs.getSignature());

		// if unverified signature delete downloaded file
		if (!valid) {
			file.delete();
			reply = new Reply(400, "Ficheiro corrompido.");
		}
		// if valid store file
		else
			reply = new Reply(200, "Ficheiro guardado em DOWNLOADS/" + file.getName());

		return reply;
	}
	
	/**
	 * Cipher a list of keys
	 * 
	 * @param members, user names to cipher key
	 * @param key, key to be ciphered
	 * @return list of ciphered keys
	 * 
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 */
	public static Map<String, CipheredKey> cipherAllKeys(Map<String, Certificate> members, Key key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException{
		
		// key map
		Map<String, CipheredKey> keys = new HashMap<>();
		Set<String> set = members.keySet();
		String[] names = set.toArray(new String[set.size()]);

		// cipher all keys
		KeyWrapper kw = null;
		for (int i = 0; i < names.length; i++) {
			
			// name of user to get certificate
			String to = names[i];
			Certificate cert = members.get(to);
			
			// cipher key with user public key
			kw = new KeyWrapper(cert.getPublicKey());
			kw.wrap(key);
			
			// insert ciphered key inside keys
			CipheredKey ck = new CipheredKey(to, kw.getWrappedKey());
			keys.put(names[i], ck);
			
		}
		return keys;
	}
	
	/**
	 * Get user cetificates based on a list of names
	 * 
	 * @param aliases, user names
	 * @param user, session user
	 * @return a Map with names and certificates
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws KeyStoreException
	 */
	public static Map<String, Certificate> getCertificates(ArrayList<String> aliases, User user) throws NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, KeyStoreException{
		
		// get keystore object
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		
		// get session user keystore
		ks.load(new FileInputStream("keys/clients/" + user.getName() + ".keyStore"), new String(user.getPassword()).toCharArray());
		
		// create certificates map
		Map<String, Certificate> certs = new HashMap<String, Certificate>();
		for (int i = 0; i < aliases.size(); i++) {
			// obtain user certificate
			Certificate cert = ks.getCertificate(aliases.get(i));
			certs.put(aliases.get(i), cert);
		}
		
		// insert session user certificate too
		Certificate cert = ks.getCertificate(user.getName());
		certs.put(user.getName(), cert);
		
		return certs;
	}
}