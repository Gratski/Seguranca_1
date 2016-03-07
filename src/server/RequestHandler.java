package server;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import common.Conversation;
import common.Message;
import common.Reply;
import common.Request;
import common.User;
import helpers.Connection;
import helpers.FilesHandler;
import proxies.ConversationsProxy;
import proxies.GroupsProxy;
import proxies.UsersProxy;

public class RequestHandler extends Thread{
	
	private Connection connection;
	public volatile UsersProxy userProxy;
	public volatile GroupsProxy groupsProxy;
	public volatile ConversationsProxy convProxy;
	
	public RequestHandler(Socket clientSocket, UsersProxy userProxy, GroupsProxy groups, ConversationsProxy conv) throws IOException {
		this.connection = new Connection(clientSocket);
		this.userProxy = userProxy;
		this.groupsProxy = groups;
		this.convProxy = conv;
		System.out.println("New Thread created!");
	}
	
	public void run() {
		Request clientRequest = null;
		Reply reply = null;
		
		//Obter request
		try {
			clientRequest = (Request) this.connection.getInputStream().readObject();
		} catch (ClassNotFoundException e2) {
			this.interrupt();
			e2.printStackTrace();
		} catch (IOException e2) {
			this.interrupt();
			e2.printStackTrace();
		}
		
		//Tratamento de request
		try {
			//trata de request
			reply = parseRequest(clientRequest, this.connection, userProxy);
				
		} catch(Exception e) {
			System.out.println("Erro ao registar users");
			e.printStackTrace();
		}

		//ENVIA RESPOSTA
		try {
			System.out.println(reply);
			this.connection.getOutputStream().writeObject(reply);
			System.out.println("Replied!");
		} catch (IOException e) {	
			e.printStackTrace();
		}
		
		//FECHA LIGACAO
		try {
			this.connection.destroy();
		} catch (IOException e1) {
			e1.printStackTrace();
		}	
	}

	/**
	 * Faz parse de request
	 * @param req
	 * 		Request a ser considerado
	 * @param uProxy
	 * 		UsersProxy a ser utilizado
	 * @return
	 * 		Reply com a resposta devida para o client
	 */
	private Reply parseRequest(Request req, Connection conn, UsersProxy uProxy) throws IOException {
		System.out.println("REQUEST RECEIVED: " + req.toString());

		//se eh apenas para registo
		if (req.getType().equals("-regUser")) {
			return insertNewUser(req.getUser(), uProxy);
		}
		//valida user
		if (!validateUser(req.getUser(), uProxy))
			return new Reply(400, "User nao autenticado");

		//executa o request
		return executeRequest(req, conn, uProxy);
	}
	
