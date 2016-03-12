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

public class RequestHandler extends Thread {

	private Connection connection;
	public volatile UsersProxy userProxy;
	public volatile GroupsProxy groupsProxy;
	public volatile ConversationsProxy convProxy;

	public RequestHandler(Socket clientSocket, UsersProxy userProxy, GroupsProxy groupsProxy,
			ConversationsProxy conversationsProxy) throws IOException {
		this.connection = new Connection(clientSocket);
		this.userProxy = userProxy;
		this.groupsProxy = groupsProxy;
		this.convProxy = conversationsProxy;
		System.out.println("New Thread created!");
	}

	public void run() {
		Request clientRequest = null;
		Reply reply = null;

		// Obter request
		try {
			clientRequest = (Request) this.connection.getInputStream().readObject();
			System.out.println("REQUEST RECEIVED: " + clientRequest.toString());
		} catch (ClassNotFoundException e2) {
			this.interrupt();
			e2.printStackTrace();
		} catch (IOException e2) {
			this.interrupt();
			e2.printStackTrace();
		}

		// Tratamento de request
		try {
			// trata de request
			reply = parseRequest(clientRequest);

		} catch (Exception e) {
			System.out.println("Erro ao registar users");
			e.printStackTrace();
		}

		// ENVIA RESPOSTA
		try {
			System.out.println(reply);
			this.connection.getOutputStream().writeObject(reply);
			System.out.println("Replied!");
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
	private Reply parseRequest(Request req) throws IOException {
		// se eh apenas para registo
		if (req.getType().equals("-regUser")) {
			return insertNewUser(req.getUser(), this.userProxy);
		}
		// valida user
		if (!validateUser(req.getUser(), this.userProxy))
			return new Reply(400, "User nao autenticado");

		// executa o request
		return executeRequest(req);
	}

	private Reply executeRequest(Request req) throws IOException {
		System.out.println("Executing request");
		Reply reply = new Reply();
		switch (req.getType()) {
		case "-a":
			System.out.println("Adicionar membro a group");
			synchronized (groupsProxy) {
				reply = addUserToGroup(req.getGroup(), req.getUser(), req.getContact(), this.userProxy);
				System.out.println("RSP DE SAIDA: " + reply.getStatus());
			}
			break;
		case "-d":
			System.out.println("Remover membro de group");
			synchronized (groupsProxy) {
				reply = removeUserFromGroup(req.getGroup(), req.getUser(), req.getContact());
			}
			break;
		case "-f":
			System.out.println("Receber ficheiro");
			try {
				if(!executeGetFile(req))
					reply = new Reply(400, "Erro ao enviar ficheiro");
				else
					reply = new Reply(200);
			} catch (Exception e) {
				reply.setStatus(400);
				reply.setMessage("Erro ao enviar ficheiro");
				e.printStackTrace();
			}
			break;
		case "-m":
			System.out.println("Enviar mensagem");
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
				System.out.println("TODAS DE TODOS");
				reply = getLastMessageFromConversations(req);
				break;
			default:
				System.out.println("UNKNOWN");
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
		if (conversations == null) {
			reply.setStatus(400);
			reply.setMessage("Não existem conversas entre " + req.getUser().getName() + " e " + req.getContact());
		} else {
			reply.setStatus(200);
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
			reply.addConversation(conversation);
		}
		return reply;
	}

	private boolean executeGetFile(Request req) throws Exception {
		System.out.println("UPLOAD ========================");

		// Obtem nome de ficheiro
		String filename = req.getFile().getFile().getName();
		System.out.println("Filename: " + filename);

		// formula message
		Message msg = new Message(req.getUser().getName(), req.getContact(), filename);
		msg.setType("-f");
		msg.setTimestampNow();
		req.setMessage(msg);

		// escreve message
		Reply reply = executeSendMessage(req);
		this.connection.getOutputStream().writeObject(reply);
		System.out.println("Sent!"); // fix thisrequehan
		if (reply.getStatus() != 200) {
			System.out.println("DIFERENTE DE 200...");
			return false;
		}

		// obtem ficheiro de upload
		FilesHandler handler = new FilesHandler();
		String path = null;
		System.out.println("Checking if is group or private...");
		// verifica se eh uma mensagem para um group

		Group group = GroupsProxy.getInstance().find(req.getContact());
		if (group != null && group.hasMemberOrOwner(req.getUser().getName())) {
			System.out.println("Store in group: " + req.getContact());
			path = "DATABASE/CONVERSATIONS/GROUP/" + req.getContact() + "/FILES";
		}
		// verifica se eh private
		else {
			path = "DATABASE/CONVERSATIONS/PRIVATE/";
			path = path + ConversationsProxy.getInstance().getConversationID(req.getUser().getName(), req.getContact());
			path = path + "/FILES";
			System.out.println("Store in Private: " + path);
		}

		System.out.println("Writing file!");
		return handler.receive(this.connection, path, filename) != null;
	}

	private Reply executeSendMessage(Request req) throws IOException {
		Reply reply = new Reply();
		Group group = GroupsProxy.getInstance().find(req.getMessage().getTo());

		// verifica se o user de destino nao eh o proprio autor
		if (req.getUser().getName().equals(req.getMessage().getTo())) {
			reply.setStatus(400);
			reply.setMessage("with yourself..? o.O");
		} else if (group != null && group.hasMemberOrOwner(req.getUser().getName())) {
			System.out.println("Message destination is a group!");
			boolean inserted = ConversationsProxy.getInstance().insertGroupMessage(req.getMessage());
			if (!inserted) {
				reply.setStatus(400);
				reply.setMessage("Erro ao enviar mensagem");
			} else
				reply.setStatus(200);
		}
		// se nao e para um group
		else {
			System.out.println("Message is for a private user!");
			// verifica se destinatario existe
			if (!this.userProxy.exists(new User(req.getMessage().getTo()))) {
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

		if (!ok){
			System.out.println("file nao existe");
			System.out.println("path eh:");
			return false;
		}

		// devolve o resultado do envio do file
		System.out.println("authenticated, vai enviar>>>");
		return new FilesHandler().send(this.connection, file);
	}

	private Reply removeUserFromGroup(String groupName, User user, String member) throws IOException {
		Reply reply = new Reply(200);
		System.out.println("==========REMOVER MEMBRO DE GROUP===========");

		// verifica se group existe
		if (!groupsProxy.exists(groupName)) {
			reply.setStatus(400);
			reply.setMessage("Group inexistente");
			return reply;
		}
		// verifica se user eh owner
		if (!groupsProxy.isOwner(groupName, user.getName())) {
			reply.setStatus(401);
			reply.setMessage("User " + user.getName() + " is not the owner of group " + groupName);
			return reply;
		}
		// verifica se o member e realmente member do group
		if (!groupsProxy.hasMember(groupName, member)) {
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
		System.out.println("==========ADICIONAR MEMBRO A GROUP===========");

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
