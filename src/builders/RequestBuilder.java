package builders;

import java.io.FileNotFoundException;
import java.util.HashMap;

import domain.Message;
import domain.NetworkFile;
import domain.Request;
import domain.User;
import validators.InputValidator;


/**
 * This class represents an entity that is responsible for
 * the creation of Request objects based on given parameters
 *
 * @author JoaoRodrigues & Simao Neves
 */
public class RequestBuilder {

	/**
	 * Formulates a new Request object
	 * As there are some options that require more parameters
	 * than others, this method considers two possible extra fields
	 * used in some options. Their names are field_1 and field_2 respectively
	 * 
	 * @param input, map with the input parameters
	 * @return The corresponding Request object if is a valid input,
	 * 			otherwise null
	 * @require input != null
     */
	public static Request make(HashMap<String, String> input) throws FileNotFoundException {

		String flag = input.get("flag");
		
		// flag validation
		if (!InputValidator.validFlag(flag))
			return null;

		// initialize user and base request
		User user = UserBuilder.make(input);
		Request request = new Request();
		request.setType(flag);
		request.setUser(user);

		// handle request based on given flag
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

			// if it is for a specific contact
			if ( input.containsKey("field_1") ) {
				request.setSpecification("single_contact");
				request.setContact(input.get("field_1"));
				
				// if it is a file download
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