package handlers;

import builders.RequestBuilder;
import domain.*;
import helpers.DatabaseBuilder;
import org.junit.*;
import org.junit.internal.matchers.ThrowableCauseMatcher;
import proxies.ConversationsProxy;
import proxies.GroupsProxy;
import proxies.UsersProxy;
import validators.InputValidator;

import java.io.File;
import java.io.FileNotFoundException;
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
    public User ricardo = new User("Ricardo", "pass");
    public User joaoInvalidPass = new User("Joao", "What?!");
    public User nonExistentUser = new User("nonExistent", "badpass");

    @BeforeClass
    public static void beforeAll() {
        DatabaseBuilder db = new DatabaseBuilder();
        db.destroy();
    }

    @Before
    public void setUp() throws Exception {
        System.out.println("Setting up things");
        this.db.make();
        UsersProxy usersProxy = UsersProxy.getInstance();
        GroupsProxy groupsProxy = GroupsProxy.getInstance();
        ConversationsProxy conversationsProxy = ConversationsProxy.getInstance();
        usersProxy.insert(simao);
        usersProxy.insert(joao);
        usersProxy.insert(ricardo);
        groupsProxy.create("FCUL", simao);
        groupsProxy.addMember("FCUL", joao.getName());
        conversationsProxy.insertGroupMessage(new Message("Simao", "FCUL", "Do Simao para o Grupo FCUL"));
        conversationsProxy.insertGroupMessage(new Message("Joao", "FCUL", "Do Joao para o Grupo FCUL"));
        conversationsProxy.insertPrivateMessage(new Message("Simao", "Joao", "Do Simao para o Joao"));
        conversationsProxy.insertPrivateMessage(new Message("Joao", "Simao", "Do Joao para o Simao"));
        conversationsProxy.insertPrivateMessage(new Message("Simao", "Joao", "Do Simao para o Joao2"));
        conversationsProxy.insertPrivateMessage(new Message("Ricardo", "Simao", "Do Ricardo para o Simao"));

        // é preciso por um servidorzito a correr para simular a connection com o socket, mas não era suposto
        this.rh = new RequestHandler(new Socket("127.0.0.1", 23456), usersProxy, groupsProxy, conversationsProxy);
    }

    @After
    public void tearDown() throws Exception {
        this.db.destroy();
    }

    @Test
    public void testParseRequest() throws Exception {
        UsersProxy usersProxy = UsersProxy.getInstance();
        Request req = new Request();
        req.setUser(nonExistentUser);
        req.setType("-regUser");
        assertFalse("User não devia existir", usersProxy.exists(nonExistentUser));
        rh.parseRequest(req);
        assertTrue("User que não existia passa a existir", usersProxy.exists(nonExistentUser));

        Request req2 = new Request();
        req2.setUser(joaoInvalidPass);
        req2.setType("-falseFlagForTest");
        assertTrue(usersProxy.exists(joao));
        Reply reply = rh.parseRequest(req2);
        assertEquals("User não autenticado é identificado", new Reply(400, "User nao autenticado"), reply);

        Request req3 = new Request();
        req3.setUser(simao);
        req3.setType("-regUser");
        Reply reply2 = rh.parseRequest(req3);
        assertEquals("User que já existe não pode ser inserido novamente", new Reply(404, "Erro ao adicionar novo utilizador"), reply2);
    }

    @Test
    public void testSendMessage() throws Exception {
        ConversationsProxy conversationsProxy = ConversationsProxy.getInstance();
        Conversation conv = conversationsProxy.getConversationBetween(simao.getName(), nonExistentUser.getName());
        assertNull("Conversa não deve de existir", conv);
        Request req = new Request();
        req.setUser(simao);
        req.setType("-m");
        req.setContact(nonExistentUser.getName());
        req.setMessage(new Message(simao.getName(), nonExistentUser.getName(), "This should not work"));

        Reply reply = rh.parseRequest(req);
        assertEquals("Enviar mensagem para nonExistentUser dá erro", new Reply(400, "Destinatário inexistente"), reply);
        conv = conversationsProxy.getConversationBetween(simao.getName(), nonExistentUser.getName());
        assertNull("Conversa deve continuar a não de existir", conv);

        conv = conversationsProxy.getConversationBetween(simao.getName(), joao.getName());
        assertEquals("É suposto haver 3 mensagens trocadas", 3, conv.getMessages().size());
        Request req2 = new Request();
        req2.setUser(simao);
        req2.setType("-m");
        req2.setContact(joao.getName());
        req2.setMessage(new Message(simao.getName(), joao.getName(), "This should work"));
        Reply reply2 = rh.parseRequest(req2);
        assertEquals("Enviar mensagem não dá erro", 200, reply2.getStatus());
        conv = conversationsProxy.getConversationBetween(simao.getName(), joao.getName());
        assertEquals("É suposto haver 4 mensagens trocadas agora", 4, conv.getMessages().size());
    }

    @Test
    public void testSendFile() throws Exception {
        ConversationsProxy conversationsProxy = ConversationsProxy.getInstance();
        Conversation conv = conversationsProxy.getConversationBetween(simao.getName(), nonExistentUser.getName());
        assertNull("Conversa não deve de existir", conv);
        Request req = new Request();
        req.setUser(simao);
        req.setType("-f");
        req.setContact(nonExistentUser.getName());
        req.setFile(new NetworkFile("README.md"));

        Reply reply = rh.parseRequest(req);
        assertEquals("Enviar File para nonExistentUser dá erro", new Reply(400, "Destinatário inexistente"), reply);
        conv = conversationsProxy.getConversationBetween(simao.getName(), nonExistentUser.getName());
        assertNull("Conversa deve continuar a não existir", conv);

        try {
            RequestBuilder.make(InputValidator.parseInput(split("Simao 127.0.0.1:23456 -p pass -f Joao non_existant_file")));
        } catch (Throwable ex) {
            assertTrue("Raise Exception when no file is found", ex instanceof FileNotFoundException);
        }
    }

    @Test
    public void testGetLastMessageFromConversations() throws Exception {
        Request req = new Request();
        req.setUser(simao);
        req.setType("-r");
    }

    @AfterClass
    public static void afterAll() {
        DatabaseBuilder db = new DatabaseBuilder();
        db.destroy();
        db.make();
    }

    public String[] split(String s) {
        return s.split(" ");
    }

}