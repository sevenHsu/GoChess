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
	private String states[] = { "空闲", "组局中", "离线", "对弈中" },
			levels[] = { "新手", "一段", "二段", "三段", "四段", "五段", "六段", "七段", "八段", "九段" };
	private String flag[] = { "否", "是" };

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
