package domain;

import java.io.Serializable;

/**
 * Esta classe representa a entidade Utilizador
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class User implements Serializable {

	private String name;
	private String password;

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
	public User(String name, String password){
		this.name = name;
		this.password = password;
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
	public String getPassword(){
		return this.password;
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
