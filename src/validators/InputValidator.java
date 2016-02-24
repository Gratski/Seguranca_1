package validators;

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
			break;
		case "-d":
			break;
		case "-f":
			break;
		case "-r":
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
