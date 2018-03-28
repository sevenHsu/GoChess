package GoNet;

import java.io.Serializable;

public class InfoRegisterLogin implements Serializable {
	private String name, password, operate;

	public InfoRegisterLogin() {

	}

	public InfoRegisterLogin(String name, String password, String operate) {
		this.name = name;
		this.password = password;
		this.operate = operate;
	}

	public String getName() {
		return this.name;
	}

	public String getPassword() {
		return this.password;
	}

	public String getOperate() {
		return this.operate;
	}

}
