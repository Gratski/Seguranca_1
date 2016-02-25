package builders;

import java.io.File;

import common.Message;
import common.Request;
import common.User;

import builders.UserBuilder;


public class RequestBuilder {
	
	public static Request make(String[] input){
		
		String flag = null;
		int pos = -1;
		
		//init user
		User user = UserBuilder.make(input);
		
		//verificar o tipo de input
		Request request = null;
		switch(flag){
		case "-a":
			try{
				pos++;
				request = new Request();
				request.setUser(user);
				request.setGroup(input[pos++]);
				request.setContact(new User(input[pos]));
				request.setType(flag);
			}catch(ArrayIndexOutOfBoundsException e){
				break;
			}
			break;
		case "-d":
			try{
				pos++;
				request = new Request();
				request.setGroup(input[pos++]);
				request.setContact(new User(input[pos]));
				request.setType(flag);
				request.setUser(user);
			}catch(ArrayIndexOutOfBoundsException e){
				break;
			}
			break;
		case "-f":
			try{
				File file = new File(input[++pos]);
				request = new Request();
				request.setFile(file);
				request.setType(flag);
				request.setUser(user);
			}catch(ArrayIndexOutOfBoundsException e){
				break;
			}catch(Exception e){
				System.out.println("Erro ao abrir ficheiro");
				break;
			}
			break;
		case "-r":
			request = new Request();
			request.setType(flag);
			request.setUser(user);
			//se eh de um contact em especifico
			if( input.length == (pos + 2) )
				request.setContact(new User(input[++pos]));
			break;
		case "-m":
			try{
				pos++;
				String to = input[pos++];
				String body = input[pos];
				request = new Request();
				request.setType("-m");
				request.setUser(user);
				request.setMessage(new Message(user.getName(), to, body));
			}catch(ArrayIndexOutOfBoundsException e){
				break;
			}
			break;
		}
		
		return request;
		
	}
	
}
