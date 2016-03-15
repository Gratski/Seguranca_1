package helpers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection {

	private Socket socket;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	
	public Connection(Socket socket) throws IOException {
		this.socket = socket;
		this.output = new ObjectOutputStream(socket.getOutputStream());
		this.input = new ObjectInputStream(socket.getInputStream());
	}
	
	/**
	 * Closes an existing connection
	 * @throws IOException
	 */
	public void destroy() throws IOException {
		this.output.close();
		this.input.close();
		this.socket.close();
	}
	
	public ObjectOutputStream getOutputStream(){
		return this.output;
	}

	public ObjectInputStream getInputStream(){
		return this.input;
	}
}