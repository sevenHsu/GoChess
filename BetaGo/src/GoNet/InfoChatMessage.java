package GoNet;

import java.io.Serializable;

public class InfoChatMessage implements Serializable {
	String chatMessage;

	public InfoChatMessage() {

	}

	public InfoChatMessage(String chatMessage) {
		this.chatMessage = chatMessage;
	}

	public String getChatMessage() {
		return this.chatMessage;
	}
}
