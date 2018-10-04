package inf226;

import java.security.SecureRandom;
import java.util.Base64;

public final class Token {

	private final byte[] token;

	/**
	 * The constructor should generate a random 128 bit token
	 */
	public Token(){
		token = new byte[16];
		new SecureRandom().nextBytes(token);
	}

	public Token(String token){
		this.token = token.getBytes();
	}

	@Override
	public String toString() {
		StringBuilder strbldr = new StringBuilder();
		for (byte t : token) {
			strbldr.append((char)t);
		}
		return strbldr.toString();
	}

	/**
	 * This method should return the Base64 encoding of the token
	 * @return A Base64 encoding of the token
	 */
	public String stringRepresentation() {
		return Base64.getEncoder().encodeToString(token);
	}
}
