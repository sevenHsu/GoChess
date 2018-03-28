package GoNet;

import java.io.Serializable;

public class InfoMyInfo implements Serializable {
	private String myName, operate;

	public InfoMyInfo() {

	}

	public InfoMyInfo(String myName, String operate) {
		this.myName = myName;
		this.operate = operate;
	}

	public String getMyName() {
		return this.myName;
	}

	public String getOperate() {
		return this.operate;
	}
}
