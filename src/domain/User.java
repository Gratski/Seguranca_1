package domain;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.Certificate;

/**
 * This class represents an entity: User
 * A user is an application user
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class User implements Serializable {

	/**
	 * user name
	 */
	private String name;
	
	/**
	 * user password
	 */
	private byte[] password;
	
	/**
	 * user password salt
	 */
	private byte[] salt;
	
	/**
	 * user private key
	 */
	private transient PrivateKey privateKey;
	
	/**
	 * user certificate containing public key
	 */
	private Certificate certificate;
	
	/**
	 * Constructor
	 *
	 * @param name, user name
     */
	public User(String name) {
		this.name = name;
	}

	/**
	 * Constructor
	 *
	 * @param name, user name
	 * @param password, user password
     */
	public User(String name, byte[] password){
		this.name = name;
		this.password = password;
	}

	/**
	 * Constructor
	 * 
	 * @param name, user name
	 * @param password, user password
	 * @param salt, user password salt
	 */
	public User(String name, byte[] password, byte[] salt){
		this.name = name;
		this.password = password;
		this.salt = salt;
	}

	/**
	 * Gets user name
	 *
	 * @return user name
     */
	public String getName(){
		return this.name;
	}

	/**
	 * Gets user password
	 *
	 * @return user password
     */
	public byte[] getPassword(){
		return this.password;
	}

	/**
	 * Gets user certificate
	 * 
	 * @return user certificate
	 */
	public Certificate getCertificate(){
		return this.certificate;
	}
	
	/**
	 * Gets user private key
	 * 
	 * @return user private key
	 */
	public PrivateKey getPrivateKey(){
		return this.privateKey;
	}

	/**
	 * Sets user private key
	 * 
	 * @param k, user new private key
	 */
	public void setPrivateKey(PrivateKey k){
		this.privateKey = k;
	}
	
	/**
	 * Sets user certificate
	 * 
	 * @param cert, user new certificate
	 */
	public void setCertificate(Certificate cert){
		this.certificate = cert;
	}
	

	/**
	 * Sets user password salt
	 * 
	 * @param salt, new user password salt
	 */
	public void setSalt(byte[] salt){
		this.salt = salt;
	}

	/**
	 * Sets user password
	 * 
	 * @param pass, user new password
	 */
	public void setPassword(byte[] pass){
		this.password = pass;
	}
	
	/**
	 * Gets user password salt
	 * 
	 * @return user password salt
	 */
	public byte[] getSalt() {
		return this.salt;
	}
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("User{");
		sb.append("name='").append(name).append('\'');
		sb.append(", password='").append(password).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
