package builders;

import java.io.File;
import java.util.HashMap;

import common.Message;
import common.NetworkFile;
import common.Request;
import common.User;


public class RequestBuilder {
	
	public static Request make(HashMap<String, String> input){
		
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
			try {
				request = new Request();
				request.setUser(user);
				request.setType(flag);
				request.setGroup(input.get("field_2"));
				request.setContact(input.get("field_1"));
			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}
			break;
		case "-d":
			try {

				request = new Request();
				request.setUser(user);
				request.setType(flag);
				request.setGroup(input.get("field_2"));
				request.setContact(input.get("field_1"));
			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}
			break;
		case "-f":
			try {
				request = new Request();
				request.setUser(user);
				request.setType(flag);
				request.setContact(input.get("field_1"));
				request.setFile(new NetworkFile(input.get("field_2")));
			} catch (ArrayIndexOutOfBoundsException e) {

				break;
			} catch(Exception e) {
				System.out.println("Erro ao abrir ficheiro");
				break;
			}
			break;
		case "-r":
			request = new Request();
			request.setUser(user);
			request.setType(flag);
			//se eh de um contact em especifico
			// if( input.length == (pos + 2) )
			// 	request.setContact(new User(input[++pos]));
			// 	
			break;
		case "-m":
			System.out.println("ITS A MESSAGE");
			try {
				String to = input.get("field_1");
				String body = input.get("field_2");
				
				request = new Request();
				request.setUser(user);
				request.setContact(to);
				request.setType(flag);
				request.setMessage(new Message(user.getName(), to, body));
			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}
			break;
		}
		
		System.out.println("FINAL REQUEST");
		System.out.println(request.toString());
		return request;
		
	}
	
}
