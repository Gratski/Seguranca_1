package proxies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import builders.FileStreamBuilder;
import domain.Conversation;
import domain.Group;
import domain.Message;
import domain.User;
import security.CipheredKey;
import security.GenericSignature;
import security.SecUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Esta classe representa o responsavel pelo acesso e manutencao
 * das Conversations entre utilizadores
 * @author JoaoRodrigues & SimãoNeves
 *
 */
public class ConversationsProxy extends Proxy {

	private static ConversationsProxy instance = null;

	/**
	 * Obter instancia de ConversationsProxy para persistir conversas
	 *
	 * @return conversationProxy a ser usado para persistência de conversas
	 * @throws IOException
	 */
	public static ConversationsProxy getInstance() throws IOException {
		if( instance == null )
			instance = new ConversationsProxy();
		return instance;
	}
	
	/**
	 * Método que devolve o nome do directorio onde esta
	 * a informacao relativa ah conversa
	 * @param user1
	 * 		Nome de utilizador 1
	 * @param user2
	 * 		Nome de utilizador 2
	 * @return
	 * 		nome do directorio caso a conversa exista,
	 * 		null caso contrario
	 * @throws IOException
	 *
	 * @requires user1 != null && user2 != null
	 */
	public String getConversationID(String user1, String user2) throws IOException {
		
		File f = new File(CONVERSATIONS_PRIVATE_INDEX);
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

	/**
	 * Método que devolve a pasta de uma conversa entre dois utilizadores
	 * Se a conversa não existir ainda, cria-a
	 *
	 * @param user1
	 * 		User que participa na conversa
	 * @param user2
	 * 		Segundo User que participa na conversa
	 * @return
	 * 		String com o nome da directoria
	 * @throws IOException
     */
	public String getOrCreate(String user1, String user2) throws IOException{
		String folder = null;
		if ( ( folder = getConversationID(user1, user2) ) == null )
			folder = this.add(user1, user2);

		return folder;
	}

	/**
	 *	Método que retorna todas as conversa privadas que User com nome user tem
	 *
	 * @param user
	 * 		Utilizador que tem as conversas a serem procuradas
	 * @return
	 * 		Lista de Conversations onde o user participa
	 * @requires user != null
	 *
	 * @throws IOException
     */
	public ArrayList<Conversation> getConversationsFrom(String user) throws IOException {
		ArrayList<Conversation> list = new ArrayList<>();
		File f = new File(CONVERSATIONS_PRIVATE_INDEX);
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

	/**
	 * Método que procura e devolve todas as Conversations (de grupo e privadas)
	 * que o User user tem.
	 * As Conversations devolvidas só têm a última mensagem nessa conversa
	 *
	 * @param user
	 * 		Utilizador que participa nas conversas que são devolvidas
	 * @return
	 * 		Lista com todas as Conversations com a última mensagem da conversa
	 * @requires user != null
	 *
	 * @throws IOException
     */
	public ArrayList<Conversation> getLastMessageFromAll(User user) throws IOException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException {
		// Ir buscar conversations
		ArrayList<Conversation> conversations = new ArrayList<>();
		conversations.addAll(getConversationsFrom(user.getName()));
		Collection<Group> groups = GroupsProxy.getInstance().getGroupsWhereMember(user.getName()).values();

		// Para cada group criar uma conversation de grupo e juntar as de privates
		for (Group group : groups) {
			conversations.add(new Conversation(group));
		}
		// Iterar as conversations para ir buscar a lastMessage (em novo metodo, como em baixo)
		for (Conversation conversation : conversations) {
			Message lastMessage = getLastMessage(conversation, user.getName());
			if (lastMessage != null)
				conversation.addMessage(lastMessage);
		}
		return conversations;
	}

	/**
	 * Método que devolve a última Message de da Conversation conversation
	 *
	 * @param conversation
	 *		Conversation de onde vamos buscar a última Message
	 * @return
	 * 		Message que é a última na Conversation (com base no número do ficheiro), ou
	 * 		null se houver algum erro
	 * @throws IOException
	 *
	 * @requires conversation != null
     */
	private Message getLastMessage(Conversation conversation, String user) throws IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
		String folder = conversation.getGroup() == null ? "PRIVATE" : "GROUP";
		String id = conversation.getGroup() == null ? conversation.getFilename() : conversation.getGroup().getName();

		String lastMessage = null;
		int lastMessageID = 0;
		File f = new File(CONVERSATIONS + folder + "/" + id);
		try {
			if ( f.listFiles().length > 1) {
				lastMessageID = f.list().length - 1;
				lastMessage = lastMessageID + "/body" + MESSAGE_FILE_EXTENSION;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erro ao tentar f.list()");
			return null;
		}

		File f2 = new File(CONVERSATIONS + folder + "/" + id + "/" + lastMessage);
		FileReader fr = new FileReader(f2);
		BufferedReader br = new BufferedReader(fr);
		String line;
		Message message = null;

		if ((line = br.readLine()) != null) {
			message = createMessageFromLine(line, user, CONVERSATIONS + folder + "/" + id + "/" + lastMessageID + "/");
		} else {
			br.close();
			fr.close();
			return null;
		}
		br.close();
		fr.close();
		return message;
	}

	/**
	 * Constrói uma nova mensagem com base numa linha
	 *
	 * @param line
	 * 		Linha de onde se vão buscar as informações para construir a mensagem
	 * @param user
	 * 		Utilizador que vai receber a mensagem, serve para ver se vamos buscar a sua .key ou não
	 * @param path
	 * 		Path onde a informação da mensagem está
	 * @return
	 * 		Retorna mensagem Message com as informações todas lidas
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     */
	private Message createMessageFromLine(String line, String user, String path) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
		String[] split = line.split(" ");
		String timeInMilliseconds = split[0];
		String from = split[1];
		String messageBody = split[3];

		Message message = new Message(from, messageBody);
		message.setTimeInMilliseconds(Long.parseLong(timeInMilliseconds));
		message.setSignature(GenericSignature.readSignatureFromFile(path + "signature.sig"));

		// Verificar se user pode receber mensagem com base na existência do user.key
		if (new File(path + user + ".key").exists())
			message.setKey(SecUtils.readKeyFromFile(path + user + ".key"));

		return message;
	}

	/**
	 * Método que devolve a Conversation entre user com nome user e com o contacto
	 * contact, que é o nome de outro User ou um Group
	 *
	 * @param user
	 * 		Nome de um User
	 * @param contact
	 * 		Nome de um User ou Group
	 * @return	Conversation entre user e contact, null se nãoe xistir essa conversa ou
	 * 		se houver algum erro
	 * @throws IOException
     */
	public Conversation getConversationBetween(String user, String contact) throws IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
		Group group = GroupsProxy.getInstance().find(contact);
		String path = userHasConversationWith(user, contact);
		System.out.println("PATH entre: " + user + " e " + contact);
		if (path == null)
			return null;

		System.out.println("PATH SUPOSTO: " + path);
		File f = new File(path);
		int messagesNum = 0;
		try {
			if ( f.listFiles().length > 1)
				messagesNum = f.list().length - 1;
		} catch (Exception e) {
			System.out.println("Erro ao tentar f.list()");
			return null;
		}

		Conversation conversation;
		if (group == null) {
			conversation = new Conversation(new User(user), new User(contact));
		} else {
			conversation = new Conversation(group);
		}

		System.out.println("Number of messages: " + messagesNum);

		for (int i = 1; i <= messagesNum; i++) {
			File f2 = new File(path + "/" + i + "/" + "body" + MESSAGE_FILE_EXTENSION);
			FileReader fr = new FileReader(f2);
			BufferedReader br = new BufferedReader(fr);
			String line;
			Message message;
			System.out.println("Processing message: " + i);

			if ((line = br.readLine()) != null) {
				message = createMessageFromLine(line, user, path + "/" + i + "/");
				conversation.addMessage(message);
			} else {
				br.close();
				fr.close();
				return null;
			}
		}
		return conversation;
	}

