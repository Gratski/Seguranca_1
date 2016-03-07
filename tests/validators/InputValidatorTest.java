package validators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by simon on 02/03/16.
 */
public class InputValidatorTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testValidInput() throws Exception {
        assertFalse("Recognizes invalid input with switched order", InputValidator.validInput(split("127.0.0.1:8080 simao -p pass -f contact file_name")));
        assertFalse("Recognizes invalid input with few arguments", InputValidator.validInput(split("simao 127.0.0.1:8080 -p pass -f")));
        assertFalse("Recognizes invalid input with few arguments", InputValidator.validInput(split("simao 127.0.0.1:8080 -p pass -m joao")));
        assertFalse("Recognizes invalid input with more arguments than suposed", InputValidator.validInput(split("simao 127.0.0.1:8080 -p pass -m joao \"asdasd\" mais argumentos")));
        assertFalse("Recognizes invalid input for flag -regUser", InputValidator.validInput(split("simao 127.0.0.1:8080 -p pass -regUser")));
        assertFalse("Recognizes invalid input with no arguments", InputValidator.validInput(null));
        assertTrue("Recognizes valid input (-r)", InputValidator.validInput(split("simao 127.0.0.1:8080 -p pass -r")));
        assertTrue("Recognizes valid input (-m) without password", InputValidator.validInput(split("simao 127.0.0.1:8080 -m ricardo \"lala\"")));
        assertTrue("Recognizes valid input (-m)", InputValidator.validInput(split("simao 127.0.0.1:8080 -p pass -m ricardo \"lala\"")));
        assertTrue("Recognizes valid input (-f)", InputValidator.validInput(split("simao 127.0.0.1:8080 -f group file_name")));
    }

    @Test
    public void testValidName() throws Exception {
        assertFalse("Recognizes invalid username with ','", InputValidator.validName("bad,name"));
        assertFalse("Recognizes invalid username with ':'", InputValidator.validName(":username"));
        assertTrue("Accepts valid username", InputValidator.validName("normalcoolname"));
    }

    @Test
    public void testValidPassword() throws Exception {
        assertFalse("Recognizes invalid passwords with length < 4", InputValidator.validPassword("bad"));
        assertFalse("Recognizes invalid passwords with ':'", InputValidator.validPassword(":pass"));
        assertTrue("Accepts valid passwords", InputValidator.validPassword("normalpass"));
    }

    @Test
    public void testValidFlag() throws Exception {
        assertFalse("Recognizes invalid flags", InputValidator.validFlag("-q"));
        assertTrue("Recognizes valid flags", InputValidator.validFlag("-r")
                                            && InputValidator.validFlag("-a")
                                            && InputValidator.validFlag("-d")
                                            && InputValidator.validFlag("-m")
                                            && InputValidator.validFlag("-regUser")
                                            && InputValidator.validFlag("-f"));
    }

    @Test
    public void testValidAddress() throws Exception {
        assertFalse("Recognizes invalid port", InputValidator.validAddress("127.0.0.1:123456"));
        assertFalse("Recognizes invalid port", InputValidator.validAddress("127.0.0.1:asd"));
        assertFalse("Recognizes invalid ip", InputValidator.validAddress("127.simao.0.1:12345"));
        assertFalse("Recognizes invalid ip", InputValidator.validAddress("327.0.0.1:12345"));
        assertFalse("Recognizes invalid ip", InputValidator.validAddress("123.0.256.1:12345"));
        assertTrue("Recognizes valid port and ip", InputValidator.validAddress("127.0.0.1:12345"));
    }

    @Test
    public void testValidPort() throws Exception {
        assertFalse("Recognizes invalid port", InputValidator.validPort("17935682"));
        assertTrue("Recognizes valid input", InputValidator.validPort("12345"));
    }

    @Test
    public void testServerInput() throws Exception {
        assertFalse("Recognizes invalid input with letters", InputValidator.validServerInput(split("asd")));
        assertFalse("Recognizes invalid input with no argumetns", InputValidator.validServerInput(null));
        assertFalse("Recognizes invalid input with multiple arguments", InputValidator.validServerInput(split("multiplos argumentos")));
        assertFalse("Recognizes invalid input with invalid port", InputValidator.validServerInput(split("123123123")));
        assertTrue("Recognizes valid input", InputValidator.validServerInput(split("12345")));
    }

    public String[] split(String s) {
        return s.split(" ");
    }
}