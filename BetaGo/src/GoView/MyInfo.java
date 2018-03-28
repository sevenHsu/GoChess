package GoView;

import GoNet.SocketConn;

public class MyInfo {
	private String myName;

	// private int myLevel;
	// private int myWin;
	// private int myLose;
	private int myState;

	public MyInfo() {

	}

	public void setMyInfo(String myName) {
		this.myName = myName;
	}

	public void setMyState(int myState) {
		this.myState = myState;
	}

	private static volatile MyInfo instance;

	public static MyInfo getInstance() {
		if (instance == null) {
			synchronized (MyInfo.class) {
				if (instance == null) {
					instance = new MyInfo();
				}
			}
		}
		return instance;
	}

	public String getMyNmae() {
		return this.myName;
	}

	public int getMyState() {
		return this.myState;
	}
}
