package builders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Esta classe foi desenvolvida com o objectivo de automatizar o recurso a canais de leitura
 * a ficheiros.
 */
public class FileStreamBuilder {

	/**
	 * Cria um stream de leitura
	 * @param filename
	 * 		nome do ficheiro a abrir
	 * @return
	 * 		BufferedReader
	 * @throws IOException
	 */
	public static BufferedReader makeReader(String filename) throws IOException {
		File file = new File(filename);
		FileReader reader = new FileReader(file);
		return new BufferedReader(reader);
	}
	
	/**
	 * Cria um stream de escrita
	 * @param filename
	 * 		nome do ficheiro a abrir
	 * @param append
	 * 		se true abre em modo b, se false abre em modo w 
	 * @return
	 * 		BufferedWriter
	 * @throws IOException
	 */
	public static BufferedWriter makeWriter(String filename, boolean append) throws IOException {
		File file = new File(filename);
		if(!file.exists())
			file.createNewFile();
		FileWriter writer = new FileWriter(file, append);
		return new BufferedWriter(writer);
	}
	
	
}
