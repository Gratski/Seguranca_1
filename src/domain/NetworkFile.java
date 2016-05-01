package domain;

import java.io.File;
import java.io.Serializable;

/**
 * This class represents an entity: Network File
 * A network file is a file that can be sent
 * both from client to server and from server to client
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class NetworkFile implements Serializable {

	/**
	 * file descriptor 
	 */
	private File file;
	
	/**
	 * file path/name.ext
	 */
	private String fullPath;

	/**
	 * Constructor
	 *
	 * @param fullPath file path
     */
	public NetworkFile(String fullPath) {
		this.fullPath = fullPath;
		this.file = new File(fullPath);
	}

	/**
	 * Gets the file full path
	 *
	 * @return file full path including file name
     */
	public String getFullPath(){
		return this.fullPath;
	}

	/**
	 * Gets file descriptor
	 *
	 * @return file descriptor
     */
	public File getFile(){
		return this.file;
	}
	
}
