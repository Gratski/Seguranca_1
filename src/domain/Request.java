package domain;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.Certificate;

/**
 * This class represents an entity: Request
 * A Request is a type of network message used when a client
 * sends a request to the server
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class Request extends NetworkMessage implements Serializable {
	
	/**
	 * message object
	 */
	private Message message;
	
	/**
	 * message contact name
	 */
	private String contact;
	
	/**
	 * message group name
	 */
	private String group;
	
	/**
	 * message file
	 */
	private NetworkFile file;
	
	/**
	 * message specification if there is any
	 */
	private String specification;

	/**
	 *	Gets message
	 *
	 * @return network message
     */
	public Message getMessage() {
		return this.message;
	}

	/**
	 * Sets message value
	 *
	 * @param message, new message to be set
     */
	public void setMessage(Message message) {
		this.message = message;
	}

	/**
	 * Sets the network file
	 *
	 * @param f, new network file
     */
	public void setFile(NetworkFile f){
		this.file = f;
	}

	/**
	 * Gets network file
	 *
	 * @return NetworkFile
     */
	public NetworkFile getFile(){
		return this.file;
	}

	/**
	 * Gets the message contact
	 *
	 * @return message contact
     */
	public String getContact() {
		return this.contact;
	}
	
	/**
	 * Sets contact value
	 *
	 * @param contact, new contact value
     */
	public void setContact(String contact) {
		this.contact = contact;
	}

	/**
	 * Gets message group
	 *
	 * @return message group
     */
	public String getGroup() {
		return group;
	}

	/**
	 * Sets message group
	 *
	 * @param group, new message group
     */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Sets message specification
	 *
	 * @param spec, new specification value
     */
	public void setSpecification(String spec){
		this.specification = spec;
	}

	/**
	 * Gets message specification
	 *
	 * @return message specification
     */
	public String getSpecs(){
		return this.specification;
	}

	/**
	 * Checks this message is of type -m (message)
	 * 
	 * @return true if so, false otherwise
	 */
	public Boolean isMessage() {
		return this.type.equals("-m");
	}

	/**
	 * Checks if this message has a file set
	 *
	 * @return true if so, false otherwise
	 */
	public Boolean isFileOperation() {
		return ( this.type.equals("-f") || ( this.type.equals("-r") && this.specification.equals("download") ) );
	}

	/**
	 * Checks if this message is of type -f (file upload)
	 * 
	 * @return true if so, false otherwise
     */
	public Boolean isFileUpload(){
		return this.type.equals("-f");
	}

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