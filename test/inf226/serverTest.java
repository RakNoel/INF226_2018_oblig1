package inf226;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TODO: Describe test
 *
 * @author RakNoel
 * @version 1.0
 * @since 03.10.18
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class serverTest {

    private Random rnd = new Random();
    private ArrayList<String> passwordsGood = new ArrayList<>();
    private ArrayList<String> usernamesGood = new ArrayList<>();

    private ArrayList<String> passwordsBad = new ArrayList<>();
    private ArrayList<String> usernamesBad = new ArrayList<>();

    private int n = 100000;
    private int p = 100;

    public serverTest() {
        passwordsGood.add("1234567890");
        passwordsGood.add("eple");
        passwordsGood.add("salatblad");
        passwordsGood.add("fruktsalat96");

        usernamesGood.add("raknoel");
        usernamesGood.add("RakNoel");
        usernamesGood.add("RakNoel23");
        usernamesGood.add("R4kN031");

        char[] possiblePass = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,:;()[]{}<>\"'#!$%&/+*?=-_|".toCharArray();
        char[] possibleUser = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();


        for (int x = 0; x < n; x++) {
            StringBuilder h = new StringBuilder();
            for (int y = 0; y < p; y++)
                h.append(possiblePass[rnd.nextInt(possiblePass.length)]);
            passwordsGood.add(h.toString());

            h = new StringBuilder();
            for (int y = 0; y < p; y++)
                h.append(possibleUser[rnd.nextInt(possibleUser.length)]);
            usernamesGood.add(h.toString());

            h = new StringBuilder();
            for (int y = 0; y < p; y++)
                h.append((char) rnd.nextInt(256));
            usernamesBad.add(h.toString());

            h = new StringBuilder();
            for (int y = 0; y < p; y++)
                h.append((char) rnd.nextInt(256));
            passwordsBad.add(h.toString());
        }
    }

    @Before
    public void before() {
        //TODO: Implement BEFORE each test
    }

    @Test
    public void serverTest_validate_passwords() {
        for (String pass : passwordsGood)
            assertFalse(Server.validatePassword(pass).isNothing());

        for (String pass : passwordsBad)
            assertTrue(Server.validatePassword(pass).isNothing());
    }

    @Test
    public void serverTest_validate_usernames() {
        for (String usr : usernamesGood)
            assertFalse(Server.validateUsername(usr).isNothing());

        for (String usr : usernamesBad)
            assertTrue(Server.validateUsername(usr).isNothing());
    }
}