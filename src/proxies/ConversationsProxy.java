package proxies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import builders.FileStreamBuilder;
import common.Conversation;
import common.User;

public class ConversationsProxy implements Proxy {

	private Map<Conversation, Conversation> conversations;
	private static ConversationsProxy instance = null;
	
	private ConversationsProxy() throws IOException{
		this.conversations = new HashMap<>();
		this.fillFromFile();
	}
	
	public static ConversationsProxy getInstance() throws IOException{
		if( instance == null )
			instance = new ConversationsProxy();
		return instance;
	}
	
	
	/**
	 * Fill conversations Set from database
	 * @throws IOException
	 */
	private void fillFromFile() throws IOException{
		
		BufferedReader reader = FileStreamBuilder.makeReader("DATABASE/CONVERSATIONS/INDEX");
		String line = null;
		while( ( line = reader.readLine() ) != null ){
			
			String[] lineSplit = line.split(" ");
			User u1 = new User(lineSplit[0]);
			User u2 = new User(lineSplit[1]);
			Conversation c = new Conversation(u1,u2, lineSplit[2]);
			this.conversations.put(c, c);
		}
		reader.close();
	}
	
	/**
	 * Verifica se uma conversation exist
	 * @param c
	 * 		Conversation a considerar
	 * @return
	 * 		true se existe, false caso contrario
	 */
	public boolean exists(Conversation c){
		return this.conversations.get(c) != null;
	}
	
	public Conversation add(String from, String to) throws IOException{
		
		int id = this.conversations.size() + 1;
		Conversation c = new Conversation(new User(from), new User(to), ""+id);
		this.conversations.put(c, c);
		
		//save on file
		BufferedWriter writer = FileStreamBuilder.makeWriter("DATABASE/CONVERSATIONS/INDEX", true);
		StringBuilder sb = new StringBuilder();
		sb.append(from);
		sb.append(" " + to);
		sb.append(" " + id);
		sb.append("\n");
		
		writer.write(sb.toString());
		writer.flush();
		writer.close();
		
		return c;
		
	}
	
	public Conversation getConversation(String u1, String u2){
		Conversation c = new Conversation(new User(u1), new User(u2), "");
		return this.conversations.get(c);
	}
	
	/**
	 * Get
	 * @param u1
	 * @param u2
	 * @return
	 */
	
	
	@Override
	public void destroy() throws IOException {
		// TODO Auto-generated method stub
		
	}

}
