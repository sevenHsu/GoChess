package GoNet;

import java.io.Serializable;

public class InfoDuiyiCommand implements Serializable {
	private String[] command = { "过子", "悔棋", "和棋", "数目", "认输", "退出" };
	private String[] commandType = { "申请", "执行", "同意", "拒绝" };
	private int commd, commdtype;

	public InfoDuiyiCommand() {

	}

	public InfoDuiyiCommand(int commd, int commdtype) {
		this.commd = commd;
		this.commdtype = commdtype;
	}

	public String getCommand() {
		return this.command[commd];
	}

	public String getCommandType() {
		return this.commandType[commdtype];
	}
}
