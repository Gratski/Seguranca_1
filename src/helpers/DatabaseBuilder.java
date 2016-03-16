package helpers;

import java.io.File;
import java.io.IOException;

public class DatabaseBuilder {

	private static final String BASE_PATH = "DATABASE";
	private static final String CONVERSATIONS = "CONVERSATIONS";
	private static final String GROUPS = "GROUP";
	private static final String PRIVATE = "PRIVATE";
	
	/**
	 * Este metodo eh o responsavel pela
	 * criacao da estrutura de ficheiros
	 * @return
	 * 		true se todos estao criados, false caso contrario
	 */
	public boolean make(){
		boolean valid = true;
		try {
			valid = makeDir(BASE_PATH)
					&& makeDir(BASE_PATH + "/" + CONVERSATIONS)
					&& makeDir(BASE_PATH + "/" + CONVERSATIONS + "/" + GROUPS)
					&& makeDir(BASE_PATH + "/" + CONVERSATIONS + "/" + PRIVATE)
					&& makeFile(BASE_PATH, "USERS")
					&& makeFile(BASE_PATH, "GROUPS")
					&& makeFile(BASE_PATH + "/"+CONVERSATIONS, "INDEX");
		} catch(Exception e) {
			valid = false;
		}
		
		return valid;
	}
	
	private boolean makeDir(String path) throws IOException{
		File file = new File(path);
		if(!file.exists())
			file.mkdirs();
		return file.exists();
	}
	
	public boolean makeFile(String path, String filename) throws IOException{
		//directory
		this.makeDir(path);
		
		//file itself
		String fullPath = path + "/" + filename;
		File file = new File(fullPath);
		if (!file.exists())
			file.createNewFile();
		return file.exists();
	}

	public boolean destroy(){
		File directory = new File(BASE_PATH);
		return deleteDirectory(directory);
	}

	public boolean deleteDirectory(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						deleteDirectory(files[i]);
					} else {
						files[i].delete();
					}
				}
			}
		}
		return(directory.delete());
	}
}