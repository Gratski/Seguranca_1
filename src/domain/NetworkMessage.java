package domain;

import java.io.Serializable;

import security.GenericSignature;

/**
 * Esta classe representa a entidade que encapsula
 * a informacao de uma mensagem a ser transmitida
 * pela rede
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class NetworkMessage implements Serializable {

	protected String type;
	private User user;
	private GenericSignature signature;

	/**
	 * Constructor
	 *
	 * Objecto "vazio"
	 */
	public NetworkMessage(){
		this.user = null;
	}
	
	/**
	 * Obtem User
	 *
	 * @param user User de NetworkMessage
     */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Altera o tipo de NetworkMessage
	 *
	 * @param type Novo tipo
     */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Setter para GenericSignature
	 *
	 * @param gs
	 */
	public void setSignature(GenericSignature gs){
		this.signature = gs;
	}
	
	/**
	 * Obtem o tipo
	 *
	 * @return String type
     */
	public String getType() {
		return this.type;
	}

	/**
	 * Obtem User
	 *
	 * @return User de NetworkMessage
     */
	public User getUser(){
		return this.user;
	}

	/**
	 * Retorna a GenericSignature guardada
	 *
	 * @return
     */
	public GenericSignature getSignature(){
		return this.signature;
	}
}
