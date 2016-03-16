package builders;

import java.util.HashMap;
import java.util.Scanner;

import domain.User;
import validators.InputValidator;

/**
 * Esta classe representa a entidade responsavel
 * pela criacao de Users
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class UserBuilder {

	/**
	 * Cria um User com base no input
	 * @param  input representa o input
	 * @return       um novo User
	 */
	public static User make(HashMap<String, String> input) {
		String password = input.get("password");
		
		// se não passou a password em args
		if (password == null) {
			Scanner sc = new Scanner(System.in);
			do {
				System.out.println("Insira a palavra-passe (sem espaços):");
				password = sc.nextLine();
			} while (!InputValidator.validPassword(password) || password.split(" ").length > 1);
			sc.close();
		}
		return new User(input.get("username"), password);
	}
	
}