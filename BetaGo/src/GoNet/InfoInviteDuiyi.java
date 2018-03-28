package GoNet;

import java.io.Serializable;

public class InfoInviteDuiyi implements Serializable {
	private String player1, player2;
	private String operate;// invite,Receive,Refuse

	public InfoInviteDuiyi() {

	}

	public InfoInviteDuiyi(String player1, String player2, String operate) {
		this.player1 = player1;
		this.player2 = player2;
		this.operate = operate;
	}

	public String getPlayer1() {
		return this.player1;
	}

	public String getPlayer2() {
		return this.player2;
	}

	public String getOperate() {
		return this.operate;
	}
}
