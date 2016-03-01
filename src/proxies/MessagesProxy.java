package proxies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import common.Message;
import common.User;
import enums.Filenames;

public class MessagesProxy {

	private static MessagesProxy instance = null;
	private File file;
	private FileWriter fw;
	private BufferedWriter bw;
	
	
	private MessagesProxy() throws IOException{
		this.init();
	}
	
	public static MessagesProxy getInstance() throws IOException{
		if(instance == null)
			instance = new MessagesProxy();
		return instance;
	}
	
	/**
	 * Inicializa os streams de messages
	 * @throws IOException
	 */
	private void init() throws IOException{
		this.file = new File(Filenames.MESSAGES.toString());
		if( !file.exists() )
			file.createNewFile();
		this.fw = new FileWriter(this.file, true);
		this.bw = new BufferedWriter(this.fw);
	}
	
	/**
	 * Get messages between to contacts
	 * @param user
	 * 		user a fazer o request
	 * @param contact
	 * 		contact with
	 * @return
	 * 		lista de mensagens
	 * @throws IOException
	 */
	public Message[] getMessages(User user, String contact) throws IOException{
		File f = new File(Filenames.MESSAGES.toString());
		FileReader fr = new FileReader(f);
		BufferedReader bf = new BufferedReader(fr);
		String line = null;
		ArrayList<Message> messages = new ArrayList<>();
		while ((line = bf.readLine())!=null) {
			
			String[] arr = line.split(" ");
			if (arr.length < 3)
				continue;
			
			if (!this.isBetween(line, user.getName(), contact))
				continue;
			
			Message msg = new Message(arr[1], arr[2], arr[5]);
			messages.add(msg);
		}
		return (Message[]) messages.toArray();
	}
	
	public boolean addMessage(String from, String to, String flag, String body) throws IOException{

		Message msg = new Message(from, to, body);
		String msgToWrite = msg.getDateString() + " " + from + " " + to + " " + flag + " " + "-m " + "\"" + body + "\"";
		
		this.bw.write(msgToWrite);
		this.bw.flush();
		
		return true;
	}
	
	
	private boolean isBetween(String line, String user, String contact){
		String[]arr = line.split(" ");
		if( arr[1].equals(user) && arr[2].equals(contact) )
			return true;
		if( arr[2].equals(user) && arr[1].equals(contact) )
			return true;
		return false;
	}
	
	
	public ArrayList<Message> getMessages(String filename){
		return null;
	}
	
	public Message getLastMessage(String filename){
		return null;
	}
	
}
