package proxies;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import domain.Message;

/**
 * Esta classe
 */
public class MessagesProxy extends Proxy {

	private static MessagesProxy instance = null;

	private MessagesProxy() throws IOException {}
	
	public static MessagesProxy getInstance() throws IOException {
		if(instance == null)
			instance = new MessagesProxy();
		return instance;
	}
	
	/**
	 * Regista uma mensagem
	 * @param path
	 * 		Path onde a mensagem vai ser registada
	 * @param fname
	 * 		Nome de ficheiro de mensagem
	 * @param msg
	 * 		Mensagem a ser registada
	 * @return
	 * 		true se registou, false caso contrario
	 */
	public boolean persist(String path, String fname, Message msg ) {
		File file = new File(path + "/" + fname + "" + MESSAGE_FILE_EXTENSION);
		if (file.exists())
			return false;

		try {
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(toStoreFormat(msg));
			bw.close();
			fw.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	// PRIVATE
	private String toStoreFormat(Message msg){
		StringBuilder sb = new StringBuilder();
		sb.append(msg.getTimeInMiliseconds() + " ");
		sb.append(msg.getFrom() + " ");
		sb.append(msg.getType() + " ");
		sb.append(msg.getBody() + "\n");
		return sb.toString();
	}
}
