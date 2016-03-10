import static org.junit.Assert.*;

import org.junit.Test;

import common.Group;
import common.User;
import proxies.GroupsProxy;

public class GroupsTests {

	private final static String groupName = "FCUL";
	private final static String errorGroupName = "FCULSTARS";
	
	private final static String userName = "João";
	private final static String userPassword = "myPass";
	private final static User user = new User(userName, userPassword);
	
	private final static String memberUserName = "FRIEND_1";
	private final static String memberPassword = "Password";
	private final static User friend1 = new User(memberUserName, memberPassword);
	
	private final static String errorUserName = "User";
	
	
	@Test
	public void insertGroup(){
		try{
			GroupsProxy proxy = GroupsProxy.getInstance();
			proxy.create(groupName, new User(userName));
			assertTrue(proxy.exists(groupName));
		}catch(Exception e){
			System.out.println("Insert Group Exception!");
		}
	}

//	@Test
//	public void getGroupsWhereMember(){
//		try{
//
//		} catch(Exception e) {
//			System.out.println("Insert Group Exception!");
//		}
//	}


	
	@Test
	public void isValidOwner(){
		try{
			GroupsProxy proxy = GroupsProxy.getInstance();
			assertTrue(proxy.isOwner(groupName, userName));
		}catch(Exception e){
			System.out.println("Is Valid Owner Exception!");
		}
	}
	
	@Test
	public void isInvalidOwner(){
		try{
			GroupsProxy proxy = GroupsProxy.getInstance();
			assertFalse(proxy.isOwner(groupName, errorUserName));
		}catch(Exception e){
			System.out.println("Is Valid Owner Exception!");
		}
	}
	
	@Test
	public void findGroupByName(){
		try{
			GroupsProxy groups = GroupsProxy.getInstance();
			Group group = groups.find(groupName);
			assertNotNull(group);
		}catch(Exception e){
			System.out.println("Find Group By Name Exception!");
		}
	}
	
	@Test
	public void insertMember(){
		try{
			GroupsProxy proxy = GroupsProxy.getInstance();
			boolean res = proxy.addMember(groupName, memberUserName);
			assertTrue(res);
		}catch(Exception e){
			
		}
	}
	
	@Test
	public void hasMember(){
		try{
			GroupsProxy proxy = GroupsProxy.getInstance();
			Group group = proxy.find(groupName);
			System.out.println("GROUP EXISTS: " + groupName != null);
			assertTrue(proxy.hasMember(groupName, memberUserName));
		}catch(Exception e){
			
		}
	}
	
	@Test
	public void removeExistingMember(){
		try{
			GroupsProxy proxy = GroupsProxy.getInstance();
			proxy.removeMember(groupName, memberUserName);
			assertFalse(proxy.hasMember(groupName, memberUserName));
		}catch(Exception e){
			
		}
	}
}
