package client;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import builders.RequestBuilder;
import domain.*;
import helpers.Connection;
import helpers.FilesHandler;
import validators.InputValidator;

public class MyWhats {

	private static boolean download_error = false;
	private static boolean upload_error = false;

	public static void main(String[] args) {
		try {
			// input validation
			if (!InputValidator.validInput(args)) {
				System.out.println("Parametros mal formed");
				System.exit(-1);
			}

			// parse input
			HashMap<String, String> parsedInput = InputValidator.parseInput(args);

			System.out.println("====================================");
			
			// estabelece ligacao
			Connection connection = new Connection(
					new Socket(parsedInput.get("ip"), Integer.parseInt(parsedInput.get("port"))));

			// create request obj
			Request request = RequestBuilder.make(parsedInput);

			// send request
			sendRequest(connection, request);
			
			// get reply
			Reply reply = receiveReply(connection);
			
			// Imprime server reply
			reply.prettyPrint(request.getUser());

			// Fecha connection
			connection.destroy();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Aplicação terminada.");
		}

	}

	/**
	 * This method handles a request sending
	 * 
	 * @param conn
	 *            Connection to be considered
	 * @param req
	 *            Request to be considered
	 * @throws IOException
	 * @require conn != null && req != null && conn.getOutputStream != null
	 */
	public static void sendRequest(Connection conn, Request req) throws IOException {
		// send base request
		conn.getOutputStream().writeObject(req);

		//caso especial para file upload
		switch (req.getType()) {
		case "-f":
			FilesHandler fHandler = new FilesHandler();
			try {
				//authenticate
				Reply auth = (Reply) conn.getInputStream().readObject();
				
				if(auth.getStatus() != 200)
				{
					upload_error = true;
					break;
				}
				
				//send file
				fHandler.send(conn, new File(req.getFile().getFullPath()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
			
		//caso especial para flag -f
		case "-r":
			
			//se file download
			if(req.getSpecs().equals("download"))
			{
				System.out.println("Downloading filename: " + req.getFile().getFullPath());
				conn.getOutputStream().writeObject(req);
				FilesHandler fileHandler = new FilesHandler();
				try {
					Reply auth = (Reply) conn.getInputStream().readObject();
					if (!(auth.getStatus() == 200)) {
						download_error = true;
						break;
					}

					System.out.println("authenticated!");
					System.out.println("starting download");
					File downloaded = fileHandler.receive(conn, "DOWNLOADS", req.getFile().getFullPath());
					if (downloaded == null)
						System.out.println("Erro ao descarregar ficheiro");
					else
						System.out.println("Descarregamento efectuado");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void printMessage(String name, Message message) {
		String personInPerspective = (name.equals(message.getFrom()) ? "me" : message.getFrom());
		System.out.println(personInPerspective + ": " + message.getBody());
		System.out.println(message.getHumanDateString());
	}

	public static void printReply(Reply reply){
		
		if(reply == null)
			return;
		
		//se erro
		if(reply.hasError())
		{
			System.out.println("Error.");
			System.out.println("Description: " + reply.getMessage());
		}
		//se nao ocorreu um erro
		
	}
	
	/**
	 * Recebe um objecto Reply do servidor
	 * 
	 * @param conn
	 *            Connection considerada na ligacao
	 * @return Reply com resposta
	 * @throws Exception
	 */
	public static Reply receiveReply(Connection conn) {
		
		//se ocorreu um erro ao executar -r contact file
		if (download_error)
			return new Reply(400, "Erro ao descarregar ficheiro");
		//se ocorreu um erro ao enviar file a contact
		else if (upload_error)
			return new Reply(400, "Erro ao enviar ficheiro");
		//caso contrario espera pela resposta
		else
			try {
				return (Reply) conn.getInputStream().readObject();
			} catch (IOException e) {
				return new Reply(400, "");
			} catch (ClassNotFoundException e) {
				// e.printStackTrace();
				return new Reply(400, "");
			}
	}
}
