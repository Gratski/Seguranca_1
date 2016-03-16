package domain;

import java.io.File;
import java.io.Serializable;

/**
 * Esta classe representa a entidade que encapsula
 * a informacao de um ficheiro a ser transmitido pela
 * rede
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class NetworkFile implements Serializable {

	private File file;
	private String fullPath;

	/**
	 * Constructor
	 *
	 * @param fullPath Path de ficheiro
     */
	public NetworkFile(String fullPath) {
		this.fullPath = fullPath;
		this.file = new File(fullPath);
	}

	/**
	 * Obtem caminho de ficheiro
	 *
	 * @return String Path
     */
	public String getFullPath(){
		return this.fullPath;
	}

	/**
	 * Obtem File Descriptor
	 *
	 * @return File ficheiro
     */
	public File getFile(){
		return this.file;
	}
	
}
