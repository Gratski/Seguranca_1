package builders;

import java.util.HashMap;
import java.util.Scanner;

import domain.User;
import validators.InputValidator;

/**
 * This class represents an entity that is responsible for
 * the creation of User objects based on the given parameters
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class UserBuilder {

	/**
	 * Creates a new User object based on input
	 * @param  input, input to be considered
	 * @return a new User object
	 * @require input != null
	 */
	public static User make(HashMap<String, String> input) {
		String password = input.get("password");
		
		// ask for password if not given yet
		if (password == null) {
			Scanner sc = new Scanner(System.in);
			do {
				System.out.println("Insira a palavra-passe (sem espaços):");
				password = sc.nextLine();
			} while (!InputValidator.validPassword(password) || password.split(" ").length > 1);
			sc.close();
		}
		return new User(input.get("username"), password.getBytes());
	}
}