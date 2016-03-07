package common;

import java.io.File;
import java.io.Serializable;

public class NetworkFile implements Serializable {

	private File file;
	private String fullPath;
	
	public NetworkFile(String fullPath) {
		this.fullPath = fullPath;
		this.file = new File(fullPath);
	}
	
	//GETTERS
	public String getFullPath(){
		return this.fullPath;
	}
	
	public File getFile(){
		return this.file;
	}
	
}
