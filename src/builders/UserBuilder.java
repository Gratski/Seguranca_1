package builders;

import java.util.HashMap;
import java.util.Scanner;

import domain.User;
import validators.InputValidator;

public class UserBuilder {

	/**
	 * Used to build a User, based on the input given on the command line
	 * @param  input represents the input given in the command line
	 * @return       the new User
	 */
	public static User make(HashMap<String, String> input) {
		String password = input.get("password");
		
		// se não passou a password em args
		if (password == null) {
			Scanner sc = new Scanner(System.in);
			do {
				System.out.println("Insira a palavra-passe (sem espaços)");
				password = sc.nextLine();
			} while (!InputValidator.validPassword(password) || password.split(" ").length > 1);
			sc.close();
		}
		return new User(input.get("username"), password);
	}
	
}