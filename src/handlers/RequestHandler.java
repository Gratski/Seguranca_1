package handlers;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
import helpers.Connection;
import helpers.FilesHandler;
import proxies.ConversationsProxy;
import proxies.GroupsProxy;
import proxies.Proxy;
import proxies.UsersProxy;
import security.CipheredKey;
import security.MACService;
import security.SecUtils;

/**
 * Esta classe representa a entidade que trata de um Request
 * enviado por um client
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class RequestHandler extends Thread {

	private Connection connection;
	private UsersProxy userProxy;
	private GroupsProxy groupsProxy;
	private ConversationsProxy convProxy;
	private SecretKey key;
	
	/**
	 * Constructor
	 *
	 * @param clientSocket 	Socket client a ser utilizada
	 * @param userProxy		Proxy de Users a ser utilizado
	 * @param groupsProxy	Proxy de Groups a ser utilizado
	 * @param conversationsProxy	Proxy de Conversations a ser utilizado
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
	 * Executa com base no request
	 */
	public void run() {
		Request clientRequest = null;
		Reply reply = null;

		// Obter request
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

		// Tratamento de request
		try {
			// trata de request
			reply = parseRequest(clientRequest, key);

		} catch(SecurityException e) {
			System.out.println("Falha de seguranca.");
			System.out.println(e.getMessage());
			e.printStackTrace();
			this.interrupt();
		} catch (Exception e) {
			System.out.println("Erro ao processar o pedido.");
			 e.printStackTrace();
			this.interrupt();
		}

		// ENVIA RESPOSTA
		try {
			this.connection.getOutputStream().writeObject(reply);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// FECHA LIGACAO
		try {
			this.connection.destroy();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Faz parse de request e encaminha
	 * 
	 * @param req 	Request a ser considerado
	 * @return 		Reply com a resposta devida para o client
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 *
	 * @require req != null
	 */
	Reply parseRequest(Request req, SecretKey key) throws IOException, InvalidKeyException, NoSuchAlgorithmException, CertificateException, InvalidKeySpecException, KeyStoreException, NoSuchProviderException, SignatureException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
		// autentica se existe, senao cria novo
		if (!validateUser(req.getUser(), key))
			return new Reply(400, "User nao autenticado");

		// executa o request
		return executeRequest(req, key);
	}

	/**
	 * Executa o request
	 *
	 * @param req 	Request a considerar
	 * @return 		Reply com resposta tratada
	 * @throws 		IOException
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws KeyStoreException 
	 * @throws CertificateException 
	 * @throws SignatureException 
	 *
	 * @require req != null
     */
	private Reply executeRequest(Request req, SecretKey key) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, SignatureException, CertificateException, KeyStoreException {
		Reply reply = new Reply();

		//verifica se a integridade foi comprometida
		if(!MACService.validateMAC(Proxy.getGroupsIndex(), key))
			throw new SecurityException("Ficheiro " + Proxy.getGroupsIndex() + " comprometido.");
		
		switch (req.getType()) {
		case "-a":
			//verifica integridade de ficheiro de groups
			if(!MACService.validateMAC(Proxy.getGroupsIndex(), key)){
				//LANCAR EXCEPTION EM DISTO
				reply.setStatus(400);
				reply.setMessage("SEGURANCA NOOOOOO GOOOOOOOOD. HIRE JOAO&SIMON NOW!!!");
				break;
			}
			// verifica se o user a adicionar é o próprio
			if (req.getUser().getName().equals(req.getContact())) {
				reply.setStatus(400);
				reply.setMessage("Não se pode adicionar a si próprio a um grupo.");
				break;
			}
			synchronized (groupsProxy) {
				reply = addUserToGroup(req.getGroup(), req.getUser(), req.getContact(), key);
			}
			break;
		case "-d":
			if(!MACService.validateMAC(Proxy.getGroupsIndex(), key)){
				System.out.println("PATH: " + Proxy.getGroupsIndex());
				//LANCAR EXCEPTION EM DISTO
				reply.setStatus(400);
				reply.setMessage("SEGURANCA NOOOOOO GOOOOOOOOD. HIRE JOAO&SIMON NOW!!!");
				break;
			}
			synchronized (groupsProxy) {
				reply = removeUserFromGroup(req.getGroup(), req.getUser(), req.getContact());
			}
			break;
		case "-f":

			if ( !(this.userProxy.exists(new User(req.getContact())) || this.groupsProxy.exists(req.getContact())) ) {
				reply.setStatus(400);
				reply.setMessage("Destinatário inexistente");
				return reply;
			}

			try {
				if (!executeGetFile(req))
					reply = new Reply(400, "Erro ao receber ficheiro");
				else
					reply = new Reply(200);
			} catch (Exception e) {
				reply.setStatus(400);
				reply.setMessage("Erro ao receber ficheiro");
				e.printStackTrace();
			}
			break;
		case "-m":
			synchronized (groupsProxy) {
				req.getMessage().setType("-t");
				req.getMessage().setTimestampNow();
				reply = executeSendMessage(req);
			}
			break;
		case "-r":

			switch (req.getSpecs()) {
			// se eh para obter mensagens de uma conversa
			case "single_contact":
				reply = getConversation(req);
				break;
			// se eh para fazer download
			case "download":
				boolean sent = sendFile(req);
				reply = sent ? new Reply(200) : new Reply(400, "Erro ao fazer download");
				break;
			// se eh para obter todas as mensagens de todos
			case "all":
				reply = getLastMessageFromConversations(req);
				break;
			default:
				reply.setStatus(400);
				reply.setMessage("Comando invalida");
				break;
			}
			break;
		default:
			reply.setStatus(400);
			reply.setMessage("Comando invalido");
			break;
		}
		return reply;
	}

	/**
	 * Obtem a ultima mensagem de cada conversacao existente
	 *
	 * @param req Request a considerar
	 * @return Reply com conversacoes
	 * @throws IOException
	 * @require req != null
     */
	private Reply getLastMessageFromConversations(Request req) throws IOException {
		Reply reply = new Reply();
		//obtem todas as conversas
		ArrayList<Conversation> conversations = this.convProxy.getLastMessageFromAll(req.getUser());
		if (conversations.size() == 0) {
			reply.setStatus(400);
			reply.setMessage("Não existem nenhumas conversas");
		} else {
			reply.setStatus(200);
			reply.setType("all");
			reply.setConversations(conversations);
		}
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
	private Reply getConversation(Request req) throws IOException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException {
		Reply reply = new Reply();

		//obtem a conversacao entre user e contacto
		Conversation conversation = this.convProxy.getConversationBetween(req.getUser().getName(),
				req.getContact());

		if (conversation == null) {
			reply.setStatus(400);
			reply.setMessage("Não existem conversas entre " + req.getUser().getName() + " e " + req.getContact());
			System.out.println(reply);
		} else {
			reply.setStatus(200);
			reply.setType("single");
			reply.setConversation(conversation);
		}

		return reply;
	}

	/**
	 * Metodo para upload de ficheiro e registo de respectiva
	 * mensagem em respectiva conversacao
	 *
	 * @param req Request a ser considerado
	 * @return	true em caso de sucesso, false caso contrario
	 * @throws Exception
	 * @require req != null && req.getFile() != null
	 * 			req.getUser() != null
     */
	private boolean executeGetFile(Request req) throws Exception {

		// Obtem nome de ficheiro
		String filename = req.getFile().getFile().getName();

		//por defeito assume que nao eh group
		boolean isGroup = false;
		
		// formula message
		Message msg = new Message(req.getUser().getName(), req.getContact(), filename);
		msg.setType("-f");
		msg.setTimestampNow();
		req.setMessage(msg);
		
		Group group = this.groupsProxy.find(req.getContact());

		String path = "";
		
		// verifica se eh group
		if (group != null) {
			if ( group.hasMemberOrOwner(req.getUser().getName()) ) {
				path = Proxy.getConversationsGroup() + req.getContact();
				isGroup = true;
			} else {
				return false;
			}
		}
		// verifica se eh private
		else {
			path = Proxy.getConversationsPrivate();
			path = path + this.convProxy.getOrCreate(req.getUser().getName(), req.getContact());
		}

		//pasta de ficheiros
		path = path + "/" + Proxy.getFilesFolder();
		System.out.println("Filepath: " + path);

		//verifica se o file jah existe
		FilesHandler fHandler = new FilesHandler();
		if (fHandler.existsFile(path + filename)) {
			System.out.println("File ja existe");
			return false;
		}

		//envia mensagem
		boolean ok = true;
		if (isGroup)
			//TODO -> Alterar isto para guardar sintese e chaves K
			ok = this.convProxy.insertGroupMessage(req.getMessage(), null, null);
		else
			ok = this.convProxy.insertPrivateMessage(req.getMessage(), null, null);
		
		//envia feedback ao user
		if (ok)
			this.connection.getOutputStream().writeObject(new Reply(200));
		else{
			this.connection.getOutputStream().writeObject(new Reply(400, "Erro ao enviar ficheiro"));
			return false;
		}

		return fHandler.receive(this.connection, path, filename) != null;
	}

	/**
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
	private Reply executeSendMessage(Request req) throws IOException {
		Reply reply = new Reply();

		// verifica se o user de destino nao eh o proprio autor
		if (req.getUser().getName().equals(req.getMessage().getTo())) {
			reply.setStatus(400);
			reply.setMessage("with yourself..? o.O");
			return reply;
		}

		ArrayList<String> names = new ArrayList<>();
		Group group = this.groupsProxy.find(req.getMessage().getTo());
		if (group != null && group.hasMemberOrOwner(req.getUser().getName())) {
			Collection<User> members = group.getMembers();
			for (User user : members) {
				names.add(user.getName());
			}
		}
		// se nao e para um group
		else {
			// verifica se destinatario existe
			if (!this.userProxy.exists(new User(req.getMessage().getTo()))) {
				reply.setStatus(400);
				reply.setMessage("Destinatário inexistente");
				return reply;
			}

			User contact = this.userProxy.find(req.getMessage().getTo());
			names.add(contact.getName());
		}

		// Envia certificados e nomes
		reply.setStatus(200);
		reply.setNames(names);
		this.connection.getOutputStream().writeObject(reply);

		// Receber assinatura digital
		NetworkMessage messageWithSignature;
		try {
			messageWithSignature = (NetworkMessage) this.connection.getInputStream().readObject();
			this.connection.getOutputStream().writeObject(new Reply(200));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("Erro ao receber assinatura, depois de enviar nomes e certificados");
			reply.setStatus(400);
			reply.setMessage("Erro ao receber assinatura, depois de enviar nomes e certificados");
			return reply;
		}

		// Receber mensagem cifrada
		Message cipherMessage;
		try {
			cipherMessage = (Message) this.connection.getInputStream().readObject();
			this.connection.getOutputStream().writeObject(new Reply(200));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("Erro ao receber cipherMessage, depois de enviar assinatura");
			reply.setStatus(400);
			reply.setMessage("Erro ao receber cipherMessage, depois de enviar assinatura");
			return reply;
		}

		
		
		// Receber lista de Ks cifrados
		Map<String, CipheredKey> cipheredKeys;
		try {
			cipheredKeys = (Map<String, CipheredKey>) this.connection.getInputStream().readObject();
			this.connection.getOutputStream().writeObject(new Reply(200));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("Erro ao receber cipheredKeys");
			reply.setStatus(400);
			reply.setMessage("Erro ao receber cipheredKeys");
			return reply;
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
		
		System.out.println("Gravou mensagem!");
		reply.setStatus(200);
		return reply;



		/////////// OLD BUILD - to be updated
//		Group group = this.groupsProxy.find(req.getMessage().getTo());
//
//		// verifica se o user de destino nao eh o proprio autor
//		if (req.getUser().getName().equals(req.getMessage().getTo())) {
//			reply.setStatus(400);
//			reply.setMessage("with yourself..? o.O");
//		} else if (group != null && group.hasMemberOrOwner(req.getUser().getName())) {
//
//			boolean inserted = this.convProxy.insertGroupMessage(req.getMessage());
//			if (!inserted) {
//				reply.setStatus(400);
//				reply.setMessage("Erro ao enviar mensagem");
//			} else
//				reply.setStatus(200);
//		}
//		// se nao e para um group
//		else {
//			// verifica se destinatario existe
//			if (!this.userProxy.exists(new User(req.getMessage().getTo()))) {
//				reply.setStatus(400);
//				reply.setMessage("Destinatário inexistente");
//				return reply;
//			}
//			this.convProxy.insertPrivateMessage(req.getMessage());
//			reply.setStatus(200);
//		}
//		return reply;
	}

	/**
	 * Envia um ficheiro para client
	 *
	 * @param req Request a ser considerado
	 * @return	true se com sucesso, false caso contrario
	 * @throws IOException
	 * @throws KeyStoreException 
	 * @throws CertificateException 
	 * @throws SignatureException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @require req != null && req.getFile() != null
	 * 			req.getUser() != null
     */
	private boolean sendFile(Request req) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, SignatureException, CertificateException, KeyStoreException {
		String filename = req.getFile().getFullPath();
		String path = this.convProxy.userHasConversationWith(req.getUser().getName(), req.getContact());

		boolean ok = false;
		File file = null;
		if (path != null) {
			file = new File(path + "/" + Proxy.getFilesFolder() + filename);
			ok = file.exists();
		}

		// send auth level
		Reply reply = new Reply(ok ? 200 : 400);

		//conexao utilizada para o envio do ficheiro
		this.connection.getOutputStream().writeObject(reply);
		this.connection.getOutputStream().flush();

		if (!ok)
			return false;

		// devolve o resultado do envio do file
		return new FilesHandler().send(this.connection, null, req.getUser(), file);
	}

	/**
	 * Remove um Utilizador de um Grupo
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
	private Reply removeUserFromGroup(String groupName, User user, String member) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
		Reply reply = new Reply(200);

		Group group = groupsProxy.find(groupName);

		// verifica se group existe
		if (group == null) {
			reply.setStatus(400);
			reply.setMessage("Group inexistente");
			return reply;
		}
		// verifica se user eh owner
		if (!group.getOwner().equals(user.getName())) {
			reply.setStatus(401);
			reply.setMessage("User " + user.getName() + " is not the owner of group " + groupName);
			return reply;
		}

		// verifica se o member e realmente member do group
		if (!group.hasMemberOrOwner(member)) {
			reply.setStatus(400);
			reply.setMessage("O utilizador " + member + " nao eh membro do group " + groupName);
			return reply;
		}

		// remove member do group
		if (!groupsProxy.removeMember(groupName, member, key)) {
			reply.setStatus(400);
			reply.setMessage("Erro ao remover membro do group");
			return reply;
		}
		return reply;
	}

	/**
	 * Adiciona um Utilizador a um Grupo
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
	private Reply addUserToGroup(String groupName, User user, String newMember, SecretKey key) throws IOException, InvalidKeyException, NoSuchAlgorithmException {

		// verifica se o user de contacto existe
		if (!this.userProxy.exists(new User(newMember)))
			return new Reply(400, "O user " + newMember + " nao existe");

		// group nao existe => cria novo
		if (!this.groupsProxy.exists(groupName))
			this.groupsProxy.create(groupName, user);

		// verifica se user eh owner
		if (!this.groupsProxy.isOwner(groupName, user.getName()))
			return new Reply(400, "User " + user.getName() + " is not the owner of group " + groupName);

		// adiciona newMember a group
		if (!this.groupsProxy.addMember(groupName, newMember, key))
			return new Reply(400, "O utilizador " + newMember + " ja e membro do grupo " + groupName);

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
