package GoView;

import java.awt.Component;
import javax.swing.*;

public class Player {
	private int width;
	private String name;
	private int level;
	private int win;
	private int lose;
	private int state;
	private String states[] = { "����", "�����", "����", "������" },
			levels[] = { "����", "һ��", "����", "����", "�Ķ�", "���", "����", "�߶�", "�˶�", "�Ŷ�" };
	private String flag[] = { "��", "��" };

	public Player(String name, int level, int win, int lose, int state) {
		this.name = name;
		this.level = level;
		this.win = win;
		this.lose = lose;
		this.state = state;
	}

	public Object[] get() {
		return new Object[] { name, levels[level], win, lose, states[state] };
	}
}
