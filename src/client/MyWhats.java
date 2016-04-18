package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.HashMap;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import builders.RequestBuilder;
import domain.Reply;
import domain.Request;
import helpers.Connection;
import helpers.FilesHandler;
import validators.InputValidator;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Esta classe representa o client
 * O modo de uso e as suas operacoes sao em mais detalhe descritas
 * no ficheiro README.info
 *
 * @author Joao Rodrigues && Simao Neves
 */
public class MyWhats {

	/**
	 * flag utilizada para identifica erro em operacao
	 * download e upload respectivamente
	 */
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
				if (request.getType().equals("-m") || request.getType().equals("-f")) {
					System.out.println("O destinatário nao pode ser o remetente.");
					System.out.println("Aplicacao terminada.");
					System.exit(-1);
				} else if (request.getType().equals("-a")) {
					System.out.println("Não se pode adicionar a si próprio a um grupo.");
					System.out.println("Aplicacao terminada.");
					System.exit(-1);
				}
			}

			// estabelece ligacao
			System.setProperty("javax.net.ssl.trustStore", "certificates.trustStore");
			SocketFactory sf = SSLSocketFactory.getDefault();
			Socket socket = sf.createSocket(parsedInput.get("ip"), Integer.parseInt(parsedInput.get("port")));
			Connection connection = new Connection(socket);

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
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	/**
	 * This method handles a request sending
	 * 
	 * @param conn Connection to be considered
	 * @param req Request to be considered
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

	/**
	 * Verifica se a operacao envolve ficheiros
	 *
	 * @param req Request a considerar
	 * @return true se opracao de ficheiros, false caso contrario
	 * @require req != null
     */
	private static boolean isFileOperation(Request req){
		String type = req.getType();
		return ( type.equals("-f") || ( type.equals("-r") && req.getSpecs().equals("download") ) );
	}

	/**
	 * Recebe um objecto Reply do servidor
	 * 
	 * @param conn Connection considerada na ligacao
	 * @return Reply com resposta
	 * @throws Exception
	 * @require conn != null
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