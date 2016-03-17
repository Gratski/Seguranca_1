package proxies;

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
}
