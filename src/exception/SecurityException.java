package exception;

/**
 * This class represents a security exception
 * This exception type is only used when an attack or
 * possible attack is identified
 * 
 * @author Joao Rodrigues & Simao Neves
 *
 */
public class SecurityException extends Exception{

	/**
	 * exception message
	 */
	private String msg;

	/**
	 * Constructor
	 * 
	 * @param msg, exception message
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
