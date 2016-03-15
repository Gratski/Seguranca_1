package handlers;

import domain.Reply;
import domain.Request;
import domain.User;
import helpers.DatabaseBuilder;
import org.junit.*;
import proxies.ConversationsProxy;
import proxies.GroupsProxy;
import proxies.UsersProxy;

import java.net.Socket;

import static org.junit.Assert.*;

/**
 * Created by simon on 10/03/16.
 */
public class RequestHandlerTest {

    public RequestHandler rh;
    public DatabaseBuilder db = new DatabaseBuilder();
    public User simao = new User("Simao", "pass");
    public User joao = new User("Joao", "pass");
    public User joaoInvalidPass = new User("Joao", "What?!");
    public User nonExistentUser = new User("nonExistent", "badpass");

    @BeforeClass
    public static void beforeAll() {
        DatabaseBuilder db = new DatabaseBuilder();
        db.destroy();
    }

    @Before
    public void setUp() throws Exception {
        this.db.make();
        UsersProxy usersProxy = UsersProxy.getInstance();
        GroupsProxy groupsProxy = GroupsProxy.getInstance();
        ConversationsProxy conversationsProxy = ConversationsProxy.getInstance();
        usersProxy.insert(simao);
        usersProxy.insert(joao);
        // é preciso por um servidorzito a correr para simular a connection com o socket, mas não era suposto
        this.rh = new RequestHandler(new Socket("127.0.0.1", 23456), usersProxy, groupsProxy, conversationsProxy);
    }

    @After
    public void tearDown() throws Exception {
        this.db.destroy();
    }

    @Test
    public void testParseRequest() throws Exception {
        Request req = new Request();
        req.setUser(nonExistentUser);
        req.setType("-regUser");
        assertFalse("User não devia existir", UsersProxy.getInstance().exists(nonExistentUser));
        rh.parseRequest(req);
        assertTrue("User que não existia passa a existir", UsersProxy.getInstance().exists(nonExistentUser));

        Request req2 = new Request();
        req2.setUser(joaoInvalidPass);
        req2.setType("-falseFlagForTest");
        assertTrue(UsersProxy.getInstance().exists(joao));
        Reply reply = rh.parseRequest(req2);
        assertEquals("User não autenticado é identificado", new Reply(400, "User nao autenticado"), reply);

        Request req3 = new Request();
        req3.setUser(simao);
        req3.setType("-regUser");
        Reply reply2 = rh.parseRequest(req3);
        assertEquals("User que já existe não pode ser inserido novamente", new Reply(404, "Erro ao adicionar novo utilizador"), reply2);
    }

    @AfterClass
    public static void afterAll() {
        DatabaseBuilder db = new DatabaseBuilder();
        db.destroy();
        db.make();
    }

}