import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import domain.Group;
import domain.User;
import proxies.GroupsProxy;
import proxies.UsersProxy;

public class UserTests {

	private static final String name = "João";
	private static final String password = "myPass";
	
	private static final String errorName = "User";
	private static final String errorPassword = "Pass";


	@Test
	public void userRegist(){
		//given
		User user = new User(name, password);
		try {
			//when
			UsersProxy uProxy = UsersProxy.getInstance();
			uProxy.insert(user);
			//then
			assertTrue( uProxy.exists(user));
			
		} catch(Exception e) {
			System.out.println(e.toString());
		}
		
	}
	
	@Test
	public void userValidAuthenticate(){
		//given
		User user = new User(name, password);
		try {
			//when
			UsersProxy proxy = UsersProxy.getInstance();
			//then
			assertTrue(proxy.autheticate(user));
		} catch(Exception e) {
			System.out.println("User Authenticate Exception!");
		}
		
	}
	
	@Test
	public void userInvalidAuthenticate(){
		//given
		User user = new User(errorName, password);
		try {
			//when
			UsersProxy proxy = UsersProxy.getInstance();
			//then
			assertFalse(proxy.autheticate(user));
		} catch(Exception e) {
			System.out.println("User Authenticate Invalid Name Exception!");
		}
		
	}
	
	@Test
	public void userInvalidPasswordAuthenticate(){
		//given
		User user = new User(name, errorPassword);
		try {
			//when
			UsersProxy proxy = UsersProxy.getInstance();
			//then
			assertFalse(proxy.autheticate(user));
		} catch(Exception e) {
			System.out.println("User Authenticate Invalid Password Exception!");
		}
		
	}
	
	@Test
	public void userDoubleRegist(){
		//given
		User user = new User(name, password);
		try {
			UsersProxy proxy = UsersProxy.getInstance();
			proxy.insert(user);
			//when
			boolean res = proxy.insert(user);
			//then
			assertFalse(res);
		} catch(Exception e) {
			System.out.println("User Double Regist Exception!");
		}	
	}
	
	@Test
	public void findValidUserByName(){
		try {
			UsersProxy uProxy = UsersProxy.getInstance();
			uProxy.insert(new User(name, password));
			assertNotNull(uProxy.find(name));
		} catch(Exception e) {
			System.out.println("Find Valid User by Name Exception!");
		}
	}
	
	@Test
	public void findInvalidUserByName(){
		try {
			UsersProxy proxy = UsersProxy.getInstance();
			assertNull(proxy.find(errorName));
		} catch(Exception e) {
			System.out.println("Find Invalid User by Name Exception!");
		}
	}
	
	@Test
	public void getGroups(){
		try {
			GroupsProxy proxy = GroupsProxy.getInstance();
			assertTrue(proxy.find("FCUL").hasMemberOrOwner(name));
		} catch(Exception e) {
			
		}
	}
}