package inf226;

import inf226.Maybe.NothingException;
import inf226.Storage.Stored;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class handles the requests from clients.
 *
 * @author INF226
 */
public final class RequestProcessor extends Thread {
    private final BlockingQueue<Request> queue;
    private final HashMap<InetAddress, ArrayList<Timestamp>> requests;

    public RequestProcessor() {
        queue = new LinkedBlockingQueue<Request>();
        requests = new HashMap<>();
    }

    /**
     * Add a request to the queue.
     *
     * @param request
     * @return
     */
    public boolean addRequest(final Request request) {
        return queue.add(request);
    }

    public void run() {
        try {
            while (true) {
                final Request request = queue.take();

                InetAddress ip = request.client.getInetAddress();
                ArrayList<Timestamp> timestamps;
                long timeOut = 10 * 60 * 1000; //10minutes

                if (requests.containsKey(ip)) {
                    timestamps = requests.get(ip);
                    ArrayList<Timestamp> newTimestamps = new ArrayList<>();
                    for (Timestamp t : timestamps) {
                        //keep only timestamps from the last 10 minutes
                        if (t.after(new Timestamp(System.currentTimeMillis() - timeOut))) {
                            newTimestamps.add(t);
                        }
                    }
                    timestamps = newTimestamps;
                } else {
                    timestamps = new ArrayList<>();
                }

                timestamps.add(new Timestamp(System.currentTimeMillis()));
                requests.put(ip, timestamps);

                //max 5 requests per 10 minutes
                if (timestamps.size() <= 5) {
                    request.start();
                } else {
                    try {
                        request.client.close();
                    } catch (IOException e) {

                    }
                }
            }
        } catch (InterruptedException e) {
        }
    }

    /**
     * The type of requests.
     *
     * @author INF226
     */
    public static final class Request extends Thread {
        private final Socket client;
        private Maybe<Stored<User>> user;

        /**
         * Create a new request from a socket connection to a client.
         *
         * @param client Socket to communicate with the client.
         */
        public Request(final Socket client) {
            this.client = client;
            user = Maybe.nothing();
        }

        @Override
        public void run() {

            try (final BufferedWriter out =
                         new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                 final BufferedReader in = new BufferedReader(
                         new InputStreamReader(client.getInputStream()))) {
                while (true) {
                    handle(in, out);
                }
            } catch (IOException e) {
                // Client disconnected
            }
            try {
                client.close();
            } catch (IOException e) {
                // Client closed.
            }
        }


		/**
		 * Handle a single request
		 * @param in Input from the client.
		 * @param out Output to the client.
		 * @throws IOException If the user hangs up unexpectedly
		 */
		private void handle(final BufferedReader in, final BufferedWriter out) throws IOException {
	    	final String requestType = Util.getLine(in);
	    	System.err.println("Request type: " + requestType);

	    	if(requestType.equals("REQUEST TOKEN")) {
	    		try {
					final Token token = Server.createToken(user.force()).force();
					out.write("TOKEN " + token.stringRepresentation());
				} catch (NothingException e) {
					out.write("FAILED");
				}
	    		out.newLine();
	    		out.flush();
	    		return;
	    	}
	    	if(requestType.equals("REGISTER")) {
	    		System.err.println("Handling registration request");
	    		user = handleRegistration(in);
	    		try {
					out.write("REGISTERED " + user.force().getValue().getName());
		    		System.err.println("Registration request succeeded.");
				} catch (NothingException e) {
					out.write("FAILED");
		    		System.err.println("Registration request failed.");
				}
	    		out.newLine();
	    		out.flush();
	    		return;
	    	}
	    	if(requestType.equals("LOGIN")) {
	    		user = handleLogin(in);
	    		try {
					out.write("LOGGED IN " + user.force().getValue().getName());
				} catch (NothingException e) {
					out.write("FAILED");
				}
	    		out.newLine();
	    		out.flush();
	    		return;
	    	}
	    	if(requestType.equals("SEND MESSAGE")) {
	    		try {
	    			final Maybe<Message> message = handleMessage(user.force().getValue().getName(),in);
	    			if(Server.sendMessage(user.force(),message.force())) {
	    				out.write("MESSAGE SENT");
	    			} else {
	    				out.write("FAILED");
	    			}
				} catch (NothingException e) {
					out.write("FAILED");
				}
	    		out.newLine();
	    		out.flush();
	    		return;
	    	}
	    	if(requestType.equals("READ MESSAGES")) {
	    		System.err.println("Handling a read message request");
	    		try {
	    			// Refresh the user object in order to get new messages.
		    		user = Server.refresh(user.force());
	    			for (Message m : user.force().getValue().getMessages()) {
	    				System.err.println("Sending message from " + m.sender);
	    				out.write("MESSAGE FROM " + m.sender); out.newLine();
	    				out.write(m.message);out.newLine();
	    				out.write(".");out.newLine();
	    				out.flush();
	    			}
	    			out.write("END OF MESSAGES");

				} catch (NothingException e) {
					out.write("FAILED");
				}
	    		out.newLine();
	    		out.flush();
	    		return;
	    	}
	    }