	private Reply executeRequest(Request req, Connection conn, UsersProxy uProxy) throws IOException {
		System.out.println("Executing request");
		Reply reply = new Reply();
		switch (req.getType()) {
		case "-a":
			System.out.println("Adicionar membro a group");
			synchronized(groupsProxy) {
				reply = addUserToGroup(req.getGroup(), req.getUser(), req.getContact(), uProxy);
			}
			break;
		case "-d":
			System.out.println("Remover membro de group");
			synchronized(groupsProxy) {
				reply = removeUserFromGroup(req.getGroup(), req.getUser(), req.getContact(), uProxy);
			}
			break;
		case "-f":
			System.out.println("Receber ficheiro");
			try {
				reply = executeGetFile(req, conn);
				reply.setStatus(200);
			} catch(Exception e) {
				reply.setStatus(400);
				reply.setMessage("Erro ao enviar ficheiro");
			}
			break;
		case "-m":
			System.out.println("Enviar mensagem");
			synchronized(groupsProxy) {
				req.getMessage().setType("-t");
				reply = executeSendMessage(req, uProxy);				
			}
			break;
		case "-r":
			switch (req.getSpecs()) {
			//se eh para obter mensagens de uma conversa
			case "single_contact":
				System.out.println("SINGLE CONTACT");
				break;
			//se eh para fazer download
			case "download":
				System.out.println("DOWNLOAD FILE");
				sendFile(conn, req);
				break;
			//se eh para obter todas as mensagens de todos
			case "all":
				System.out.println("TODAS DE TODOS");
				break;
			default:
				System.out.println("UNKNOWN");
				break;
			}
			reply.setStatus(200);
			break;
		default:
			reply.setStatus(400);
			reply.setMessage("Comando invalido");
			break;
		}
		return reply;
	}
	
	
	private Reply executeGetFile(Request req, Connection conn) throws Exception {
		System.out.println("UPLOAD ========================");
		
		//Obtem nome de ficheiro
		String filename = req.getFile().getFile().getName();
		System.out.println("Filename: "+filename);
		
		//formula message
		Message msg = new Message(req.getUser().getName(), req.getContact(), filename);
		msg.setType("-f");
		req.setMessage(msg);
		
		//escreve message
		Reply reply = executeSendMessage(req, UsersProxy.getInstance());
		System.out.println("Sent!"); // fix this
		if(reply.getStatus() != 200){
			System.out.println("DIFERENTE DE 200...");
			return reply;
		}

		//obtem ficheiro de upload
		FilesHandler handler = new FilesHandler();
		String path = null;
		System.out.println("Checking if is group or private...");
		//verifica se eh uma mensagem para um group
		if ( req.getUser().getGroups().containsKey(req.getContact()) ) {
			System.out.println("Store in group: " + req.getContact());
			path = "DATABASE/CONVERSATIONS/GROUP/"+req.getContact()+"/FILES";
		}
		//verifica se eh private
		else{
			path = "DATABASE/CONVERSATIONS/PRIVATE/";
			path = path + "" + ConversationsProxy.getInstance().getConversationID(req.getUser().getName(), req.getContact());
			path = path + "/FILES";
			System.out.println("Store in Private: " + path);
		}
		
		System.out.println("Writing file!");
		handler.receive(conn, path , filename);
		return reply;
	}

	private Reply executeSendMessage(Request req, UsersProxy uProxy) throws IOException {
		Reply reply = new Reply();
		
		//verifica se eh uma mensagem para um group
		if ( req.getUser().getGroups().containsKey(req.getMessage().getTo()) ) {
			System.out.println("Message destination is a group!");
			boolean inserted = ConversationsProxy.getInstance().insertGroupMessage(req.getMessage());
			if (!inserted) {
				reply.setStatus(400);
				reply.setMessage("Erro ao enviar mensagem");
			} else
				reply.setStatus(200);
		}
		//se nao e para um group
		else {
			System.out.println("Message is for a private user!");
			//verifica se destinatario existe
			if (!uProxy.exists( new User(req.getMessage().getTo()))) {
				reply.setStatus(400);
				reply.setMessage("Destinatario inexistente");
				return reply;
			}
			
			System.out.println("Now we are going to send it!");
			ConversationsProxy.getInstance().insertPrivateMessage(req.getMessage());
			reply.setStatus(200);
		}
		return reply;
	}

	private void sendFile(Connection conn, Request req) throws IOException {
		String contact = req.getContact();
		String filename = req.getFile().getFullPath();
		
		//get conversation between users folder
		Conversation c = null;
		if (c == null) {
			conn.getOutputStream().writeLong(-1);
			conn.getOutputStream().flush();
			return;
		}
		File file = new File("CONVERSATIONS/" + contact + "/" + filename);
		if (!file.exists()) {
			conn.getOutputStream().writeLong(-1);
			conn.getOutputStream().flush();
			return;
		}
		FilesHandler fHandler = new FilesHandler();
		
		//fazer qualquer coisa com isto :P
		boolean sent = fHandler.send(conn, file);
	}
	
