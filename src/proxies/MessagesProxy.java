package proxies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import domain.Message;
import enums.Filenames;

/**
 * Esta classe
 */
public class MessagesProxy {

	private static MessagesProxy instance = null;
	private static final String ext = ".msg";
	private File file;
	private FileWriter fw;
	private BufferedWriter bw;
	
	
	private MessagesProxy() throws IOException{
	}
	
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
		File file = new File(path + "/" + fname + "" + ext);
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
		System.out.println("ALL GOOD!");
		return true;
	}
	
	public ArrayList<Message> getMessages(String filename){
		return null;
	}
	
	public Message getLastMessage(String filename){
		return null;
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
