package domain;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import proxies.GroupsProxy;
import validators.InputValidator;

/**
 * Esta classe representa a entidade Utilizador
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class User implements Serializable {

	private String name;
	private String password;
	private List<Group> groups;

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
	 * Pede ao utilizador uma nova palavra-passe
	 */
	private void askForPassword() {
		Scanner sc = new Scanner(System.in);
		String pwd = sc.nextLine();
		while (pwd == null || (pwd.split(" ")).length > 1 || !InputValidator.validPassword(pwd)) {
			System.out.println("Insira uma palavra passe valida (5 chars min, sem espacos)");
			pwd = sc.nextLine();
		}
		this.password = pwd;
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
