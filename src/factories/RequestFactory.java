package factories;

import java.util.Scanner;

import common.Request;
import common.User;

public class RequestFactory {

	private String[]args;
	
	public RequestFactory(String[] args){
		this.args = args;
	}
	
	public Request make(){
		
		if(args.length < 4)
			return null;
		
		Request request = new Request();
		
		//ip e port
		String[]address = args[1].split(":");
		if( address.length < 2 )
			return null;
		request.setIP(address[0]);
		request.setPort(Integer.parseInt(address[1]));
		
		//user
		User user = new User(this.args[0]);
		int i = 2;
		//se nao indicou pass
		if( !args[2].equals("-p") )
		{
			Scanner sc = new Scanner(System.in);
			String pwd = sc.nextLine();
			user.setPassword(pwd);
		}
		//se indicou pass
		else{
			i++;
			user.setPassword(args[i++]);
		}
		request.setUser(user);
		
		
		//request type
		switch(args[i]){
		case "-m":
			i++;
			request.setType("m");
			request.setMessage(args[i++], args[i]);
			break;
		case "-r":
			break;
		case "-f":
			break;
		case "-a":
			break;
		case "-d":
			break;
		}
		
		return request;
	}
	
}
