package helpers;

public class FilesHandler {

	private String filename;
	private Connection connection;
	
	public FilesHandler(){}
	
	public boolean sendReceive(Connection conn, String filename){
		this.connection = conn;
		this.filename = filename;
		
		return true;
	}
	
	public boolean receiveSend(Connection conn, String filename){
		this.connection = conn;
		this.filename = filename;
		
		return true;
	}
	
	
	public int send(){
		return 0;
	}
	
	public int receive(){
		return 0;
	}
	
}
