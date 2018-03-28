package GoNet;

import java.io.Serializable;

public class InfoPlayerCompetitor implements Serializable {
	private String player1;
	private String player2;

	public InfoPlayerCompetitor() {

	}

	public InfoPlayerCompetitor(String player1, String player2) {
		this.player1 = player1;
		this.player2 = player2;
	}

	public String getPlayer1() {
		return this.player1;
	}

	public String getPlayer2() {
		return this.player2;
	}
}
