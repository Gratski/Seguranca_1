package validators;

import java.util.HashMap;

public class InputValidator {

	public static Boolean validInput(String[] input) {
		if (input == null || input.length < 3 ) {
			System.out.println("first condition");
			return false;
		}

		if( !validName(input[0]))
		{
			System.out.println("Second");
			return false;
		}

		if( !validAddress(input[1])  )
		{
			System.out.println("Thrid");
			return false;
		}
		
		int i = 2;
		if (input[2].equals("-p")) {
			if (!(input.length > 3) || !validPassword(input[3])) {
				System.out.println("-p invalida");
				return false;
			}
			if (input.length == 4)
				return true;
			i = 4;
		}

		if (!validFlag(input[i])) {
			System.out.println("flag invalida");
			return false;
		}
		else if (!input[i].equals("-r") && input.length != (i + 3)) {
			System.out.println("flag -r incompleta");
			return false;
		}

		return true;
	}

	public static Boolean validServerInput(String[] input) {
		return  (input != null && input.length == 1 && validPort(input[0]));
	}

	public static Boolean validName(String name) {
		return !name.contains(":") && !name.contains(",");
	}
	
	public static Boolean validPassword(String password) {
		return !password.contains(":") && password.length() > 2;
	}
	
	public static Boolean validFlag(String flag) {
		return (flag.equals("-a") || flag.equals("-d") || flag.equals("-r") 
								  || flag.equals("-f") || flag.equals("-m") || flag.equals("-regUser"));
	}

	private static Boolean validIp(String ip) {
		return ip.matches("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");
	}

	public static Boolean validPort(String port) {
		int portInt;
		try {
			portInt= Integer.parseInt(port);
		} catch(Exception e) {
			return false;
		}
		//return portInt == 23456;
		return (portInt >= 1024 && portInt <= 65535);
	}
	
	public static Boolean validAddress(String address) {
		String[] addressSplit = address.split(":");
		return (validPort(addressSplit[1]) && validIp(addressSplit[0]));
	}

	public static HashMap<String, String> parseInput(String[] args) {
		HashMap<String, String> parsedInput = new HashMap<>();
		parsedInput.put("username", args[0]);

		String[] addressSplit = args[1].split(":");

		parsedInput.put("ip", addressSplit[0]);
		parsedInput.put("port", addressSplit[1]);

		int i = 2;
		if (args[2].equals("-p")) {
			parsedInput.put("passwordFlag", args[2]);
			parsedInput.put("password", args[3]);
			i = 4;
		} else {
			parsedInput.put("password", null);
			parsedInput.put("passwordFlag", null);
		}
		
		//se eh apenas para registar user
		if ( parsedInput.get("password") != null && args.length == 4 ) {
			System.out.println("É apenas para registar user");
			parsedInput.put("flag", "-regUser");
			return parsedInput;
		}
		//flag
		parsedInput.put("flag", args[i++]);
		
		//specification fields
		//<flag> <field_1> <field_2>
		if (args.length > i)
			parsedInput.put("field_1", args[i++]);
		if (args.length > i)
			parsedInput.put("field_2", args[i]);

		return parsedInput;
	}
	
	
}
