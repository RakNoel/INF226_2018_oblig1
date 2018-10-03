package inf226;

/**
 * TODO: Describe class
 *
 * @author RakNoel
 * @version 1.0
 * @since 03.10.18
 */
public class Program {
    public static void main(String[] args) {
        String[] port = {"localhost"};
        switch (args[0]) {
            case "s":
            case "server":
                Server.main(null);
                break;
            case "c":
            case "client":
                Client.main(port);
                break;
        }
    }
}
