package GoNet;

import java.io.Serializable;

public class InfoDuiyiCommand implements Serializable {
	private String[] command = { "����", "����", "����", "��Ŀ", "����", "�˳�" };
	private String[] commandType = { "����", "ִ��", "ͬ��", "�ܾ�" };
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
