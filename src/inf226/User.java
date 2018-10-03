package inf226;

/**
 * Immutable class for users.
 *
 * @author INF226
 */
public final class User {

    private final UserName name;
    private final Password passwd;
    private final ImmutableLinkedList<Message> log;


    public User(final UserName name, final Password passwd) {
        this.name = name;
        this.passwd = passwd;
        this.log = new ImmutableLinkedList<>();
    }

    private User(final UserName name, final Password passwd, final ImmutableLinkedList<Message> log) {
        this.name = name;
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
        return new User(name, passwd, new ImmutableLinkedList<Message>(m, log));
    }

}

class UserName {
    private final String username;

    UserName(String username) throws inf226.Maybe.NothingException {
        this.username = Server.validateUsername(username).force();
    }

    public String get() {
        return username;
    }

    @Override
    public String toString() {
        return this.get();
    }
}

class Password {
    private final String password;

    Password(String pass) throws inf226.Maybe.NothingException {
        this.password = Server.validatePassword(pass).force();
    }

    public String get() {
        return password;
    }

    public boolean equals(Password password) {
        return this.get().equals(password.get());
    }

    @Override
    public String toString() {
        return this.get();
    }
}
