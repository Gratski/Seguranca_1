package builders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class represents an entity that is responsible for the creation
 * read/write streams
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class FileStreamBuilder {

	/**
	 * Creates a reading stream
	 * @param filename, Filename to be opened
	 * @return BufferedReader, an open and ready reading stream 
	 * @throws IOException
	 */
	public static BufferedReader makeReader(String filename) throws IOException {
		File file = new File(filename);
		FileReader reader = new FileReader(file);
		return new BufferedReader(reader);
	}
	
	/**
	 * Creates a writing stream
	 * @param filename, Filename to be opened
	 * @param append, used to flag b ou w file accessing mode
	 * @return BufferedWriter
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
