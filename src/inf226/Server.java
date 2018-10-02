package inf226;

import inf226.Storage.KeyedStorage;
import inf226.Storage.Storage.ObjectDeletedException;
import inf226.Storage.Stored;
import inf226.Storage.TransientStorage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * The Server main class. This implements all critical server functions.
 *
 * @author INF226
 */
public class Server {
    private static final int portNumber = 1337;
    private static final KeyedStorage<String, User> storage
            = new TransientStorage<>(User::getName);

    public static Maybe<Stored<User>> authenticate(String username, String password) {
        return Maybe.nothing();
    }

    public static Maybe<Stored<User>> register(String username, String password) {
        // TODO: Implement user registration
        return Maybe.nothing();
    }

    public static Maybe<Token> createToken(Stored<User> user) {
        // TODO: Implement token creation
        return Maybe.nothing();
    }

    public static Maybe<Stored<User>> authenticate(String username, Token token) {
        // TODO: Implement user authentication
        return Maybe.nothing();
    }

    /**
     * Method to validate that the username is a safe string
     * @param username Username to be sanitized
     * @return Maybe.just(username)
     */
    public static Maybe<String> validateUsername(String username) {
        boolean res = username.matches("/[\\w\\d]*/gim");
        return (res) ? Maybe.just(username) : Maybe.nothing();
    }

    /**
     * Method to validate that the password is a safe string
     * @param pass Password to be sanitized
     * @return Maybe.just(password)
     */
    public static Maybe<String> validatePassword(String pass) {
        boolean res = pass.matches("/[\\w\\d.,:;()\\[\\]{}<>\"'#!$%&/+*?=_|-]*/gim");
        return (res) ? Maybe.just(pass) : Maybe.nothing();
    }

    public static boolean sendMessage(Stored<User> sender, Message message) {
        // TODO: Implement the message sending.
        return false;
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
        } catch (IOException e) {
        }
        return Maybe.nothing();
    }

    /**
     * @param args TODO: Parse args to get port number
     */
    public static void main(String[] args) {
        final RequestProcessor processor = new RequestProcessor();
        System.out.println("Staring authentication server");
        processor.start();
        try (final ServerSocket socket = new ServerSocket(portNumber)) {
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
