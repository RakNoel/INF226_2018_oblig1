package inf226;

/**
 * Immutable class for users.
 *
 * @author INF226
 */
public final class User {

    private final String name;
    private final String passwd;
    private final ImmutableLinkedList<Message> log;


    public User(final String name, final String passwd) {
        this.name = name;
        this.passwd = passwd;
        this.log = new ImmutableLinkedList<Message>();
    }

    private User(final String name, final String passwd, final ImmutableLinkedList<Message> log) {
        this.name = name;
        this.passwd = passwd;
        this.log = log;
    }

    public boolean testPassword(String passwd) {
        return this.passwd.equals(passwd);
    }

    /**
     * @return User name
     */
    public String getName() {
        return name;
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
