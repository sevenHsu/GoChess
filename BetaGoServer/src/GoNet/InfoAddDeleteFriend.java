package GoNet;

import java.io.Serializable;

public class InfoAddDeleteFriend implements Serializable {
	private String friend, player, operate;

	public InfoAddDeleteFriend() {

	}

	public InfoAddDeleteFriend(String friend, String player, String operate) {
		this.friend = friend;
		this.player = player;
		this.operate = operate;
	}

	public String getFriend() {
		return this.friend;
	}

	public String getPlayer() {
		return this.player;
	}

	public String getOperate() {
		return this.operate;
	}

}
