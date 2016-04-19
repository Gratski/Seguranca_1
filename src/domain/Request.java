package domain;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.Certificate;

/**
 * Esta classe representa um pedido de uma acção do cliente para o servidor
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class Request extends NetworkMessage implements Serializable {
	
	private Message message;
	private String contact;
	private String group;
	private NetworkFile file;
	private String specification;

	/**
	 *	Getter para Message
	 *
	 * @return
	 * 		Message que estava guardada
     */
	public Message getMessage() {
		return this.message;
	}

	/**
	 * Setter para Message
	 *
	 * @param message
	 * 		Message a ser guardada
     */
	public void setMessage(Message message) {
		this.message = message;
	}

	/**
	 * Setter para Networkfile
	 *
	 * @param f
	 * 		NetworkFile a ser guardado
     */
	public void setFile(NetworkFile f){
		this.file = f;
	}

	/**
	 * Getter para Networkfile
	 *
	 * @return
	 * 		Devolve o NetworkFile guardado
     */
	public NetworkFile getFile(){
		return this.file;
	}

	/**
	 * Getter para o contact guardado
	 *
	 * @return
	 * 		Devolve o contact guardado
     */
	public String getContact() {
		return this.contact;
	}
	
	
	
	/**
	 * Setter para contact
	 *
	 * @param contact
	 * 		contact a ser guardada
     */
	public void setContact(String contact) {
		this.contact = contact;
	}

	/**
	 * Getter para Group
	 *
	 * @return
	 * 		Devolve Group que está guardado
     */
	public String getGroup() {
		return group;
	}

	/**
	 * Setter para Group
	 *
	 * @param group
	 * 		Group para ser guardado
     */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Setter para especificação
	 * Serve para especificar uma acção em Type
	 *
	 * @param spec
	 * 		Especificação em forma de String
     */
	public void setSpecification(String spec){
		this.specification = spec;
	}

	/**
	 * Getter para especificação
	 *
	 * @return
	 * 		String que denota a especificação
     */
	public String getSpecs(){
		return this.specification;
	}

	/**
	 * Override de toString
	 *
	 * @return
	 * 		Representação textual de um Request
     */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("=== REQUEST ===\n");
		sb.append("User: " + this.getUser().toString() + "\n");
		sb.append("Type: " + this.getType() + "\n");
		sb.append("Message: " + this.getMessage() + "\n");
		sb.append("Contact: " + this.getContact() + "\n");
		sb.append("Group: " + this.getGroup() + "\n");
		return sb.toString();
	}
}