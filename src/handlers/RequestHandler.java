package handlers;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import domain.*;
import helpers.Connection;
import helpers.FilesHandler;
import proxies.ConversationsProxy;
import proxies.GroupsProxy;
import proxies.UsersProxy;

/**
 * Esta classe representa a entidade que trata de um Request
 * enviado por um client
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class RequestHandler extends Thread {

	private Connection connection;
	public volatile UsersProxy userProxy;
	public volatile GroupsProxy groupsProxy;
	public volatile ConversationsProxy convProxy;

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
	public RequestHandler(Socket clientSocket, UsersProxy userProxy, GroupsProxy groupsProxy,
			ConversationsProxy conversationsProxy) throws IOException {
		this.connection = new Connection(clientSocket);
		this.userProxy = userProxy;
		this.groupsProxy = groupsProxy;
		this.convProxy = conversationsProxy;
	}

	/**
	 * Executa com base nos seus
	 */
	public void run() {
		Request clientRequest = null;
		Reply reply = null;

		// Obter request
		try {
			clientRequest = (Request) this.connection.getInputStream().readObject();
		} catch(Exception e){
			try{
				this.connection.destroy();
			}catch(Exception ex){
				this.interrupt();
			}
			this.interrupt();
		}

		// Tratamento de request
		try {
			// trata de request
			reply = parseRequest(clientRequest);

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
	 * Faz parse de request
	 * 
	 * @param req
	 *            Request a ser considerado
	 * @return Reply com a resposta devida para o client
	 */
	Reply parseRequest(Request req) throws IOException {
		// valida user
		if (!validateUser(req.getUser(), this.userProxy))
			return new Reply(400, "User nao autenticado");

		// executa o request
		return executeRequest(req);
	}

	private Reply executeRequest(Request req) throws IOException {
		Reply reply = new Reply();
		switch (req.getType()) {
		case "-a":
			System.out.println("Adicionar membro a group");
			synchronized (groupsProxy) {
				reply = addUserToGroup(req.getGroup(), req.getUser(), req.getContact(), this.userProxy);
			}
			break;
		case "-d":
			System.out.println("Remover membro de group");
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
				System.out.println("SINGLE CONTACT");
				reply = getConversation(req);
				break;
			// se eh para fazer download
			case "download":
				System.out.println("DOWNLOAD FILE");
				boolean sent = sendFile(req);
				reply = sent ? new Reply(200) : new Reply(400, "Erro ao fazer download");
				break;
			// se eh para obter todas as mensagens de todos
			case "all":
				reply = getLastMessageFromConversations(req);
				break;
			default:
				reply.setStatus(400);
				reply.setMessage("Operacao invalida");
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

	private Reply getLastMessageFromConversations(Request req) throws IOException {
		Reply reply = new Reply();
		ArrayList<Conversation> conversations = ConversationsProxy.getInstance().getLastMessageFromAll(req.getUser());
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

	private Reply getConversation(Request req) throws IOException {
		Reply reply = new Reply();
		Conversation conversation = ConversationsProxy.getInstance().getConversationBetween(req.getUser().getName(),
				req.getContact());
		if (conversation == null) {
			reply.setStatus(400);
			reply.setMessage("Não existem conversas entre " + req.getUser().getName() + " e " + req.getContact());
		} else {
			reply.setStatus(200);
			reply.setType("single");
			reply.addConversation(conversation);
		}
		return reply;
	}

	private boolean executeGetFile(Request req) throws Exception {

		// Obtem nome de ficheiro
		String filename = req.getFile().getFile().getName();
		System.out.println("Filename: " + filename);
		boolean isGroup = false;
		
		// formula message
		Message msg = new Message(req.getUser().getName(), req.getContact(), filename);
		msg.setType("-f");
		msg.setTimestampNow();
		req.setMessage(msg);
		
		Group group = GroupsProxy.getInstance().find(req.getContact());
		String path = "DATABASE/CONVERSATIONS/";
		
		// verifica se eh group
		if (group != null) {
			if ( group.hasMemberOrOwner(req.getUser().getName()) ) {
				System.out.println("Store in group: " + req.getContact());
				path = path + "GROUP/" + req.getContact();
				isGroup = true;
				System.out.println("eh para group");
			} else {
				return false;
			}
		}
		// verifica se eh private
		else {
			path = path + "PRIVATE/";

			path = path + ConversationsProxy.getInstance().getOrCreate(req.getUser().getName(), req.getContact());
			System.out.println("eh para private");
			isGroup = false;
		}
		//pasta de ficheiros
		path = path + "/FILES";
		
		
		//verifica se o file jah existe
		FilesHandler fHandler = new FilesHandler();
		if (fHandler.existsFile(path + "/" + filename)) {
			System.out.println("File ja existe");
			return false;
		}
		
		//envia mensagem
		boolean ok = true;
		ConversationsProxy cProxy = ConversationsProxy.getInstance();
		System.out.println("Registering message -f.");
		if(isGroup)
			ok = cProxy.insertGroupMessage(req.getMessage());
		else
			ok = cProxy.insertPrivateMessage(req.getMessage());
		
		//envia feedback ao user
		if(ok)
			this.connection.getOutputStream().writeObject(new Reply(200));
		else{
			this.connection.getOutputStream().writeObject(new Reply(400, "Erro ao enviar ficheiro"));
			return false;
		}

		return fHandler.receive(this.connection, path, filename) != null;
	}

	private Reply executeSendMessage(Request req) throws IOException {
		Reply reply = new Reply();
		Group group = GroupsProxy.getInstance().find(req.getMessage().getTo());

		// verifica se o user de destino nao eh o proprio autor
		if (req.getUser().getName().equals(req.getMessage().getTo())) {
			reply.setStatus(400);
			reply.setMessage("with yourself..? o.O");
		} else if (group != null && group.hasMemberOrOwner(req.getUser().getName())) {

			boolean inserted = ConversationsProxy.getInstance().insertGroupMessage(req.getMessage());
			if (!inserted) {
				reply.setStatus(400);
				reply.setMessage("Erro ao enviar mensagem");
			} else
				reply.setStatus(200);
		}
		// se nao e para um group
		else {

			// verifica se destinatario existe
			if (!this.userProxy.exists(new User(req.getMessage().getTo()))) {
				reply.setStatus(400);
				reply.setMessage("Destinatário inexistente");
				return reply;
			}


			ConversationsProxy.getInstance().insertPrivateMessage(req.getMessage());
			reply.setStatus(200);
		}
		return reply;
	}

	private boolean sendFile(Request req) throws IOException {
		String filename = req.getFile().getFullPath();
		ConversationsProxy cProxy = ConversationsProxy.getInstance();
		String path = cProxy.userHasConversationWith(req.getUser().getName(), req.getContact());

		boolean ok = false;
		File file = null;
		if (path != null) {
			file = new File(path + "/FILES/" + filename);
			ok = file.exists();
		}

		// send auth level
		Reply reply = new Reply(ok ? 200 : 400);
		this.connection.getOutputStream().writeObject(reply);
		this.connection.getOutputStream().flush();

		if (!ok)
			return false;

		// devolve o resultado do envio do file
		return new FilesHandler().send(this.connection, file);
	}

	private Reply removeUserFromGroup(String groupName, User user, String member) throws IOException {
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
		if (!groupsProxy.removeMember(groupName, member)) {
			reply.setStatus(400);
			reply.setMessage("Erro ao remover membro do group");
			return reply;
		}
		return reply;
	}

	private Reply addUserToGroup(String groupName, User user, String newMember, UsersProxy uProxy) throws IOException {

		// verifica se o user de contacto existe
		if (!uProxy.exists(new User(newMember)))
			return new Reply(400, "O user " + newMember + " nao existe");

		// group nao existe => cria novo
		GroupsProxy gProxy = GroupsProxy.getInstance();
		if (!gProxy.exists(groupName))
			gProxy.create(groupName, user);

		// verifica se user eh owner
		if (!gProxy.isOwner(groupName, user.getName()))
			return new Reply(400, "User " + user.getName() + " is not the owner of group " + groupName);

		// adiciona newMember a group
		if (!gProxy.addMember(groupName, newMember))
			return new Reply(400, "O utilizador " + newMember + " ja e membro do grupo " + groupName);

		return new Reply(200);
	}

	private Reply insertNewUser(User user, UsersProxy proxy) {
		if (proxy.exists(user) || !proxy.insert(user))
			return new Reply(404, "Erro ao adicionar novo utilizador");
		else
			return new Reply(200);
	}

	// valida user de request
	// se nao existe user => cria novo user
	private boolean validateUser(User user, UsersProxy uProxy) {
		boolean valid = false;
		// se user existe
		if (uProxy.exists(user))
			valid = uProxy.autheticate(user);
		// se user nao existe
		else
			valid = uProxy.insert(user);

		return valid;
	}
}
