package common;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import proxies.GroupsProxy;

public class User implements Serializable {

	private String name;
	private String password;
	private List<Group> groups;
	
	public User(String name) {
		this.name = name;
	}
	
	public User(String name, String password){
		this.name = name;

		if ( password == null )
			this.askForPassword();
		else
			this.password = password;
	}

	/**
	 * Pede ao utilizador uma nova palavra-passe
	 */
	private void askForPassword() {
		Scanner sc = new Scanner(System.in);
		String pwd = sc.nextLine();
		while (pwd == null || pwd.length() < 5 || (pwd.split(" ")).length > 1) {
			System.out.println("Insira uma palavra passe valida (5 chars min, sem espacos)");
			pwd = sc.nextLine();
		}
		this.password = pwd;
	}
	
	public Map<String, Group> getGroups() throws IOException {
		if(this.groups == null)
			this.groups = new ArrayList<Group>();
		
		GroupsProxy proxy = GroupsProxy.getInstance();
		return proxy.getGroupsWhereMember(this.name);
	}
	
	public String getName(){
		return this.name;
	}
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
