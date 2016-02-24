package validators;

import java.io.File;
import java.io.IOException;

import builders.UserBuilder;
import common.Message;
import common.Request;
import common.User;

public class InputValidator {

	public InputValidator(){}
	
	public Request validInput(String[]input){
		int pos = -1;
		String flag = null;
		for(int i=0; i < input.length; i++)
		{
			if(input[i].equals("-a") || input[i].equals("-d") || input[i].equals("-r")
					|| input[i].equals("-f") || input[i].equals("-m")){
				pos = i;
				flag = input[i];
			}
		}
		
		if(pos == -1)
			return null;
		
		//init user
		UserBuilder ub = new UserBuilder();
		User user = ub.make(input);
		
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
