package validators;


public class InputValidator {

	public static Boolean validInput(String[] input){
		if (input.length >= 3 && validName(input[0]) && validAddress(input[1])) {
			return ((input[2].equals("-p") && validPassword(input[3]) && validFlag(input[4]))
					 || validFlag(input[2]));
		}
		return false;
	}
	
	public static Boolean validName(String name) {
		return true;
	}
	
	public static Boolean validPassword(String password) {
		return true;
	}
	
	public static Boolean validFlag(String flag) {
		return (flag.equals("-a") || flag.equals("-d") || flag.equals("-r") 
								  || flag.equals("-f") || flag.equals("-m"));
	}
	
	public static Boolean validAddress(String address) {
		
		String[] addressSplit = address.split(":");
		String ip = addressSplit[0];
		int port = Integer.parseInt(addressSplit[1]);
		
		return (port <= 99999 && port >= 0 && ip.matches("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$"));
	}
	
	
}
