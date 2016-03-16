package builders;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import domain.Message;
import domain.NetworkFile;
import domain.Request;
import domain.User;
import validators.InputValidator;


/**
 * Esta classe eh um builder de requests
 * Nesta estah encapsulado o conhecimento sobre criacao de diferentes
 * tipos de Request
 * A sua unica funcao eh formularar Requests com base em parametros
 */
public class RequestBuilder {

	/**
	 * Formula um request
	 * @param input
	 * 		Map no qual se vai basear a criacao do Request
	 * @return
	 * 		o Request devido caso valido, null caso contrario
     */
	public static Request make(HashMap<String, String> input) throws FileNotFoundException {
		String flag = input.get("flag");
		//valida flag
		if (!InputValidator.validFlag(flag))
			return null;

		//inicializa user e request base
		User user = UserBuilder.make(input);
		Request request = new Request();
		request.setType(flag);
		request.setUser(user);

		// tratar cada flag de forma diferente
		switch(flag) {
		case "-a":
			request.setContact(input.get("field_1"));
			request.setGroup(input.get("field_2"));
			break;
		case "-d":
			request.setContact(input.get("field_1"));
			request.setGroup(input.get("field_2"));
			break;
		case "-f":
			try {
				request.setContact(input.get("field_1"));
				request.setFile(new NetworkFile(input.get("field_2")));
			} catch(Exception e) {
				throw new FileNotFoundException();
			}
			break;
		case "-r":
			request.setSpecification("all");

			//se eh especifico para um contacto
			if ( input.containsKey("field_1") ) {
				request.setSpecification("single_contact");
				request.setContact(input.get("field_1"));
				//se eh para fazer o download de um file
				if ( input.containsKey("field_2") ) {
					request.setSpecification("download");
					request.setFile(new NetworkFile(input.get("field_2")));
				}
			}
			break;
		case "-m":
			String to = input.get("field_1");
			String body = input.get("field_2");
			request.setContact(to);
			request.setMessage(new Message(user.getName(), to, body));
			break;
		}
		return request;
	}
}