package common;

import java.io.Serializable;
import java.util.Scanner;

public class User implements Serializable{

	private String name;
	private String password;
	
	public User(String name) {
		this.name = name;
	}
	
	public User(String name, String password){
		this.name = name;

		if( password == null )
			this.askForPassword();
		else
			this.password = password;
	}

	/**
	 * Pede ao utilizador uma nova palavra-passe
	 */
	private void askForPassword(){
		Scanner sc = new Scanner(System.in);
		String pwd = sc.nextLine();
		while(pwd == null || pwd.length() < 5 || (pwd.split(" ")).length > 1){
			System.out.println("Insira uma palavra passe valida (5 chars min, sem espacos)");
			pwd = sc.nextLine();
		}
		this.password = pwd;
	}
	
	public String getName(){
		return this.name;
	}
	public String getPassword(){
		return this.password;
	}

	public String toString() {
		return this.name + ":" + this.password;
	}

}
