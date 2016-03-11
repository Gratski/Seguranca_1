package proxies;

import java.io.IOException;

public class Proxy {
	
	protected static final String DATABASE = "DATABASE";
	
	protected static final String USERS_INDEX = DATABASE+"/USERS";
	protected static final String GROUPS_INDEX = DATABASE+"/GROUPS";
	
	protected static final String CONVERSATIONS = DATABASE+"/CONVERSATIONS";
	protected static final String CONVERSATIONS_PRIVATE_INDEX = DATABASE+"/"+CONVERSATIONS+"/INDEX";
	protected static final String CONVERSATIONS_PRIVATE = DATABASE+"/"+CONVERSATIONS+"/PRIVATE";
	protected static final String CONVERSATIONS_GROUP = DATABASE+"/"+CONVERSATIONS+"/GROUP";
	
}