	/**
	 * Devolve o próximo ID, o novo ficheiro onde guardar uma nova mensagem
	 *
	 * @return	Inteiro que representa o nome da nova mensagem a ser guardada
	 * @throws IOException
     */
	private int getNextID() throws IOException {
		File f = new File(CONVERSATIONS_PRIVATE);
		if ( f.list() != null )
			return f.list().length + 1;
		return -1;
	}
	
	/**
	 * Regista mensagem em pasta de group
	 *
	 * @param msg
	 * 		Mensagem a registar
	 * @return
	 * 		true se a mensagem persistiu, false caso contrario
	 * @requires	msg != null
	 *
	 * @throws IOException
	 */
	public boolean insertGroupMessage(Message msg, Map<String, CipheredKey> keys, GenericSignature sign) throws IOException {
		//criar pasta de group
		File file = new File(CONVERSATIONS_GROUP + msg.getTo());
		
		//set sent time to now
		msg.setTimestampNow();
		boolean res = MessagesProxy.getInstance().persist(
				CONVERSATIONS_GROUP + msg.getTo(),
				"" + file.list().length,
				msg, keys, sign);
		return res;
	}

	/**
	 * Devolve o path para a conversa que existe entre o User com nome user e o contact with
	 * que pode ser um outro User ou um Group
	 *
	 * @param user
	 * 		Nome de User que participa na conversa
	 * @param with
	 * 		Nome de outro User ou Group que participa na conversa
	 * @return
	 * 		Path para a directoria da conversa, ou null se não existir conversa entre user e with
	 * @requires
	 * 		user != null
	 * @throws IOException
     */
	public String userHasConversationWith(String user, String with) throws IOException{
		String path = null;
		Map<String, Group> groups = GroupsProxy.getInstance().getGroupsWhereMember(user);
		
		//se eh-group
		if ( groups.containsKey(with) ) {
			path = CONVERSATIONS_GROUP + with;
		}
		//se eh private
		else {
			String dir = getConversationID(user, with);
			if (dir != null)
				path = CONVERSATIONS_PRIVATE + dir;
		}
		return path;
	}
	
