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

	/**
	 * Constructor
	 *
	 * @param name Nome de utilizador
	 * @param password Password de utilizador
	 * @param salt Salt que foi utilizado para gerar hash de password final
     */
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
	 * Getter para certificado de utilizador
	 * 
	 * @return Certificate que representa o certificado
	 */
	public Certificate getCertificate(){
		return this.certificate;
	}
	
	/**
	 * Getter para chave privada do utilizador
	 *
	 * @return Chave privada de utilizador
	 */
	public PrivateKey getPrivateKey(){
		return this.privateKey;
	}

	/**
	 * Setter para chave privada
	 *
	 * @param k Chave privada
     */
	public void setPrivateKey(PrivateKey k){
		this.privateKey = k;
	}
	
	/**
	 * Setter para certificado
	 *
	 * @param cert Certificado para ser guardado
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

	/**
	 * Setter para salt gerado
	 *
	 * @param salt Salt a ser guardado
     */
	public void setSalt(byte[] salt){
		this.salt = salt;
	}

	/**
	 * Setter para password do utilizador
	 *
	 * @param pass Password de utilizador
     */
	public void setPassword(byte[] pass){
		this.password = pass;
	}

	/**
	 * Getter para salt gerado
	 *
	 * @return Salt que está guardado
     */
	public byte[] getSalt() {
		return this.salt;
	}
}
