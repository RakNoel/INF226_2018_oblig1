package inf226;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Immutable class for users.
 *
 * @author INF226
 */
public final class User {

    private final UserName name;
    private final Password passwd;
    private final String salt;
    private final ImmutableLinkedList<Message> log;


    public User(final UserName name, final Password passwd, String salt) {
        this.name = name;
        this.passwd = passwd;
        this.salt = salt;
        this.log = new ImmutableLinkedList<>();
    }

    private User(final UserName name, final Password passwd, String salt, final ImmutableLinkedList<Message> log) {
        this.name = name;
        this.salt = salt;
        this.passwd = passwd;
        this.log = log;
    }

    public boolean testPassword(Password passwd) {
        return this.passwd.equals(passwd);
    }

    /**
     * @return User name
     */
    public UserName getName() {
        return this.name;
    }

    public String getSalt() {
        return this.salt;
    }

    public Password getPass() {
        return this.passwd;
    }

    /**
     * @return Messages sent to this user.
     */
    public Iterable<Message> getMessages() {
        return log;
    }


    /**
     * Add a message to this user’s log.
     *
     * @param m Message
     * @return Updated user object.
     */
    public User addMessage(Message m) {
        return new User(name, passwd, salt, new ImmutableLinkedList<>(m, log));
    }

}

class UserName implements Comparable<UserName> {
    private final String username;

    UserName(String username) throws inf226.Maybe.NothingException {
        this.username = Server.validateUsername(username).force();
    }

    @Override
    public String toString() {
        return this.username;
    }

    @Override
    public int compareTo(UserName userName) {
        return this.toString().compareTo(userName.toString());
    }
}

class Password {
    private final String password;

    /**
     * Constructor will create a password-hash with the same result for the same input every time
     * @param pass Password to hash
     * @param salt Random string (should allways be the same for same hash) to add security
     * @throws inf226.Maybe.NothingException
     */
    Password(String pass, String salt) throws inf226.Maybe.NothingException {
        String h = Server.validatePassword(pass).force();
        h += salt;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(h.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            pass = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            pass = null;
            System.exit(1);
        }
        this.password = pass;
    }

    Password(String pass) throws inf226.Maybe.NothingException {
        this.password = Server.validatePassword(pass).force();
    }

    public boolean equals(Password password) {
        return this.toString().equals(password.toString());
    }

    @Override
    public String toString() {
        return this.password;
    }
}