	/**
	 * Insere uma nova mensagem numa conversacao
	 * nova ou existente
	 * @param msg
	 * 		mensagem em si
	 * @return
	 * 		true se okay, caso contrario false
	 * @requires
	 * 		msg != null
	 * @throws IOException
	 */
	public boolean insertPrivateMessage(Message msg, Map<String, CipheredKey> keys, GenericSignature sign) throws IOException {
		String folder = null;
		if ( (folder = this.getConversationID(msg.getFrom(), msg.getTo())) == null)
			if ((folder = this.add(msg.getFrom(), msg.getTo())) == null)
				return false;
		
		//set sent time to now
		msg.setTimestampNow();
		
		//verifica se a pasta de conversacao existe
		File file = new File(CONVERSATIONS_PRIVATE + folder);
		if (!file.exists())
			return false;
		
		//cria file de mensagem
		return MessagesProxy.getInstance().persist(CONVERSATIONS_PRIVATE + folder, "" + file.list().length, msg, keys, sign);
	}

	/**
	 * Adiciona nova conversa ao ficheiro de indice de conversas
	 * e cria todas as pastas necessárias associadas a essa conversa
	 *
	 * @param from
	 * 		Nome de User que vai participar na conversa
	 * @param to
	 * 		Nome de User que vai participar na conversa
	 * @return
	 *		Devolve nome da pasta onde a conversa vai ficar guardada
	 * @requires from != null && to != null
	 * @throws IOException
     */
	private String add(String from, String to) throws IOException {
		int id = getNextID();
		File f = new File(CONVERSATIONS_PRIVATE + id + "/" + FILES_FOLDER);
		f.mkdirs();
		if (!f.exists())
			return null;
		
		// cria files index file
		File index = new File(CONVERSATIONS_PRIVATE + id + "/" + FILES_FOLDER + "/index");
		if(!index.exists())
			index.createNewFile();
		
		//save on file
		BufferedWriter writer = FileStreamBuilder.makeWriter(CONVERSATIONS_PRIVATE_INDEX, true);
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
}