package builders;

import java.util.Scanner;

import common.User;

public class UserBuilder {

	public User make(String[]args){
		int i;
		String name = args[0];
		String password = null;
		//se passou a password em args
		if( (i = this.containsPassword(args)) != -1 )
		{
			password = args[i];
		}
		//se nao passou a password em args
		else{
			Scanner sc = new Scanner(System.in);
			do{
				System.out.println("Insira a palavra-passe (sem espaços)");
				password = sc.nextLine();
			}while(password.length() > 3 && (password.split(" ").length) > 1);
			sc.close();
		}
		return new User(name, password);
	}
	
	/**
	 * Verifica se no input existe a flag password
	 * @param args
	 * @return
	 */
	private int containsPassword(String[] args){
		int pos = -1;
		
		for(int i = 0; i < args.length; i++)
		{
			if(args[i].equals("-p"))
				if( (args.length - 1) > i )
				{
					pos = (i+1);
					break;
				}
				else
					break;
		}
		
		return pos;
	}
	
}
