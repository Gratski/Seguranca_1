package domain;

import java.io.Serializable;

import security.GenericSignature;

/**
 * This class represents an entity that encapsulates
 * the information of message to be sent
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class NetworkMessage implements Serializable {

	/**
	 * network message type
	 * it can be one of application options
	 * -a, -d, -m, -f, -r
	 */
	protected String type;
	
	/**
	 * network message author
	 */
	private User user;
	
	/**
	 * generic signature object for convenience
	 */
	private GenericSignature signature;

	/**
	 * Constructor
	 *
	 * Empty user object
	 */
	public NetworkMessage(){
		this.user = null;
	}
	
	/**
	 * Sets user
	 *
	 * @param user, new user
     */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Sets network message type
	 *
	 * @param type, new network message type
     */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Sets signature value
	 *
	 * @param gs, new siganture value
	 */
	public void setSignature(GenericSignature gs){
		this.signature = gs;
	}
	
	/**
	 * Gets network message type
	 *
	 * @return network message type
     */
	public String getType() {
		return this.type;
	}

	/**
	 * Gets network message author
	 *
	 * @return User, network message author
     */
	public User getUser(){
		return this.user;
	}

	/**
	 * Gets signature
	 *
	 * @return the signature
     */
	public GenericSignature getSignature(){
		return this.signature;
	}
}
