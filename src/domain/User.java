package domain;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.Certificate;

/**
 * Esta classe representa a entidade Utilizador
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class User implements Serializable {

	private String name;
	private byte[] password;
	private byte[] salt;
	private transient PrivateKey privateKey;
	private Certificate certificate;
	
	/**
	 * Constructor
	 *
	 * @param name Nome do Utilizador
     */
	public User(String name) {
		this.name = name;
	}

	/**
	 * Constructor
	 *
	 * @param name Nome de utilizador
	 * @param password Password de utilizador
     */
	public User(String name, byte[] password){
		this.name = name;
		this.password = password;
	}
	
	public User(String name, byte[] password, byte[] salt){
		this.name = name;
		this.password = password;
		this.salt = salt;
	}

	/**
	 * Obtem o valor de nome
	 *
	 * @return actual name
     */
	public String getName(){
		return this.name;
	}

	/**
	 * Obtem o valor da password
	 *
	 * @return actual password
     */
	public byte[] getPassword(){
		return this.password;
	}

	/**
	 * 
	 * @return
	 */
	public Certificate getCertificate(){
		return this.certificate;
	}
	
	/**
	 * 
	 * @return
	 */
	public PrivateKey getPrivateKey(){
		return this.privateKey;
	}
	
	public void setPrivateKey(PrivateKey k){
		this.privateKey = k;
	}
	
	/**
	 * 
	 * @param cert
	 */
	public void setCertificate(Certificate cert){
		this.certificate = cert;
	}
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("User{");
		sb.append("name='").append(name).append('\'');
		sb.append(", password='").append(password).append('\'');
		sb.append('}');
		return sb.toString();
	}
	
	public void setSalt(byte[] salt){
		this.salt = salt;
	}

	public void setPassword(byte[] pass){
		this.password = pass;
	}
	
	public byte[] getSalt() {
		return this.salt;
	}
}
