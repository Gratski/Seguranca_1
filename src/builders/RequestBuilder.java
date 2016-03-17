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
 * Esta classe representa a entidade responsavel
 * pela criacao de Requests
 *
 * @author JoaoRodrigues & Simao Neves
 */
public class RequestBuilder {

	/**
	 * Formula um request
	 * @param input Map no qual se vai basear a criacao do Request
	 * @return Request devido caso valido, null caso contrario
	 * @require input != null
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