		/**
		 * Handle a message send request
		 * @param username The name of the user sending the message.
		 * @param in Reader to read the message data from.
		 * @return Message object.
		 */
		private static Maybe<Message> handleMessage(UserName username, BufferedReader in) throws IOException {
			final String lineOne = Util.getLine(in);
			final String lineTwo = Util.getLine(in);
			final String dotLine = Util.getLine(in);

			if (lineOne.startsWith("RECIPIENT ")) {
                try {
                    final Maybe<UserName> recipient = Maybe.just(new UserName(lineOne.substring("RECIPIENT ".length(), lineOne.length())));
                    final Maybe<String> messageText = Maybe.just(lineTwo);

                    //TODO: is it possible to get registered user?
					User user = new User(username, new Password("password"));
					final Maybe<Message> message = Maybe.just(new Message(user, recipient.force(), messageText.force()));
					return message;
				} catch (Exception e){
					return Maybe.nothing();
				}
			} else {
				return Maybe.nothing();
			}
		}

        /**
         * Handle a registration request.
         *
         * @param in Request input.
         * @return The stored user as a result of the registration.
         * @throws IOException If the client hangs up unexpectedly.
         */
        private static Maybe<Stored<User>> handleRegistration(BufferedReader in) throws IOException {
            final String lineOne = Util.getLine(in);
            final String lineTwo = Util.getLine(in);

            if (lineOne.startsWith("USER ") && lineTwo.startsWith("PASS ")) {
                try {
                    final UserName username = new UserName(lineOne.substring("USER ".length()));
                    final Password password = new Password(lineTwo.substring("PASS ".length()));
                    return Server.register(username, password);
                } catch (NothingException e) {
                    return Maybe.nothing();
                }
            } else {
                return Maybe.nothing();
            }

        }

        /**
         * Handle a login request.
         *
         * @param in Request input.
         * @return User object as a result of a successfu login.
         * @throws IOException If the user hangs up unexpectedly.
         */
        private static Maybe<Stored<User>> handleLogin(final BufferedReader in) throws IOException {

            final String lineOne = Util.getLine(in);
            final String lineTwo = Util.getLine(in);
            if (lineOne.startsWith("USER ") && lineTwo.startsWith("PASS ")) {
                try {
                    final UserName username = new UserName(lineOne.substring("USER ".length()));
                    final Password password = new Password(lineTwo.substring("PASS ".length()));
                    System.err.println("Login request from user: " + username);
                    return Server.authenticate(username, password);
                } catch (NothingException e) {
                    return Maybe.nothing();
                }
            } else {
                return Maybe.nothing();
            }
        }
    }
}
