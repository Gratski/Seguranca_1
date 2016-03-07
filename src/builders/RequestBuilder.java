package builders;

import java.io.File;
import java.util.HashMap;

import common.Message;
import common.NetworkFile;
import common.Request;
import common.User;


public class RequestBuilder {
	
	public static Request make(HashMap<String, String> input) {
		
		String flag = input.get("flag");
		
		// init user
		User user = UserBuilder.make(input);
		
		// verificar o tipo de input
		Request request = null;
		switch(flag) {
		case "-regUser":
			System.out.println("User eh nulo? "+ user == null);
			request = new Request();
			request.setType(flag);
			request.setUser(user);
			break;
		case "-a":
			System.out.println("Adicionar user a group");
			request = new Request();
			request.setUser(user);
			request.setType(flag);
			request.setContact(input.get("field_1"));
			request.setGroup(input.get("field_2"));
			break;
		case "-d":
			request = new Request();
			request.setUser(user);
			request.setType(flag);
			request.setContact(input.get("field_1"));
			request.setGroup(input.get("field_2"));
			break;
		case "-f":
			try {
				request = new Request();
				request.setUser(user);
				request.setType(flag);
				request.setContact(input.get("field_1"));
				request.setFile(new NetworkFile(input.get("field_2")));
			} catch(Exception e) {
				System.out.println("Erro ao abrir ficheiro");
				break;
			}
			break;
		case "-r":
			request = new Request();
			request.setUser(user);
			request.setType(flag);
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

			request = new Request();
			request.setUser(user);
			request.setContact(to);
			request.setType(flag);
			request.setMessage(new Message(user.getName(), to, body));
			break;
		}
		
		System.out.println("FINAL REQUEST");
		System.out.println(request.toString());
		return request;
		
	}
	
}
