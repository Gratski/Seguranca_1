package helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Esta classe é responsável pelo download e upload de ficheiros
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class FilesHandler {

	/**
	 * Numero de bytes a ser transmitido de cada vez
	 */
	private static final int CHUNK = 1024;

	/**
	 * Construtor da class FilesHandler
	 */
	public FilesHandler() {}

	/**
	 *	Método que envia o File file, em CHUNK bytes de cada vez
	 *
	 * @param conn
	 * 		Connection que detem o socket por onde vai ser enviado o ficheiro
	 * @param file
	 * 		Ficheiro a ser enviado pela rede
	 * @return
	 * 		Retorna verdadeiro se enviar o mesmo número de bytes que o tamanho em bytes do ficheiro file
	 *
	 * @requires conn != null && file != null
	 * @throws IOException
     */
	public boolean send(Connection conn, File file) throws IOException {
		byte[] byteArr;
		FileInputStream fs = new FileInputStream(file);
		
		//send filesize
		conn.getOutputStream().writeLong(file.length());
		conn.getOutputStream().flush();
		
		//send file itself
		long fileSize = file.length();
		long totalSent = 0;
		while (totalSent < fileSize) {
			int byteNum = 0;
			if ( (totalSent + CHUNK) <= fileSize )
				byteNum = CHUNK;
			else
				byteNum = (int) ( fileSize - totalSent );
				
			byteArr = new byte[byteNum];
			int toSend = fs.read(byteArr, 0, byteNum);

			conn.getOutputStream().write(byteArr, 0, toSend);
			conn.getOutputStream().flush();
			totalSent += toSend;
		}
		fs.close();
		return totalSent == fileSize;
	}

	/**
	 *	Método que recebe da rede o ficheiro com nome filename, em CHUNK bytes de cada vez
	 *  Recebe o tamanho do ficheiro primeiro e depois vai escrevendo em disco à medida que recebe
	 *
	 * @param conn
	 * 		Connection que detem o socket por onde vai ser recebido o ficheiro
	 * @param dir
	 * 		Path onde o ficheiro vai ser gravado
	 * @param filename
	 * 		Nome do ficheiro a ser enviado pela rede
	 * @return
	 * 		Retorna verdadeiro se receber o mesmo número de bytes que o tamanho em bytes do ficheiro filename
	 *
	 * @requires conn != null && filename != null
     * @throws Exception
     */
	public File receive(Connection conn, String dir, String filename) throws Exception {
		byte[] byteArr = new byte[CHUNK];

		//receiving filename
		File file = new File(dir + "/" + filename);
		file.getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(dir + "/" + filename);
		
		//receiving filesize
		long fileSize = conn.getInputStream().readLong();
		long totalRead = 0;
		
		//receiving file itself
		while (totalRead < fileSize) {
			int cur = conn.getInputStream().read(byteArr);
			if (cur == -1)
				continue;
			
			out.write(byteArr, 0, cur);
			totalRead += cur;
		}
		out.close();
		return totalRead == fileSize ? file : null;
	}

	/**
	 * Método que testa se um ficheiro já existe no path recebido
	 * @param path
	 * 		Path para verificar se o ficheiro existe
	 * @return
	 * 		Retorna True se o ficheiro em path já existir, false caso contrário
	 * @requires
	 * 		path != null
     */
	public boolean existsFile(String path) {
		return new File(path).exists();
	}
}