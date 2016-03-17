package validators;

import java.util.HashMap;

/**
 * Esta classe representa um validator de input
 * Neste projecto eh utilizada para validar o input
 * dado aquando da execução do programa client e server
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class InputValidator {

	/**
	 * Valida input com base em regras MyWhats
	 *
	 * @param input Input a ser analisado
	 * @return true se valido, false caso contrario
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
	 * Valida server
	 *
	 * @param input Server a ser considerado
	 * @return true se valido, false caso contrario
     */
	public static Boolean validServerInput(String[] input) {
		return  (input != null && input.length == 1 && validPort(input[0]));
	}

	/**
	 * Valida o nome de User passado como atributo
	 *
	 * @param name Nome a ser considerado
	 * @return true se valido, false caso contrario
     */
	public static Boolean validName(String name) {
		return !name.contains(":") && !name.contains(",");
	}

	/**
	 * Valida a password
	 *
	 * @param password Password a ser considerada
	 * @return true se valida, false caso contrario
     */
	public static Boolean validPassword(String password) {
		return !password.contains(":") && password.length() > 2;
	}

	/**
	 * Valida flag
	 *
	 * @param flag Flag a ser considerada
	 * @return true se valida, false caso contrario
     */
	public static Boolean validFlag(String flag) {
		return (flag.equals("-a") || flag.equals("-d") || flag.equals("-r") 
								  || flag.equals("-f") || flag.equals("-m"));
	}

	/**
	 * Valida IP
	 *
	 * @param ip IP a ser considerada
	 * @return true se valida, false caso contrario
     */
	private static Boolean validIp(String ip) {
		return ip.matches("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");
	}

	/**
	 * Valida porto
	 *
	 * @param port Porto a ser considerado
	 * @return true se valido, false caso contrario
     */
	public static Boolean validPort(String port) {
		int portInt;
		try {
			portInt= Integer.parseInt(port);
		} catch(Exception e) {
			return false;
		}
		return portInt == 23456;
//		return (portInt >= 1024 && portInt <= 65535);
	}

	/**
	 * Valida endereco com IP e Porto
	 *
	 * @param address Endereco a ser considerado
	 * @return true se valido, false caso contrario
     */
	public static Boolean validAddress(String address) {
		String[] addressSplit = address.split(":");
		return (validPort(addressSplit[1]) && validIp(addressSplit[0]));
	}

	/**
	 * Devolve um Input mais facil de utilizar
	 *
	 * @param args Input base a ser considerado
	 * @return Nova estrutura de Input
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
