package GoNet;

import java.io.Serializable;

public class InfoMessage implements Serializable {
	private String message;

	public InfoMessage() {

	}

	public InfoMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}
}
