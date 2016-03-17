package helpers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Esta classe é responsável por gerir a conexão de um Socket
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class Connection {

	private Socket socket;
	private ObjectOutputStream output;
	private ObjectInputStream input;

	/**
	 * Constructor de uma Connection
	 *
	 * @param socket
	 * 		Socket que vai ser utilizado na ligação
	 * @requires
	 * 		socket != null
	 * @throws IOException
     */
	public Connection(Socket socket) throws IOException {
		this.socket = socket;
		this.output = new ObjectOutputStream(socket.getOutputStream());
		this.input = new ObjectInputStream(socket.getInputStream());
	}
	
	/**
	 * Fecha uma Connection aberta
	 *
	 * @throws IOException
	 */
	public void destroy() throws IOException {
		this.output.close();
		this.input.close();
		this.socket.close();
	}

	/**
	 * Devolve o ObjectOutputStream para a socket
	 *
	 * @return
	 * 		Stream para ser usado na trasacção de ficheiros/mensagens
     */
	public ObjectOutputStream getOutputStream(){
		return this.output;
	}

	/**
	 * Devolve o ObjectInputStream
	 *
	 * @return
	 * 		Stream para ser usado na trasacção de ficheiros/mensagens
     */
	public ObjectInputStream getInputStream(){
		return this.input;
	}
}