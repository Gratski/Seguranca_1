package client;

import java.io.File;
import java.io.FileNotFoundException;
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

/**
 * Esta classe representa o client
 * O modo de uso e as suas operacoes sao em mais detalhe descritas
 * no ficheiro README.info
 *
 * @author Joao Rodrigues && Simao Neves
 */
public class MyWhats {

	private static boolean download_error = false;
	private static boolean upload_error = false;

	public static void main(String[] args) {
		try {
			// input validation
			if (!InputValidator.validInput(args)){
				System.out.println("Parametros mal formed");
				System.exit(-1);
			}

			// parse input
			HashMap<String, String> parsedInput = InputValidator.parseInput(args);

			// estabelece ligacao
			Connection connection = new Connection(
					new Socket(parsedInput.get("ip"), Integer.parseInt(parsedInput.get("port"))));

			// create request obj
			Request request = null;
			try {
				request = RequestBuilder.make(parsedInput);
			} catch (FileNotFoundException e) {
				System.out.println("Ficheiro não encontrado");
				System.exit(-1);
			}

			//validate request before send
			if (request.getUser().getName().equals(request.getContact()) && !request.getType().equals("-d")) {
				System.out.println("O destinatário nao pode ser o remetente.");
				System.out.println("Aplicacao terminada.");
				System.exit(-1);
			}
			
			// send request
			sendRequest(connection, request);
			
			// get reply
			Reply reply = receiveReply(connection);
			
			// Imprime server reply
			reply.prettyPrint(request.getUser());

			// Fecha connection
			connection.destroy();

		} catch (Exception e) {
			System.out.println("Aplicação terminada com erro, tente de novo.");
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

		//se a operacao inclui ficheiros
		if ( isFileOperation(req) ) {
			//obtem autorizacao para enviar/receber ficheiro
			try {
				Reply auth = (Reply) conn.getInputStream().readObject();
				//se nao autorizado
				if (auth.getStatus() != 200) {
					upload_error = true;
					return;
				}
			} catch (ClassNotFoundException e) {
				throw new IOException();
			}
		}

		//se a operacao inclui ficheiros e tem autorizacao
		switch (req.getType()) {
		//caso especial para upload
		case "-f":
			FilesHandler fHandler = new FilesHandler();
			try {
				//enviar file
				fHandler.send(conn, new File(req.getFile().getFullPath()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
			
		//caso especial para download
		case "-r":
			//se file download
			if (req.getSpecs().equals("download")) {
				try {
					File downloaded = new FilesHandler().receive(conn, ".", req.getFile().getFullPath());
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

	private static boolean isFileOperation(Request req){
		String type = req.getType();
		return ( type.equals("-f") || ( type.equals("-r") && req.getSpecs().equals("download") ) );
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
		else {
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
}