package inf226;

import inf226.Storage.Storage.ObjectDeletedException;
import inf226.Storage.Stored;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The Server main class. This implements all critical server functions.
 *
 * @author INF226
 */
public class Server {
    private static final int portNumber = 1337;
    private static final DataBaseUserStorage storage = DataBaseUserStorage.getInstance();

    /**
     * Method to authenticate a user by password
     *
     * @param username Username we hope to find
     * @param password Password to test if user is found
     * @return Maybe(User) if exist and matches password
     */
    public static Maybe<Stored<User>> authenticate(UserName username, Password password) {
        try {
            Stored<User> u = storage.lookup(username).force();
            return (u.getValue().testPassword(password)) ? Maybe.just(u) : Maybe.nothing();
        } catch (inf226.Maybe.NothingException ex) {
            return Maybe.nothing();
        }
    }

    /**
     * Method that will add a new user if there is not already someone with that username
     *
     * @param username Unique username to register
     * @param password password of said user
     * @return Maybe(User) of the new user, depending on success.
     */
    public static Maybe<Stored<User>> register(UserName username, Password password, String salt) {
        try {
            if (!storage.lookup(username).isNothing()) return Maybe.nothing();
            storage.save(new User(username, password, salt));
            return storage.lookup(username);
        } catch (IOException ex) {
            return Maybe.nothing();
        }
    }

    /**
     * Method that generates a token for the user
     * @param user
     * @param TTL Time to live for token in seconds.
     * @return Maybe(token) saved if successful
     */
    public static Maybe<Token> createToken(Stored<User> user, int TTL) {
        Token token = new Token();
        try {
            return (storage.insertToken(token, user.getValue(), TTL)) ? Maybe.just(token) : Maybe.nothing();
        } catch (IOException e) {
            return Maybe.nothing();
        }
    }

    /**
     * Authenticationmethod with the token and username instead of password.
     * @param username Username of user to auth.
     * @param token The given token which whould be stored for that user
     * @return Maybe(User) if the token mathches the given username
     * @throws Token.TokenExpiredException if token is too old(expired)
     */
    public static Maybe<Stored<User>> authenticate(UserName username, Token token) throws Token.TokenExpiredException {
        return storage.lookup(username, token);
    }

    /**
     * Method to validate that the username is a safe string
     *
     * @param username Username to be sanitized
     * @return Maybe.just(username)
     */
    public static Maybe<String> validateUsername(String username) {
        boolean res = username.matches("[\\w\\d]*");
        return (res) ? Maybe.just(username) : Maybe.nothing();
    }

    /**
     * Method to validate that the password is a safe string
     *
     * @param pass Password to be sanitized
     * @return Maybe.just(password)
     */
    public static Maybe<String> validatePassword(String pass) {
        boolean res = pass.matches("[\\w\\d.,:;()\\[\\]{}<>\"'#!$%&/+*?=_|\\-]*");
        return (res) ? Maybe.just(pass) : Maybe.nothing();
    }

    public static boolean sendMessage(Message message) {
        Maybe<Stored<User>> recipient = storage.lookup(message.recipient);
        if (recipient.isNothing()) {
            return false;
        }
        try {
            User newUser = recipient.force().getValue().addMessage(message);
            storage.update(recipient.force(), newUser);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Refresh the stored user object from the storage.
     *
     * @param user
     * @return Refreshed value. Nothing if the object was deleted.
     */
    public static Maybe<Stored<User>> refresh(Stored<User> user) {
        try {
            return Maybe.just(storage.refresh(user));
        } catch (ObjectDeletedException e) {
        }
        return Maybe.nothing();
    }

    /**
     * @param args TODO: Parse args to get port number
     */
    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.keyStore", "inf226.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "lengdeslaarkompleksitet");

        final RequestProcessor processor = new RequestProcessor();
        System.out.println("Staring authentication server");
        processor.start();
        SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        try (final ServerSocket socket = factory.createServerSocket(portNumber)) {
            while (!socket.isClosed()) {
                System.err.println("Waiting for client to connect…");
                Socket client = socket.accept();
                System.err.println("Client connected.");
                processor.addRequest(new RequestProcessor.Request(client));
            }
        } catch (IOException e) {
            System.out.println("Could not listen on port " + portNumber);
            e.printStackTrace();
        }
    }


}
