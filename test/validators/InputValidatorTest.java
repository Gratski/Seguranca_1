package validators;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sun.jvm.hotspot.utilities.Assert;

import static org.junit.Assert.*;

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

    }

    @Test
    public void testValidAddress() throws Exception {

    }
}