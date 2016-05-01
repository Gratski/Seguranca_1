package validators;

import java.util.HashMap;

/**
 * This class represents a validator input
 * In this project it is used to validate command line inputs
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class InputValidator {

	/**
	 * Validates input according to MyWhats rules
	 *
	 * @param input Input to be parsed
	 * @return true if valid, false otherwise
     */
	public static Boolean validInput(String[] input) {
		if (input == null || input.length < 3 || !validName(input[0]) || !validAddress(input[1]))
			return false;
		
		int i = 2;
		if (input[2].equals("-p")) {
			if (!(input.length > 3) || !validPassword(input[3]))
				return false;
			if (input.length == 4)
				return true;
			i = 4;
		}

		if (!validFlag(input[i]))
			return false;
		else if (!input[i].equals("-r") && input.length != (i + 3))
			return false;

		return true;
	}

	/**
	 * Validates server command line input
	 *
	 * @param input, input to be parsed
	 * @return true if valid, false otherwise
     */
	public static Boolean validServerInput(String[] input) {
		return  (input != null && input.length == 1 && validPort(input[0]));
	}

	/**
	 * Validates username string
	 *
	 * @param name, user name to be parsed
	 * @return true if valid, false otherwise
     */
	public static Boolean validName(String name) {
		return !name.contains(":") && !name.contains(",");
	}

	/**
	 * Validates password string
	 *
	 * @param password, password to be parsed
	 * @return true if valid, false otherwise
     */
	public static Boolean validPassword(String password) {
		return !password.contains(":") && password.length() > 2;
	}

	/**
	 * Validates flag according to MyWhats valid operations
	 *
	 * @param flag, flag to be considered
	 * @return true if valid, false otherwise
     */
	public static Boolean validFlag(String flag) {
		return (flag.equals("-a") || flag.equals("-d") || flag.equals("-r") 
								  || flag.equals("-f") || flag.equals("-m"));
	}

	/**
	 * Validates IP address
	 *
	 * @param ip, ip to be parsed
	 * @return true if valid, false otherwise
     */
	private static Boolean validIp(String ip) {
		return ip.matches("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");
	}

	/**
	 * Validates address port
	 *
	 * @param port, address port to be parsed
	 * @return true if valid, false otherwise
     */
	public static Boolean validPort(String port) {
		int portInt;
		try {
			portInt= Integer.parseInt(port);
		} catch(Exception e) {
			return false;
		}
		return portInt == 23456;
	}

	/**
	 * Validates address, both the ip and port
	 *
	 * @param address, complete address to be parsed
	 * @return true if valid, false otherwise
     */
	public static Boolean validAddress(String address) {
		String[] addressSplit = address.split(":");
		return (validPort(addressSplit[1]) && validIp(addressSplit[0]));
	}

	/**
	 * Creates a more handful input object
	 *
	 * @param args, input array to be considered
	 * @return new input object
     */
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

		// flag
		parsedInput.put("flag", args[i++]);
		
		// specification fields
		// <flag> <field_1> <field_2>
		if (args.length > i)
			parsedInput.put("field_1", args[i++]);
		if (args.length > i)
			parsedInput.put("field_2", args[i]);

		return parsedInput;
	}
}