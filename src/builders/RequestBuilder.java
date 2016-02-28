package builders;

import java.io.File;
import java.util.HashMap;

import common.Message;
import common.Request;
import common.User;
import builders.UserBuilder;


public class RequestBuilder {
	
	public static Request make(HashMap<String, String> input){
		
		String flag = input.get("flag");
		
		// init user
		User user = UserBuilder.make(input);
		
		System.out.println("Building request");
		
		// verificar o tipo de input
		Request request = null;
		switch(flag){
		case "-regUser":
			System.out.println("User eh nulo? "+ user == null);
			request = new Request();
			request.setType(flag);
			request.setUser(user);
			break;
		case "-a":
			System.out.println("Adicionar user a group");
			try{
				request = new Request();
				request.setUser(user);
				request.setType(flag);
				request.setGroup(input.get("type"));
				request.setContact(new User(input.get("contact")));
			}catch(ArrayIndexOutOfBoundsException e){
				break;
			}
			break;
		case "-d":
			try{
				request = new Request();
				request.setUser(user);
				request.setType(flag);
				request.setGroup(input.get("type"));
				request.setContact(new User(input.get("contact")));
			}catch(ArrayIndexOutOfBoundsException e){
				break;
			}
			break;
		case "-f":
			try{
				File file = new File(input.get("type"));
				request = new Request();
				request.setUser(user);
				request.setType(flag);
				request.setContact(new User(input.get("contact")));
				request.setFile(file);
			}catch(ArrayIndexOutOfBoundsException e){
				break;
			}catch(Exception e){
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
			try{
				String to = input.get("contact");
				String body = input.get("type");
				
				System.out.println("CONTACT NAME: " + to);
				System.out.println("MESSAGE BODY: " + body);
				
				
				request = new Request();
				request.setUser(user);
				request.setType(flag);
				request.setMessage(new Message(user.getName(), to, body));
				System.out.println("FILLED aLL");
			}catch(ArrayIndexOutOfBoundsException e){
				break;
			}
			break;
		}
		
		System.out.println("FINAL REQUEST");
		System.out.println("Request type: " + request.getType());
		return request;
		
	}
	
}
