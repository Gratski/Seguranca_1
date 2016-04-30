package exception;

/**
 * Classe que representa uma Exception de segurança
 */
public class SecurityException extends Exception{

	private String msg;

	/**
	 * Constructor
	 *
	 * @param msg Mensagem que explica o erro que ocorreu
     */
	public SecurityException(String msg){
		super();
		this.msg = msg;
	}
	
	@Override
	public String getMessage(){
		return this.msg;
	}
	
}
