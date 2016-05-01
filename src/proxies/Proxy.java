package proxies;

/**
 * Esta classe contém as directorias todas da aplicação
 * usadas pelos outros Proxys
 *
 * @author Joao Rodrigues & Simao Neves
 */
public class Proxy {
	protected static final String DATABASE = "DATABASE/";
	
	protected static final String USERS_INDEX = DATABASE + "/USERS";
	protected static final String GROUPS_INDEX = DATABASE + "/GROUPS";
	
	protected static final String CONVERSATIONS = DATABASE + "CONVERSATIONS/";
	protected static final String CONVERSATIONS_PRIVATE_INDEX = CONVERSATIONS + "INDEX";
	protected static final String CONVERSATIONS_PRIVATE = CONVERSATIONS + "PRIVATE/";
	protected static final String CONVERSATIONS_GROUP = CONVERSATIONS + "GROUP/";

	protected static final String FILES_FOLDER = "FILES/";
	protected static final String MESSAGE_FILE_EXTENSION = ".msg";
	protected static final String MAC_FILE_EXTENSION = ".mac";
	protected static final String KEY_FILE_EXTENSION = ".key";
	protected static final String SIGNATURE_FILE_EXTENSION = ".sig";

	protected static final String HASH_ALGORITHM = "SHA-256";

	/**
	 * Getter para a pasta onde são guardados todos os dados
	 * @return Path da pasta
     */
	public static String getDATABASE() {
		return DATABASE;
	}

	/**
	 * Getter para a o ficheiro onde se guardam todos os utilizadores
	 * @return Path do ficheiro
     */
	public static String getUsersIndex() {
		return USERS_INDEX;
	}

	/**
	 * Getter para a o ficheiro onde se guardam as informações de grupos
	 * @return Path do ficheiro
     */
	public static String getGroupsIndex() {
		return GROUPS_INDEX;
	}

	/**
	 *Getter do nome da pasta onde são guardadas as pastas de conversas entre users e entre users e grupos
	 * @return Path da pasta
     */
	public static String getCONVERSATIONS() {
		return CONVERSATIONS;
	}

	/**
	 * Getter para o ficheiro onde são guardadas informações sobre conversas
	 * @return Path do ficheiro
     */
	public static String getConversationsPrivateIndex() {
		return CONVERSATIONS_PRIVATE_INDEX;
	}

	/**
	 * Getter para a pasta onde são guardadas as conversas de users
	 * @return Path da pasta
     */
	public static String getConversationsPrivate() {
		return CONVERSATIONS_PRIVATE;
	}

	/**
	 * Getter para a pasta onde são guardadas as conversas de grupos
	 * @return Path da pasta
     */
	public static String getConversationsGroup() {
		return CONVERSATIONS_GROUP;
	}

	/**
	 * Getter do nome da pasta onde se guardam ficheiros
	 * @return Nome da pasta
     */
	public static String getFilesFolder() {
		return FILES_FOLDER;
	}

	/**
	 * Getter para a extensão com que os ficheiros MAC são guardados
	 * @return Extensão dos MACs
     */
	public static String getMacFileExtension(){
		return MAC_FILE_EXTENSION;
	}

	/**
	 * Getter para a extensão com que os ficheiros de mensagens são guardados
	 * @return Extensão dos ficheiros das mensagens
	 */
	public static String getMessageFileExtension(){
		return MESSAGE_FILE_EXTENSION;
	}

	/**
	 * Getter para a extensão com que os ficheiros de mensagens são guardados
	 * @return Extensão dos ficheiros das mensagens
	 */
	public static String getKeyFileExtension(){
		return KEY_FILE_EXTENSION;
	}

	/**
	 * Getter para a extensão com que os ficheiros de mensagens são guardados
	 * @return Extensão dos ficheiros das mensagens
	 */
	public static String getSignatureFileExtension(){
		return SIGNATURE_FILE_EXTENSION;
	}
}
