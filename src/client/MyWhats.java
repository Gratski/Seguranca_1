package client;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import builders.RequestBuilder;
import common.Reply;
import common.Request;
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
			Reply reply = null;
			if (download_error)
				reply = new Reply(400, "Erro ao descarregar ficheiro");
			else if (upload_error)
				reply = new Reply(400, "Erro ao enviar ficheiro");
			else
				reply = receiveReply(connection);

			// Se erro
			if (reply.hasError()) {
				System.out.println("There was an error, Reply received:");
				System.out.println(reply.getMessage());
			} else {
				// TODO Just for le debugging, erase in the end
				System.out.println("Reply received:");
				System.out.println(reply);
			}

			// Fecha connection
			connection.destroy();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Aplicação terminada. Estabelecer nova ligacao.");
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

		// file type request handler
		switch (req.getType()) {
		case "-f":
			System.out.println("FILENAME: " + req.getFile().getFullPath());
			FilesHandler fHandler = new FilesHandler();
			try {
				System.out.println("Sending file...");
				Reply auth = (Reply) conn.getInputStream().readObject();
				if(auth.getStatus() != 200)
				{
					upload_error = true;
					break;
				}
				
				fHandler.send(conn, new File(req.getFile().getFullPath()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case "-m":
			try {
				System.out.println("Sending message...");
				conn.getOutputStream().writeObject(req);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case "-r":
			switch (req.getSpecs()) {
			case "download":
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
				break;
			case "all":
				break;
			case "single_contact":
				break;
			}
			break;
		}
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