	private Reply removeUserFromGroup(String groupName, User user, String member, UsersProxy uProxy) throws IOException {
		Reply reply = new Reply(200);
		System.out.println("==========REMOVER MEMBRO DE GROUP===========");
		
		//verifica se o user de contacto existe
		/*if( !uProxy.exists(newMember) ){
			reply.setStatus(400);
			reply.setMessage("User nao existe");
			return reply;
		}
		*/
		
		//verifica se group existe
		System.out.println("Ver se group existe");
		if ( !groupsProxy.exists(groupName) ) {
			reply.setStatus(400);
			reply.setMessage("Group inexistente");
			return reply;
		}
		
		System.out.println("Ver owner");
		//verifica se user eh owner
		if ( !groupsProxy.isOwner(groupName, user.getName()) ) {
			reply.setStatus(401);
			reply.setMessage("User " + user.getName() + " is not the owner of group " + groupName);
			return reply;
		}
		System.out.println("Ver se eh membro");
		//verifica se o member e realmente member do group
		if (!groupsProxy.hasMember(groupName, member)) {
			reply.setStatus(400);
			reply.setMessage("O utilizador "+ member +" nao eh membro do group " + groupName + "");
			return reply;
		}
		
		System.out.println("Vai remover");
		//remove member do group
		if (!groupsProxy.removeMember(groupName, member)) {
			reply.setStatus(400);
			reply.setMessage("Erro ao remover membro do group");
			return reply;
		}
		return reply;
	}
	
	/**
	 * Adiciona um novo membro a um group
	 * @param groupName
	 * 		nomo do group a ser considerado
	 * @param user
	 * 		User autor do request
	 * @param newMember
	 * 		User novo membro do group groupName
	 * @param uProxy
	 * 		Proxy de Utilizadores a ser utilizado
	 * @return
	 * 		Reply em conformidade com a logica aplicacional
	 * @throws IOException
	 */
	private Reply addUserToGroup(String groupName, User user, String newMember, UsersProxy uProxy) throws IOException {
		Reply reply = new Reply(200);
		System.out.println("==========ADICIONAR MEMBRO A GROUP===========");
		//verifica se o user de contacto existe
		/*if( !uProxy.exists(newMember) ){
			reply.setStatus(400);
			reply.setMessage("User nao existe");
			return reply;
		}
		*/
		
		//verifica se group existe
		GroupsProxy gProxy = GroupsProxy.getInstance();
		
		if ( !gProxy.exists(groupName) )
			gProxy.create(groupName, user);
		
		//verifica se user eh owner
		if ( !gProxy.isOwner(groupName, user.getName()) ) {
			reply.setStatus(401);
			reply.setMessage("User " + user.getName() + " is not the owner of group " + groupName);
			return reply;
		}
		
		//verificar se member existe em users
		//AQUI POR FAZER DEVIDO A TESTES
		//ASSIM NAO TEMOS QUE INSERIR SEMPRE PARA TESTAR ADICIONAR
		
		//adiciona newMember a group
		if ( !gProxy.addMember(groupName, newMember) ) {
			reply.setStatus(402);
			reply.setMessage("Erro ao adicionar novo membro a " + groupName);
			return reply;
		}
		return reply;
	}
	
	/**
	 * Adiciona um novo utilizador
	 * @param user
	 * 		User a ser adicionado
	 * @param proxy
	 * 		UsersProxy a ser utilizado
	 * @return
	 * 		reply a ser enviada ao user
	 */
	private Reply insertNewUser(User user, UsersProxy proxy) {
		if (proxy.exists(user) || !proxy.insert(user))
			return new Reply(404, "Erro ao adicionar novo utilizador");
		else
			return new Reply(200);
	}

	//valida user de request
	//se nao existe user => cria novo user
	private boolean validateUser(User user, UsersProxy uProxy) {
		boolean valid = false;
		//se user existe
		if ( uProxy.exists(user) )
			valid = uProxy.autheticate(user);
			//se user nao existe
		else
			valid = uProxy.insert(user);

		return valid;
	}
}
