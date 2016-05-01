package exception;

public class ApplicationException extends Exception{

	private String message;
	
	public ApplicationException(String message){
		super();
		this.message = message;
	}
	public <T extends Exception> ApplicationException(String msg, T e){
		super(e);
		this.message = msg;
	}
	
	@Override
	public String getMessage(){
		return this.message;
	}
	
}
