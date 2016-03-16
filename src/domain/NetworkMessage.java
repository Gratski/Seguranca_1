package domain;

import java.io.Serializable;

/**
 * Esta classe representa a entidade que encapsula
 * a informacao de uma mensagem a ser transmitida
 * pela rede
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class NetworkMessage implements Serializable {

	private String type;
	private User user;

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
}
