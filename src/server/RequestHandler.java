package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import builders.FileStreamBuilder;
import common.Conversation;
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
	
	public RequestHandler(Socket clientSocket, UsersProxy userProxy, GroupsProxy groups, ConversationsProxy conv) throws IOException{
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
			
			System.out.println("receiving request");
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
	private Reply parseRequest(Request req, Connection conn, UsersProxy uProxy) throws IOException{
		
		Reply reply = null;
		
		System.out.println("REQUEST TYPE: " +req.getType() );
		
		//se eh apenas para registo
		if(req.getType().equals("-regUser")){
			System.out.println("Registar novo user");
			return insertNewUser(req.getUser(), uProxy);
		}
		
		//valida user
		if(!validateUser(req, uProxy))
		{
			reply = new Reply();
			reply.setStatus(400);
			reply.setMessage("User nao autenticado");
			return reply;
		}
		System.out.println("Request is valid");
		System.out.println("Executing request");
		//executa o request
		reply = executeRequest(req, conn, uProxy);

		return reply;
	}
	
	//valida user de request
	//se nao existe user => cria novo user
	private boolean validateUser(Request req, UsersProxy uProxy){
		boolean valid = false;
		//se user existe
		if( uProxy.exists(req.getUser()) )
			valid = uProxy.autheticate(req.getUser());
		//se user nao existe
		else
			valid = uProxy.insert(req.getUser());
		
		return valid;
	}
	
	private Reply executeRequest(Request req, Connection conn, UsersProxy uProxy) throws IOException{
		
		Reply reply = new Reply();
		
		switch(req.getType()){
		case "-a":
			System.out.println("Adicionar membro a group");
			synchronized(groupsProxy){
				reply = addUserToGroup(req.getGroup(), req.getUser(), req.getContact(), uProxy);
			}
			break;
		case "-d":
			System.out.println("Remover membro de group");
			synchronized(groupsProxy){
				reply = removeUserFromGroup(req.getGroup(), req.getUser(), req.getContact(), uProxy);
			}
			break;
		case "-f":
			System.out.println("RECEIVE FILE");
			FilesHandler fHandler = new FilesHandler();
			
			String filename = req.getFile().getFile().getName();
			
			//get file
			try {
				System.out.println("Prepare to receive");
				File file = fHandler.receive(conn, "SERVERFILES", filename);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			reply = new Reply();
			reply.setStatus(200);
			break;
		case "-m":
			System.out.println("Enviar mensagem");
			synchronized(groupsProxy){
				
				System.out.println("==================");
				System.out.println("Message Details");
				System.out.println("From: " + req.getMessage().getFrom());
				System.out.println("To: " + req.getMessage().getTo());
				System.out.println("Body: " + req.getMessage().getBody());
				System.out.println("==================");
				
				reply = executeSendMessage(req, uProxy);
				
				reply.setStatus(200);
				
			}
			break;
		case "-r":
			
			switch(req.getSpecs())
			{
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
			
			reply = new Reply();
			reply.setStatus(200);
			
			break;
		default:
			reply = new Reply();
			reply.setStatus(400);
			reply.setMessage("Comando invalido");
			break;
		}
		
		
		return reply;
	}
	
	
	private Reply executeSendMessage(Request req, UsersProxy uProxy) throws IOException {
		Reply reply = new Reply();
		
		//verifica se eh uma mensagem para um group
		if( req.getUser().getGroups().containsKey(req.getMessage().getTo()) )
		{
			System.out.println("Message destination is a group!");
			reply.setStatus(200);
		}
		//se nao e para um group
		else{
			System.out.println("Message is for a private user!");
			//verifica se destinatario existe
			if(!uProxy.exists( new User(req.getMessage().getTo()))){
				reply.setStatus(400);
				reply.setMessage("Destinatario inexistente");
				return reply;
			}
			
			System.out.println("Now we are going to send it!");
			ConversationsProxy.getInstance().insertMessage(req.getMessage());
		}
		return reply;
	}

	private void sendFile(Connection conn, Request req) throws IOException{
		
		String contact = req.getContact();
		String filename = req.getFile().getFullPath();
		
		//get conversation between users folder
		Conversation c = null;
		if(c == null)
		{
			conn.getOutputStream().writeLong(-1);
			conn.getOutputStream().flush();
			return;
		}
		
		File file = new File("CONVERSATIONS/"+contact+"/"+filename);
		if(!file.exists()){
			conn.getOutputStream().writeLong(-1);
			conn.getOutputStream().flush();
			return;
		}
		
		FilesHandler fHandler = new FilesHandler();
		
		//fazer qualquer coisa com isto :P
		boolean sent = fHandler.send(conn, file);
		
	}
	
	private Reply removeUserFromGroup(String groupName, User user, String member, UsersProxy uProxy) throws IOException{
		
		Reply reply = new Reply();
		reply.setStatus(200);
		
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
	private Reply addUserToGroup(String groupName, User user, String newMember, UsersProxy uProxy) throws IOException{
		
		Reply reply = new Reply();
		reply.setStatus(200);
		
		
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
		
		if( !gProxy.exists(groupName) )
			gProxy.create(groupName, user);
		
		//verifica se user eh owner
		if( !gProxy.isOwner(groupName, user.getName()) ){
			reply.setStatus(401);
			reply.setMessage("User " + user.getName() + " is not the owner of group " + groupName);
			return reply;
		}
		
		//verificar se member existe em users
		//AQUI POR FAZER DEVIDO A TESTES
		//ASSIM NAO TEMOS QUE INSERIR SEMPRE PARA TESTAR ADICIONAR
		
		//adiciona newMember a group
		if ( !gProxy.addMember(groupName, newMember) ){
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
		Reply reply = new Reply();
		if(proxy.exists(user) || !proxy.insert(user)){
			reply.setStatus(404);
			reply.setMessage("Erro ao adicionar novo utilizador");
		}
		else
			reply.setStatus(200);
			
		return reply;
	}
}
