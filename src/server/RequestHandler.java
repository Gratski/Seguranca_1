package server;

import java.io.IOException;
import java.net.Socket;

import common.Reply;
import common.Request;
import common.User;
import helpers.Connection;
import proxies.GroupsProxy;
import proxies.UsersProxy;

public class RequestHandler extends Thread{
	
	private Connection connection;

	public RequestHandler(Socket clientSocket) throws IOException{
		this.connection = new Connection(clientSocket);
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
			
			UsersProxy userProxy = UsersProxy.getInstance();
			
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
		
		switch(req.getType()){
		case "-regUser":
			System.out.println("Registar novo user");
			reply = insertNewUser(req.getUser(), uProxy);
			break;
		case "-a":
			System.out.println("Adicionar membro a group");
			reply = addUserToGroup(req.getGroup(), req.getUser(), req.getContact(), uProxy);
			break;
		case "-d":
			System.out.println("Remover membro de group");
			reply = removeUserFromGroup(req.getGroup(), req.getUser(), req.getContact(), uProxy);
			break;
		default:
			reply = new Reply();
			reply.setStatus(400);
			reply.setMessage("Comando invalido");
			break;
		}
		
		return reply;
	}
	
	private Reply removeUserFromGroup(String groupName, User user, User member, UsersProxy uProxy) throws IOException{
		
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
		GroupsProxy gProxy = GroupsProxy.getInstance();
		System.out.println("Ver se group existe");
		if( !gProxy.exists(groupName) )
		{
			reply.setStatus(400);
			reply.setMessage("Group inexistente");
			return reply;
		}
		
		System.out.println("Ver owner");
		//verifica se user eh owner
		if( !gProxy.isOwner(groupName, user.getName()) ){
			reply.setStatus(401);
			reply.setMessage("User " + user.getName() + " is not the owner of group " + groupName);
			return reply;
		}
		System.out.println("Ver se eh membro");
		//verifica se o member e realmente member do group
		if(!gProxy.hasMember(groupName, member)){
			reply.setStatus(400);
			reply.setMessage("O utilizador "+ member.getName() +" nao eh membro do group " + groupName + "");
			return reply;
		}
		
		System.out.println("Vai remover");
		//remove member do group
		if(!gProxy.removeMember(groupName, member))
		{
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
	private Reply addUserToGroup(String groupName, User user, User newMember, UsersProxy uProxy) throws IOException{
		
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
