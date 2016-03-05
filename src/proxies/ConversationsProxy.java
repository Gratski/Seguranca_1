package proxies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import builders.FileStreamBuilder;
import common.Conversation;
import common.Message;
import common.User;

/**
 * Esta classe representa o responsavel pelo acesso e manutencao
 * das conversacoes entre utilizadores
 * @author JoaoRodrigues & SimãoNeves
 *
 */
public class ConversationsProxy implements Proxy {

	private Map<Conversation, Conversation> conversations;
	private static ConversationsProxy instance = null;
	
	private ConversationsProxy() throws IOException{
		this.conversations = new HashMap<>();
	}
	
	public static ConversationsProxy getInstance() throws IOException{
		if( instance == null )
			instance = new ConversationsProxy();
		return instance;
	}
	
	/**
	 * Este metodo devolve o nome do directorio onde esta
	 * a informacao relativa ah conversa
	 * @param user1
	 * 		Nome de utilizador 1
	 * @param user2
	 * 		Nome de utilizador 2
	 * @return
	 * 		nome do directorio caso a conversa exista,
	 * 		null caso contrario
	 * @throws IOException
	 */
	private String getConversationID(String user1, String user2) throws IOException{
		
		File f = new File("DATABASE/CONVERSATIONS/INDEX");
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		String res = null;
		
		while((line = br.readLine())!= null){
			
			String[]split = line.split(" ");
			if(split.length <2)
				continue;
			
			String u1 = split[0];
			String u2 = split[1];
			String dir = split[2];
			
			if( (user1.equals(u1) && user2.equals(u2) )
					|| ( user2.equals(u1) && user1.equals(u2) ) )
			{
				res = dir;
				break;
			}
			
		}
		
		return res;
	}
	
	
	private int getNextID() throws IOException{
		File f = new File("DATABASE/CONVERSATIONS/PRIVATE");
		if( f.list() != null )
			return f.list().length + 1;
		return -1;
	}
	
	
	/**
	 * Insere uma nova mensagem numa conversacao
	 * nova ou existente
	 * @param from
	 * 		autor da mensagem
	 * @param to
	 * 		destinatario da mensagem
	 * @param msg
	 * 		mensagem em si
	 * @return
	 * 		true se okay, caso contrario false
	 * @throws IOException
	 */
	public boolean insertMessage(Message msg) throws IOException{
		String folder = null;
		
		System.out.println("working...");
		if(  (folder = this.getConversationID(msg.getFrom(), msg.getTo())) == null){
			System.out.println("Conversacao inexistente");
			if((folder = this.add(msg.getFrom(), msg.getTo()))==null){
				System.out.println("erro ao criar conversacao");
				return false;
			}else
				System.out.println("Conversacao criada com sucesso");
		}
		
		System.out.println("Conversation directory is: " + folder);
		
		//verifica se a pasta de conversacao existe
		File file = new File("DATABASE/CONVERSATIONS/PRIVATE/"+folder);
		if(!file.exists())
			return false;
		
		//cria file de mensagem
		file = new File("DATABASE/CONVERSATIONS/PRIVATE/"+folder+"/"+file.list().length+".msg");
		if(!file.createNewFile())
			return false;
		
		//escreve em file
		StringBuilder sb = new StringBuilder();
		sb.append(msg.getFrom()+" ");
		sb.append("-t ");
		sb.append(msg.getBody()+"\n");
		
		FileWriter fr = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fr);
		bw.write(sb.toString());
		bw.flush();
		bw.close();
		
		
		return true;
	}
	
	/**
	 * Insere um ficheiro na conversa entre dois users
	 * Assim como uma mensagem representativa do mesmo
	 * @param from
	 * 		autor de upload
	 * @param to
	 * 		destinatario de upload
	 * @param file
	 * 		Ficheiro a ser recebido
	 * @return
	 */
	public boolean insertFile(String from, String to, File file){
		return false;
	}
	
	
	//add a new conversation to index conversations file
	private String add(String from, String to) throws IOException{
		
		int id = getNextID();
		File f = new File("DATABASE/CONVERSATIONS/PRIVATE/"+id);
		f.mkdirs();
		if( !f.exists() )
			return null;
		
		f = new File("DATABASE/CONVERSATIONS/PRIVATE/"+id+"/FILES");
		f.mkdirs();
		if(!f.exists())
			return null;
		
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
		
		return ""+id;
		
	}
	
	
	public Conversation getConversation(String u1, String u2){
		Conversation c = new Conversation(new User(u1), new User(u2), "");
		return this.conversations.get(c);
	}
	
	
	@Override
	public void destroy() throws IOException {
		// TODO Auto-generated method stub
		
	}

}
