package proxies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

	private static ConversationsProxy instance = null;

	public static ConversationsProxy getInstance() throws IOException {
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
	public String getConversationID(String user1, String user2) throws IOException {
		
		File f = new File("DATABASE/CONVERSATIONS/INDEX");
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		String res = null;
		
		while ((line = br.readLine()) != null) {
			String[] split = line.split(" ");
			if (split.length < 2)
				continue;
			
			String u1 = split[0];
			String u2 = split[1];
			String dir = split[2];
			
			if ( (user1.equals(u1) && user2.equals(u2))
					|| (user2.equals(u1) && user1.equals(u2)) ) {
				res = dir;
				break;
			}
		}
		return res;
	}

	public ArrayList<Conversation> getConversationsFrom(String user) throws IOException {

		ArrayList<Conversation> list = new ArrayList<>();
		File f = new File("DATABASE/CONVERSATIONS/INDEX");
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String line = null;

		while ((line = br.readLine()) != null) {
			String[] split = line.split(" ");
			if (split.length < 2)
				continue;

			String u1 = split[0];
			String u2 = split[1];
			String dir = split[2];

			if ( user.equals(u1) || user.equals(u2) )
				list.add(new Conversation(new User(u1), new User(u2), dir));
		}
		return list;
	}

	public List<Conversation> getLastMessageFromAll(User user) throws IOException {

		// Ir buscar conversations com privates
		ArrayList<Conversation> conversations = getConversationsFrom(user.getName());

		//  Ir buscar groups ao groupProxy com getGroupsWhereMember
		// Para cada group criar uma conversation de grupo e juntar as de privates
		// Iterar as conversations para ir buscar a lastMessage (em novo metodo, como em baixo)

		if (conversations == null)
			return null;

		for (Conversation id : conversations) {
			int lastMessage;
			File f = new File("DATABASE/CONVERSATIONS/PRIVATE/" + id);
			if ( f.list() != null )
				lastMessage = f.list().length - 1;
			else
				return null;

			File f2 = new File("DATABASE/CONVERSATIONS/PRIVATE/" + id + "/" + lastMessage + ".msg");

			FileReader fr = new FileReader(f2);
			BufferedReader br = new BufferedReader(fr);
			String line = null;

			if ((line = br.readLine()) != null) {
				String[] split = line.split(" ");
				if (split.length < 2)
					continue;

				String timeInMilliseconds = split[0];
				String from = split[1];
				String type = split[2];

				split = line.split(type);
				String messageBody = split[1];

				Message message = new Message(from, user.getName(), messageBody);
				message.setTimeInMilliseconds(Long.parseLong(timeInMilliseconds));
				Conversation convo = new Conversation(user, new User(from));
				if (convo.addMessage(message))
					conversations.add(convo);

			} else {
				br.close();
				fr.close();
				return null;
			}

			br.close();
			fr.close();
		}
		return conversations;
	}

	public Conversation getConversationBetween(String user1, String user2) throws IOException {
		getConversationID(user1, user2);
		return null;
	}
	
	private int getNextID() throws IOException {
		File f = new File("DATABASE/CONVERSATIONS/PRIVATE");
		if ( f.list() != null )
			return f.list().length + 1;
		return -1;
	}
	
	/**
	 * Regista mensagem em pasta de group
	 * @param msg
	 * 		Mensagem a registar
	 * @return
	 * 		true se enviou, false caso contrario
	 * @throws IOException
	 */
	public boolean insertGroupMessage(Message msg) throws IOException {
		
		//criar pasta de group
		File file = new File("DATABASE/CONVERSATIONS/GROUP/" + msg.getTo());
		if (!file.exists())
			file.mkdirs();
		
		//criar files folder
		File filesFolder = new File("DATABASE/CONVERSATIONS/GROUP/" + msg.getTo() + "/FILES");
		if (!filesFolder.exists())
			filesFolder.mkdirs();
		
		//criar file de msg
		File msgFile = new File("DATABASE/CONVERSATIONS/GROUP/" + msg.getTo() + "/" + file.list().length + ".msg");
		if (msgFile.exists())
			return false;

		msgFile.createNewFile();
		//escrever mensagem
		writeOnFile(msgFile, msg);

		return true;
	}
	
	/**
	 * Insere uma nova mensagem numa conversacao
	 * nova ou existente
	 * @param msg
	 * 		mensagem em si
	 * @return
	 * 		true se okay, caso contrario false
	 * @throws IOException
	 */
	public boolean insertPrivateMessage(Message msg) throws IOException {
		String folder = null;
		if ( (folder = this.getConversationID(msg.getFrom(), msg.getTo())) == null)
			if ((folder = this.add(msg.getFrom(), msg.getTo())) == null)
				return false;
		
		//verifica se a pasta de conversacao existe
		File file = new File("DATABASE/CONVERSATIONS/PRIVATE/" + folder);
		if (!file.exists())
			return false;
		
		//cria file de mensagem
		file = new File("DATABASE/CONVERSATIONS/PRIVATE/" + folder + "/" + file.list().length + ".msg");
		if (!file.createNewFile())
			return false;
		
		//escreve em file
		writeOnFile(file, msg);
		return true;
	}

	private void writeOnFile(File file, Message msg) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(msg.getTimeInMiliseconds() + " ");
		sb.append(msg.getFrom() + " ");
		sb.append(msg.getType() + " ");
		sb.append(msg.getBody() + "\n");
		
		FileWriter fr = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fr);
		bw.write(sb.toString());
		bw.flush();
		bw.close();
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
	private String add(String from, String to) throws IOException {
		
		int id = getNextID();
		File f = new File("DATABASE/CONVERSATIONS/PRIVATE/" + id);
		f.mkdirs();
		if ( !f.exists() )
			return null;
		
		f = new File("DATABASE/CONVERSATIONS/PRIVATE/" + id + "/FILES");
		f.mkdirs();
		if (!f.exists())
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
		
		return "" + id;
	}
	
	@Override
	public void destroy() throws IOException {
		// TODO Auto-generated method stub
		
	}
}
