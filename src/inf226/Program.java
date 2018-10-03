package inf226;

/**
 * Simple main class to run either client or a server.
 * only meant to use with JAR artifact for ease of use.
 *
 * @author RakNoel
 * @version 1.1
 * @since 03.10.18
 */
public class Program {
    public static void main(String[] args) {
        String[] hostname = {"localhost"};
        switch (args[0]) {
            case "s":
            case "server":
                Server.main(null);
                break;
            case "c":
            case "client":
                Client.main(hostname);
                break;
        }
    }
}
