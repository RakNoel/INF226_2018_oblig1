package inf226;

public class Message {
	public final String sender, recipient, message;
	
	Message(final User user, final String recipient, final String message) throws Invalid {
		this.sender = user.getName().toString();
		this.recipient = recipient;
		if (!valid(message))
			throw new Invalid(message);
		this.message = message;
	}

	public static boolean valid(String message) {
		for(char c : message.toCharArray()){
			if(Character.isISOControl(c)){
				return false;
			}
		}

		String messageLines[] = message.split("\\r?\\n");
		for(String line : messageLines){
			if(line.equals(".")){
				return false;
			}
		}
		return true;
	}

	public static class Invalid extends Exception {
		private static final long serialVersionUID = -3451435075806445718L;

		public Invalid(String msg) {
			super("Invalid string: " + msg);
		}
	}
}
