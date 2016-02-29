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
		Reply reply = new Reply();
		reply.setStatus(200);
		
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
		try{
			//trata de request
			parseRequest(clientRequest, userProxy);
				
		}catch(Exception e){
			System.out.println("Erro ao registar users");
			e.printStackTrace();
		}
		
			
		//ENVIA RESPOSTA
		try {
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
	private Reply parseRequest(Request req, UsersProxy uProxy) throws IOException{
		
		Reply reply = null;
		
		System.out.println("REQUEST TYPE: " +req.getType() );
		
		switch(req.getType()){
		case "-regUser":
			System.out.println("Registar novo user");
			reply = insertNewUser(req.getUser(), uProxy);
			break;
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
		case "-m":
			System.out.println("Enviar mensagem");
			synchronized(groupsProxy){
				
				//se eh para group
				if( groupsProxy.exists(req.getMessage().getTo())){
					
					File file = new File("DATABASE/CONVERSATIONS/GROUPS/" + req.getMessage().getTo());
					if( !file.exists() ){
						System.out.println("Criar novo file para mensagem");
						file.createNewFile();
					}else
					{
						System.out.println("File de mensagem ja existe");
					}
					
					BufferedWriter writer = FileStreamBuilder.makeWriter("DATABASE/CONVERSATIONS/GROUPS/" + req.getMessage().getTo(), true);
					StringBuilder sb = new StringBuilder();
					sb.append("data");
					sb.append(" " + req.getMessage().getFrom());
					sb.append(" -t");
					sb.append(" " + req.getMessage().getBody());
					sb.append("\n");
					
					writer.write(sb.toString());
					writer.flush();
					writer.close();
					
					reply = new Reply();
					reply.setStatus(200);
				}
				//se eh private
				else{
					
					System.out.println("Eh private");
					
					//verifica se user exist
					if(!userProxy.exists(req.getUser()))
					{
						System.out.println("User inexistente");
						reply = new Reply();
						reply.setStatus(400);
					}
					else if( !userProxy.autheticate(req.getUser()) )
					{
						System.out.println("User nao autenticado");
						reply = new Reply();
						reply.setStatus(401);
					}else if( !userProxy.exists(new User(req.getMessage().getTo())) ){
						System.out.println("Contact inexistente");
						reply = new Reply();
						reply.setStatus(402);
					}
					//se estah tudo ok
					else{
						
						Conversation c;
						if( !this.convProxy.exists(new Conversation(req.getUser(), new User(req.getMessage().getTo()), "")))
							c = this.convProxy.add(req.getMessage().getFrom(), req.getMessage().getTo());
						else
							c = this.convProxy.getConversation(req.getUser().getName(), req.getMessage().getTo());
						
						//abre canal de escrita
						BufferedWriter writer = FileStreamBuilder.makeWriter("DATABASE/CONVERSATIONS/PRIVATE/"+c.getFilename(), true);
						//escreve
						StringBuilder sb = new StringBuilder();
						sb.append("data");
						sb.append(" " + req.getMessage().getFrom());
						sb.append(" -t");
						sb.append(" " + req.getMessage().getBody());
						sb.append("\n");
						
						writer.write(sb.toString());
						writer.flush();
						writer.close();
						
						reply = new Reply();
						reply.setStatus(200);
						
					}
				}
				
			}
			break;
		default:
			reply = new Reply();
			reply.setStatus(400);
			reply.setMessage("Comando invalido");
			break;
		}
		
		return reply;
	}
	
	private Reply removeUserFromGroup(String groupName, User user, String member, UsersProxy uProxy) throws IOException{
		
		Reply reply = new Reply();
		reply.setStatus(200);
		
		System.out.println("==========REMOVER MEMBRO DE GROUP===========");
		
		//authentica user
		if(!authenticateUser(uProxy, user))
		{
			reply.setStatus(400);
			reply.setMessage("User nao autenticado");
			return reply;
		}
		
		
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
		
		//authentica user
		if(!authenticateUser(uProxy, user))
		{
			reply.setStatus(400);
			reply.setMessage("User nao autenticado");
			return reply;
		}
		
		
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
	
	private boolean authenticateUser(UsersProxy uProxy, User user){
		if(!uProxy.exists(user) || !uProxy.autheticate(user))
			return false;
		return true;
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
