package exception;

public class SecurityException extends Exception{

	private String msg;
	
	public SecurityException(String msg){
		super();
		this.msg = msg;
	}
	
	@Override
	public String getMessage(){
		return this.msg;
	}
	
}
