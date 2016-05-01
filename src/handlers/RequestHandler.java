package handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import domain.Conversation;
import domain.Group;
import domain.Message;
import domain.NetworkMessage;
import domain.Reply;
import domain.Request;
import domain.User;
import exception.ApplicationException;
import helpers.Connection;
import proxies.ConversationsProxy;
import proxies.GroupsProxy;
import proxies.Proxy;
import proxies.UsersProxy;
import security.CipheredKey;
import security.GenericSignature;
import security.MACService;
import security.SecUtils;

/**
 * This class represents a server request handler
 * It has the responsibility of handling a server request
 * based on it's request type
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class RequestHandler extends Thread {
	
	/**
	 * connection used to comunicate with client
	 */
	private Connection connection;
	
	/**
	 * users proxy
	 */
	private UsersProxy userProxy;
	
	/**
	 * groups proxy
	 */
	private GroupsProxy groupsProxy;
	
	/**
	 * conversations proxy
	 */
	private ConversationsProxy convProxy;
	
	/**
	 * 
	 */
	private SecretKey key;
	
	/**
	 * Constructor
	 *
	 * @param clientSocket 	client socket to be used
	 * @param userProxy		users proxy to be used
	 * @param groupsProxy	groups proxy to be used
	 * @param conversationsProxy	conversations proxy to be used
	 *
     * @throws IOException
     */
	public RequestHandler(SecretKey key, Socket clientSocket, UsersProxy userProxy, GroupsProxy groupsProxy,
			ConversationsProxy conversationsProxy) throws IOException {
		this.connection = new Connection(clientSocket);
		this.userProxy = userProxy;
		this.groupsProxy = groupsProxy;
		this.convProxy = conversationsProxy;
		this.key = key;
	}

	/**
	 * Thread run method
	 * Executes a received request
	 */
	public void run() {
		
		// declare request and reply variables
		Request clientRequest = null;
		Reply reply = null;

		// get client request
		// in this case we are not considering any time out period
		// but it would improve server's performance
		try {
			clientRequest = (Request) this.connection.getInputStream().readObject();
		} catch(Exception e) {
			try {
				this.connection.destroy();
			} catch(Exception ex) {
				this.interrupt();
			}
			this.interrupt();
		}

		// request handling
		try {
			// parse request and execute it
			reply = parseRequest(clientRequest, key);

		} catch(SecurityException e) {
			reply = new Reply(400, "The server may have been hacked");
		} catch( ApplicationException e){
			reply = new Reply(400, e.getMessage());
		}catch (Exception e) {
			reply = new Reply(400, "Something went wrong executing your request");
		}

		// sends final request execution reply to client
		try {
			this.connection.getOutputStream().writeObject(reply);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// closes the connection
		try {
			this.connection.destroy();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Parses a given request and forwards it to
	 * the corresponding method in order to be
	 * executed
	 * 
	 * @param req 	Request to be considered
	 * @return 		Reply object with operation result
	 * @throws Exception 
	 * @require req != null
	 */
	Reply parseRequest(Request req, SecretKey key) throws Exception {
		
		// checks server files integrity
		if(!MACService.validateMAC(Proxy.getGroupsIndex(), key)
				|| !MACService.validateMAC(Proxy.getUsersIndex(), key))
			throw new SecurityException("Server has been hacked");
		
		// authenticate user if exists
		// if it doesn't exist yet, then create a new one
		if (!validateUser(req.getUser(), key))
			throw new ApplicationException("User nao autenticado");

		// executes this request
		return executeRequest(req, key);
	}

	/**
	 * Executes a request based on it's request type
	 * Request type can be:
	 * -a, if a user who is group owner wants to add another user to a given Group
	 * -d, if a users is a group owner and wants to remove a given user from a certain Group
	 * -m, if a user wants to send a new message to a Group or User
	 * -f, if a user wants to upload a file to be shared with a Group or a User
	 * -r, if a user wants to retrieve data from server
	 *
	 * @param req 	Request to be considered
	 * @return 		Reply object with request execution final result
	 * @throws Exception 
	 * @require req != null
     */
	private Reply executeRequest(Request req, SecretKey key) throws ApplicationException, SecurityException, Exception {
		Reply reply = new Reply();
		
		// handle request based on it's type
		switch (req.getType()) {
		
		// add user to a group
		case "-a":			
			// this validation is also been done by MyWhats client
			// as this is a secure service we decided to keep this
			// just to double check it on request arrival
			if (req.getUser().getName().equals(req.getContact()))
				throw new ApplicationException("Não se pode adicionar a si próprio a um grupo.");
		
			synchronized (groupsProxy) {
				reply = addUserToGroup(req.getGroup(), req.getUser(), req.getContact(), key);
			}
			break;
			
		// removing a user from a group
		case "-d":
			
			synchronized (groupsProxy) {
				reply = removeUserFromGroup(req.getGroup(), req.getUser(), req.getContact());
			}
			break;
			
		// upload a ciphered file
		case "-f":
			
			// if message receiver doesn't exist
			if ( !(this.userProxy.exists(new User(req.getContact())) || this.groupsProxy.exists(req.getContact())) )
				throw new ApplicationException("Destinatário inexistente");
				
			// execute receive file
			reply = executeReceiveFile(req);
			break;
			
		// upload a ciphered text message
		case "-m":
			synchronized (groupsProxy) {
				req.getMessage().setType("-t");
				req.getMessage().setTimestampNow();
				reply = executeReceiveMessage(req);
			}
			break;
			
		// download data
		case "-r":

			// download execution based on what user really wants
			switch (req.getSpecs()) {
			
			// if request is only to obtain a single conversation
			case "single_contact":
				reply = getConversation(req);
				break;
				
			// if request is to obtain all last received messages
			case "all":
				reply = getLastMessageFromConversations(req);
				break;
				
			// if request is for a file download
			case "download":
				reply = executeSendFile(req);
				break;
			
			// if download specification was not recognized
			default:
				throw new ApplicationException("Comando invalido");
			}
			break;
			
		// if request operation was not recognized
		default:
			throw new ApplicationException("Comando invalido");
		}
		return reply;
	}

	/**
	 * Sends a file through a given socket stream
	 *
	 * @param req Request object that contains request specifications
	 * @return Reply object that contains operation's final result
	 * 
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
	private Reply executeSendFile(Request req) throws ApplicationException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		
		Reply reply = new Reply();
		
		// checks if the conversation exists
		String path = this.convProxy.userHasConversationWith(req.getUser().getName(), req.getContact());
		if(path == null)
			throw new ApplicationException("Não existem conversas entre " + req.getUser().getName() + " e " + req.getContact());

		
		File file = new File(path + "/" + Proxy.getFilesFolder() + req.getFile().getFullPath() + "/");
		if (!file.exists())
			throw new ApplicationException("Ficheiro inexistente");
		
		// Verifica se quem pediu o ficheiro o pode receber
		file = new File(path + "/" + Proxy.getFilesFolder() + req.getFile().getFullPath() + "/" + req.getUser().getName() + ".key");
		if (!file.exists())
			throw new ApplicationException("Ficheiro não pode ser acedido");

		// obter author de upload
		file = new File(path + "/" + Proxy.getFilesFolder() + req.getFile().getFullPath() + "/author");
		if (!file.exists())
			throw new ApplicationException("Ficheiro de author missing");

		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String uploader = br.readLine();
		br.close();
		
		//enviar uploader
		System.out.println("Uploader was: " + uploader);
		reply.setStatus(200);
		reply.setUploader(uploader);
		this.connection.getOutputStream().writeObject(reply);
		
		//envia tamanho de ficheiro
		File filePath = new File(path + "/" + Proxy.getFilesFolder() + req.getFile().getFullPath() + "/" + req.getFile().getFullPath());
		System.out.println("FilePath is: " + path + "/" + Proxy.getFilesFolder() + req.getFile().getFullPath() + "/" + req.getFile().getFullPath());
		long fileSize = filePath.length();
		this.connection.getOutputStream().writeLong(fileSize);
		
		//obtem chave cifrada
		String keyPath = path + "/" + Proxy.getFilesFolder() + req.getFile().getFullPath() + "/" + req.getUser().getName() + ".key";
		System.out.println("Key Path: " + keyPath);
		byte[] cipheredKey = SecUtils.readKeyFromFile(keyPath);
		CipheredKey ck = new CipheredKey(req.getUser().getName(), cipheredKey);
		this.connection.getOutputStream().writeObject(ck);
		
		//envia IV
		File IVPath = new File(path + "/" + Proxy.getFilesFolder() + req.getFile().getFullPath()+"/iv");
		fr = new FileReader(IVPath);
		br = new BufferedReader(fr);
		String IV = br.readLine();
		br.close();
		this.connection.getOutputStream().writeObject(IV);
		
		// envia ficheiro
		int read = 0;
		long totalSent = 0;
		byte[] buf = new byte[16];
		FileInputStream fis = new FileInputStream(filePath);
		while ((read = fis.read(buf)) != -1) {
			System.out.println("line: " + SecUtils.getHexString(buf));
			System.out.println("================");
			this.connection.getOutputStream().write(buf, 0, read);
			totalSent += read;
		}
		fis.close();
		System.out.println("ENVIADOS: " + totalSent +", OF: " + fileSize );
		
		//envia assinatura
		String sigPath = path + "/" + Proxy.getFilesFolder() + req.getFile().getFullPath() + "/signature.sig";
		GenericSignature gs = GenericSignature.readSignatureFromFile(sigPath);
		this.connection.getOutputStream().writeObject(gs);
		
		return new Reply(200);
	}

	/**
	 * Executa a acção de receber um ficheiro do cliente
	 *
	 * @param req Request que contém toda a informação necessária para realizar a acção
	 * @return Reply final que vai ser enviada ao cliente
	 * @throws Exception
     */
	private Reply executeReceiveFile(Request req) throws Exception {

		Group group = this.groupsProxy.find(req.getContact());

		// get base path where to store the new file
		String path = this.getPath(group, req);
		path = path + "/" + Proxy.getFilesFolder();

		// get filename with it's extension
		String filename = req.getFile().getFullPath();
		File file = new File(path + "" + filename + "/");
		File filePath = new File(path + "" + filename + "/" + filename);
		
		// if file exists then error
		if (file.exists())
			throw new ApplicationException("Ficheiro ja existente");
		
		// if file doesn't exist then create it
		else{
			file.mkdirs();
			if (!filePath.exists())
				filePath.createNewFile();
			else
				throw new ApplicationException("Ficheiro ja existente");
		}

		// get conversation members names
		ArrayList<String> names = new ArrayList<>();
		
		// if it is a group conversation
		if (group != null && group.hasMemberOrOwner(req.getUser().getName())) {
			Collection<User> members = group.getMembersAndOwner();
			for (User user : members) {
				names.add(user.getName());
			}
		}
		
		// if is a private conversation
		else {
			
			// if the receiver doesn't exist
			if (!this.userProxy.exists(new User(req.getContact())))
				throw new ApplicationException("Destinatário inexistente");
				
			// get contact's user object
			User contact = this.userProxy.find(req.getContact());
			if(contact == null)
				throw new ApplicationException("Erro ao obter destinatário");
			
			// puts receiver name into conversation members list
			names.add(contact.getName());
		}

		// send conversation member names to client
		Reply reply = new Reply(200);
		reply.setNames(names);
		this.connection.getOutputStream().writeObject(reply);
		
		// gets file signature
		GenericSignature signature = (GenericSignature) this.connection.getInputStream().readObject();

		// gets ciphered textual message
		Message message = (Message) this.connection.getInputStream().readObject();

		// gets file size
		long fileSize = this.connection.getInputStream().readLong();
		System.out.println("Filesize eh: " + fileSize);
		
		// stores upload author name
		// change this to username.sig
		File authorFile = new File(path + "" + filename + "/author");
		FileWriter fw = new FileWriter(authorFile);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(req.getUser().getName());
		bw.close();
		
		
		// gets encryption IV
		String IV = (String) this.connection.getInputStream().readObject();
		File IVPath = new File(path + "" + filename + "/iv");
		IVPath.createNewFile();
		fw = new FileWriter(IVPath);
		bw = new BufferedWriter(fw);
		bw.write(IV);
		bw.close();
		
		// gets the file
		boolean ok = this.receiveFile(fileSize, filePath);
		if (!ok)
			throw new ApplicationException("Erro ao receber ficheiro");
			
		
		// gets all ciphered keys and store them properly
		Map<String, CipheredKey> keys = (Map<String, CipheredKey>) this.connection.getInputStream().readObject();
		storeAllKeys(path + "" + filename, keys);
			
		// stores signature
		storeSignature(path + "" + filename, signature);

		// stores file upload message
		boolean inserted;
		if (group != null) {
			inserted =
					this.convProxy.insertGroupMessage(message, keys,
							message.getSignature());
		}
		else {
			inserted = this.convProxy.insertPrivateMessage(message, keys,
					message.getSignature());
		}

		if (!inserted)
			throw new ApplicationException("Ficheiro não foi gravado correctamente!");

		return new Reply(200);
	}

	/**
	 * Guarda uma assinatura
	 *
	 * @param basePath, pasta onde guardar assinatura
	 * @param gs, assinatura a guardar
	 * @throws IOException
	 */
	private void storeSignature(String basePath, GenericSignature gs) throws IOException{
		File sigPath = new File(basePath + "/" + "signature.sig");
		FileWriter fw = new FileWriter(sigPath);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(SecUtils.getHexString(gs.getSignature()));
		bw.close();
	}
	
	/**
	 * Guarda multiplas chaves cifradas
	 *
	 * @param basePath, pasta onde guardar chaves
	 * @param keys, chaves a guardar
	 * @throws IOException
	 */
	private void storeAllKeys(String basePath, Map<String, CipheredKey> keys) throws IOException{
		Set<String> keyNames = keys.keySet();
		Iterator<String> it = keyNames.iterator();
		while (it.hasNext()) {
			String name = it.next();
			CipheredKey key = keys.get(name);
			File keyFile = new File(basePath + "/" + name + ".key");
			FileWriter fw = new FileWriter(keyFile);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(SecUtils.getHexString(key.getKey()));
			bw.close();
		}
	}
	
	/**
	 * Recebe ficheiro por stream
	 *
	 * @param fileSize, tamanho do ficheiro esperado
	 * @param filePath, path onde guardar o ficheiro recebido
	 * @return true se recebeu o tamanho esperado, false caso contrario
	 * @throws IOException
	 */
	private boolean receiveFile(long fileSize, File filePath) throws IOException{
		int totalRead = 0;
		int read = 0;
		byte[] buf = new byte[16];
		FileOutputStream fos = new FileOutputStream(filePath);
		while (totalRead < fileSize) {
			read = this.connection.getInputStream().read(buf);
			if (read == -1)
				continue;
			totalRead += read;
			System.out.println("READ: " + read);
			System.out.println("TOTAL: " + totalRead);
			fos.write(buf, 0, read);
		}
		
		//fechar fos
		fos.close();
		System.out.println("RECEIVED: " + totalRead + " OF: " + fileSize);
		return totalRead == fileSize;
	}
	
	/**
	 * Obtem a ultima mensagem de cada conversacao existente
	 *
	 * @param req Request a considerar
	 * @return Reply com conversacoes
	 * @throws IOException
	 * @require req != null
     */
	private Reply getLastMessageFromConversations(Request req) throws ApplicationException, IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
		
		// obtain all session user conversations
		ArrayList<Conversation> conversations = this.convProxy.getLastMessageFromAll(req.getUser());
		
		// if user has no conversations
		if (conversations.size() == 0)
			throw new ApplicationException("Não existem nenhumas conversas");
			

		ArrayList<Conversation> toDeleteList = new ArrayList<>();
		
		// checks if there are any conversations without messages
		for (Conversation conversation : conversations) {
			if (conversation.getMessages().size() == 0) {
				toDeleteList.add(conversation);
			}
		}

		// delete conversations with no messages
		for (Conversation conversation : toDeleteList) {
			conversations.remove(conversation);
		}

		// if there are no conversations then error
		if (conversations.size() == 0)
			throw new ApplicationException("Não existem nenhumas conversas");
		
		// operation ok
		Reply reply = new Reply(200);
		reply.setType("all");
		reply.setConversations(conversations);
		return reply;
	}

	/**
	 * Obtem a conversacao entre dois utilizadores
	 * Considera-se neste metodo que um utilizador pode
	 * ser um Grupo
	 *
	 * @param req Request a considerar
	 * @return	Reply com conversacao
	 * @throws IOException
	 * @require req != null && req.getUser() != null
     */
	private Reply getConversation(Request req) throws ApplicationException, IOException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException {

		// obtain the corresponding conversation object
		Conversation conversation = this.convProxy.getConversationBetween(req.getUser().getName(),
				req.getContact());
		
		// checks if conversation exists
		if (conversation == null)
			throw new ApplicationException("Não existem conversas entre " + req.getUser().getName() + " e " + req.getContact());
			
		// checks if conversation has messages
		if (conversation.getMessages().size() == 0)
			throw new ApplicationException("Não existem nenhumas mensagens nesta conversa");
			
		// operation ok
		Reply reply = new Reply(200);
		reply.setType("single");
		reply.setConversation(conversation);
		return reply;
	}

	/**
	 * Executa a acção de receber uma mensagem de um cliente
	 * Envia uma mensagem de utilizador para outro
	 * Considera-se neste metodo que um utilizador pode
	 * ser um Grupo
	 *
	 * @param req Request a considerar
	 * @return	Reply para client ja tratada
	 * @throws IOException
	 * @require req != null && req.getUser() != null
	 * 			req.getMessage() != null
     */
	private Reply executeReceiveMessage(Request req) throws ApplicationException, IOException {
		Reply reply = new Reply();

		// verifica se o user de destino nao eh o proprio autor
		if (req.getUser().getName().equals(req.getMessage().getTo()))
			throw new ApplicationException("with yourself..? o.O");

		ArrayList<String> names = new ArrayList<>();
		Group group = this.groupsProxy.find(req.getMessage().getTo());
		if (group != null && group.hasMemberOrOwner(req.getUser().getName())) {
			Collection<User> members = group.getMembersAndOwner();
			for (User user : members) {
				names.add(user.getName());
			}
		}
		// se nao e para um group
		else {
			// verifica se destinatario existe
			if (!this.userProxy.exists(new User(req.getMessage().getTo())))
				throw new ApplicationException("Destinatário inexistente");

			User contact = this.userProxy.find(req.getMessage().getTo());
			names.add(contact.getName());
		}

		// Envia certificados e nomes
		reply.setStatus(200);
		reply.setNames(names);
		System.out.println("NOME ENVIADOS: " + names);
		this.connection.getOutputStream().writeObject(reply);

		// Receber assinatura digital
		NetworkMessage messageWithSignature;
		try {
			messageWithSignature = (NetworkMessage) this.connection.getInputStream().readObject();
			this.connection.getOutputStream().writeObject(new Reply(200));
		} catch (ClassNotFoundException e){
			e.printStackTrace();
			System.out.println("Erro ao receber assinatura, depois de enviar nomes e certificados");
			throw new ApplicationException("Erro ao receber assinatura, depois de enviar nomes e certificados");
		}

		// Receber mensagem cifrada
		Message cipherMessage;
		try {
			cipherMessage = (Message) this.connection.getInputStream().readObject();
			this.connection.getOutputStream().writeObject(new Reply(200));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("Erro ao receber cipherMessage, depois de enviar assinatura");
			throw new ApplicationException("Erro ao receber cipherMessage, depois de enviar assinatura");
		}

		// Receber lista de Ks cifrados
		Map<String, CipheredKey> cipheredKeys;
		try {
			cipheredKeys = (Map<String, CipheredKey>) this.connection.getInputStream().readObject();
			this.connection.getOutputStream().writeObject(new Reply(200));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("Erro ao receber cipheredKeys");
			throw new ApplicationException("Erro ao receber cipheredKeys");
		}

		//se eh para group
		boolean inserted = false;
		if (group != null) {
			inserted =
					this.convProxy.insertGroupMessage(cipherMessage, cipheredKeys,
							messageWithSignature.getSignature());
		}
		//se eh para private
		else {
			inserted = this.convProxy.insertPrivateMessage(cipherMessage, cipheredKeys,
					messageWithSignature.getSignature());
		}

		if (!inserted)
			throw new ApplicationException("Mensagem não foi gravada correctamente!");

		System.out.println("Gravou mensagem!");
		return new Reply(200);

	}

	/**
	 * Execura a acção de remover um Utilizador de um Grupo
	 *
	 * @param groupName Nome do Grupo
	 * @param user		Utilizador autor de pedido
	 * @param member	Nome de membro a remover de grupo
	 * @return 			Reply ja tratada para client
	 * @throws IOException
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @require user != null
     */
	private Reply removeUserFromGroup(String groupName, User user, String member) throws ApplicationException, IOException, InvalidKeyException, NoSuchAlgorithmException {
		
		// find group by the given group name
		Group group = groupsProxy.find(groupName);
		
		// if group doesn't exist
		if (group == null)
			throw new ApplicationException("O grupo " + groupName + " nao existe");
		
		// if session user is not group owner
		if (!group.getOwner().equals(user.getName()))
			throw new ApplicationException("User " + user.getName() + " nao eh owner de grupo " + groupName);
		
		// checks if user to be removed really is a group member
		if (!group.hasMemberOrOwner(member))
			throw new ApplicationException("O utilizador " + member + " nao eh membro do group " + groupName);
			
		// remove member do group
		if (!groupsProxy.removeMember(groupName, member, key))
			throw new ApplicationException("Erro ao remover membro do group");
		
		// operation ok
		return new Reply(200);
	}

	/**
	 * Getter para um path de uma conversa com base nos intervenientes
	 *
	 * @param group Possível interveniente
	 * @param req Request que contém um dos intervenientes
	 * @return String com o path da conversa
	 * @throws IOException
     */
	private String getPath(Group group, Request req) throws IOException {
		String path = "";
		if (group != null) {
			if ( group.hasMemberOrOwner(req.getUser().getName()) )
				path = Proxy.getConversationsGroup() + req.getContact();
		}
		// verifica se eh private
		else {
			path = Proxy.getConversationsPrivate();
			path = path + this.convProxy.getOrCreate(req.getUser().getName(), req.getContact());
		}
		return path;
	}
	
	/**
	 * Executa a acção de adicionar um Utilizador a um Grupo
	 *
	 * @param groupName Nome do Grupo
	 * @param user        Utilizador autor do Request
	 * @param newMember    Nome do novo membro a adicionar a Grupo
	 * @return	Reply ja tratada para client
     * @throws IOException
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @require uProxy != null && user != null
     */
	private Reply addUserToGroup(String groupName, User user, String newMember, SecretKey key) throws ApplicationException, IOException, InvalidKeyException, NoSuchAlgorithmException {

		// checks if the new given member exists
		if (!this.userProxy.exists(new User(newMember)))
			throw new ApplicationException("O user " + newMember + " nao existe");

		// if group doesn't exists then create a new one
		if (!this.groupsProxy.exists(groupName))
			this.groupsProxy.create(groupName, user);
		
		// if group already exists confirm if session user is it's owner
		else if(!this.groupsProxy.isOwner(groupName, user.getName()))
			throw new ApplicationException("Operacao nao permitida. Nao é o owner deste grupo");

		// adds the new member to group
		if (!this.groupsProxy.addMember(groupName, newMember, key))
			throw new ApplicationException("O utilizador " + newMember + " ja e membro do grupo " + groupName);

		// operation ok
		return new Reply(200);
	}
	
	/**
	 * Valida utilizador
	 * Se utilizador nao existe cria um novo
	 *
	 * @param user	User a considerar
     * @return	true se sucesso, false caso contrario
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws IOException 
	 * @require user != null && uProxy != null
     */
	private boolean validateUser(User user, SecretKey key) throws InvalidKeyException, NoSuchAlgorithmException, IOException, InvalidKeySpecException, CertificateException, SignatureException, KeyStoreException, NoSuchProviderException {
		boolean valid = false;
		// se user existe
		if (this.userProxy.exists(user))
			valid = this.userProxy.autheticate(user);
		// se user nao existe
		else {
			
			//valida integridade de ficheiro de users
			if (!MACService.validateMAC(Proxy.getUsersIndex(), key))
				return false;
			//insere
			String password = this.userProxy.insert(user); 
			valid = password != null;

			//actualiza mac de users file
			if (valid){
				MACService.updateMAC(Proxy.getUsersIndex(), key);
			}
		}
		return valid;
	}
}