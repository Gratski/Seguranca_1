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
	
	protected static final String HASH_ALGORITHM = "SHA-256";

	public static String getDATABASE() {
		return DATABASE;
	}

	public static String getUsersIndex() {
		return USERS_INDEX;
	}

	public static String getGroupsIndex() {
		return GROUPS_INDEX;
	}

	public static String getCONVERSATIONS() {
		return CONVERSATIONS;
	}

	public static String getConversationsPrivateIndex() {
		return CONVERSATIONS_PRIVATE_INDEX;
	}

	public static String getConversationsPrivate() {
		return CONVERSATIONS_PRIVATE;
	}

	public static String getConversationsGroup() {
		return CONVERSATIONS_GROUP;
	}

	public static String getFilesFolder() {
		return FILES_FOLDER;
	}

	public static String getMessageFileExtension() {
		return MESSAGE_FILE_EXTENSION;
	}
	
	public static String getMacFileExtension(){
		return MAC_FILE_EXTENSION;
	}
}
