package domain;

import java.io.Serializable;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Esta classe representa uma resposta do servidor para o client
 *
 * @author JoaoRodrigues & Simao Neves
 */
public class Reply extends NetworkMessage implements Serializable {
	
	private ArrayList<Conversation> conversations;
	private int status;
	private String message;
	private ArrayList<String> names;

	/**
	 * Constructor
	 */
	public Reply() {
		this.conversations = null;
		this.status = 0;
		this.message = null;
		this.names = null;
	}

	/**
	 * Getter para nomes e certificados
	 *
	 * @return Hashmap com nomes de users e os seus certificados
     */
	public ArrayList<String> getNames() {
		return this.names;
	}

	/**
	 * Getter para nomes
	 *
	 */
	public void setNames(ArrayList<String> names) {
		this.names = names;
	}

	/**
	 * Constructor
	 *
	 * @param status Status de Reply
	 * @param message Mensagem de Reply
     */
	public Reply(int status, String message) {
		this.conversations = null;
		this.status = status;
		this.message = message;
	}

	/**
	 * Constructor
	 *
	 * @param status Status de Reply
     */
	public Reply(int status) {
		this.status = status;
		this.message = null;
		this.conversations = null;
	}

	/**
	 * Obtem conversacoes
	 *
	 * @return lista com conversacoes
     */
	public ArrayList<Conversation> getConversations() {
		return this.conversations;
	}

	/**
	 * Altera o valor das conversacoes
	 *
	 * @param conversations Novo valor para conversacoes
     */
	public void setConversations(ArrayList<Conversation> conversations) {
		this.conversations = conversations;
	}

	/**
	 * Adiciona uma conversacao a lista de conversacoes
	 *
	 * @param conversation Conversacao a adicionar
     */
	public void setConversation(Conversation conversation) {
		ArrayList<Conversation> list = new ArrayList<>();
		list.add(conversation);
		this.conversations = list;
	}

	/**
	 * Altera o valor do status
	 *
	 * @param status Novo valor de Status
     */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Altera o valor da message
	 *
	 * @param message Novo valor da message
     */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Obtem o valor da mensagem
	 *
	 * @return mensagem actual
     */
	public String getMessage(){
		return this.message;
	}

	/**
	 * Obtem o status
	 *
	 * @return status actual
     */
	public int getStatus(){
		return this.status;
	}

	/**
	 * Faz o pretty print da Reply
	 *
	 * @param user Utilizador em sessao
	 * @require user != null
     */
	public void prettyPrint(User user){
		//se erro
		if (this.hasError()) {
			System.out.println("Error");
			System.out.println("Description: " + this.message);
		}
		//se ok
		else {
			//se eh -r
			if (this.conversations != null) {
				//se eh apenas de contact
				if (this.getType().equals("single") && this.conversations.size() == 1) {
					ArrayList<Message> messages = conversations.get(0).getMessages();
					Collections.sort(messages);
					for (Message message : messages) {
						message.printMessageFromPerspective(user.getName());
					}
				}
				//se eh last de todos os contact
				else if (this.getType().equals("all")) {
					for (Conversation conversation : conversations) {
						if (conversation.getGroup() != null)
							System.out.println("Contact: " + conversation.getGroup().getName());
						else {
							ArrayList<User> users = conversation.getUsers();
							String finalContact = null;
							for (User u : users) {
								if (u.getName().equals(user.getName()))
									continue;
								finalContact = u.getName();
							}
							System.out.println("Contact: " + finalContact);
						}
						ArrayList<Message> messages = conversation.getMessages();
						if (messages != null && messages.size() == 1) {
							messages.get(0).printMessageFromPerspective(user.getName());
						} else {

						}
					}
				}
			}
		}
	}

	/**
	 * Verifica se este reply tem error status
	 * @return
	 * 	true se tem, false caso contrario
	 */
	public boolean hasError(){
		return !(this.status == 200 || this.status == 0 );
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Reply))
			return false;
		Reply other = (Reply) o;
		return other.getStatus() == this.status && other.getMessage().equals(this.message);
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Reply{");
		sb.append("conversations=").append(conversations);
		sb.append(", status=").append(status);
		sb.append(", message='").append(message).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
