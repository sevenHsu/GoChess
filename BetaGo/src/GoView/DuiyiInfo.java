package GoView;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.text.SimpleDateFormat;

import javax.swing.Timer;

import GoNet.SocketConn;

import javax.swing.*;

public class DuiyiInfo extends JPanel {
	private int window_width = Toolkit.getDefaultToolkit().getScreenSize().width;
	private int window_height = Toolkit.getDefaultToolkit().getScreenSize().height;
	private JTextArea white_text, black_text;
	private String player1_name, player2_name, player1_level, player2_level;
	private String str1, str2;
	private Font font = new Font(null, 25, 25);
	private int countWhite = 0, countBlack = 0;// 被吃掉的白子和黑子數
	private int dateWhite = 1800, dateBlack = 1800, minWhite = 30, secWhite = 0, minBlack = 30, secBlack = 0;
	private Timer timerWhite, timerBlack;
	private String whiteTimedown = "30:00", blackTimedown = "30:00";

	public DuiyiInfo() {

	}

	public void setDuiyiInfo(String player1, String player2, String player1_level, String player2_level) {
		this.setPreferredSize(new Dimension(window_width - window_height - 200, window_height * 1 / 5));
		this.player1_name = player1;
		this.player2_name = player2;
		this.player1_level = player1_level;
		this.player2_level = player2_level;
		this.setLayout(new GridLayout(1, 2));
		white_text = new JTextArea();
		white_text.setEditable(false);
		white_text.setFont(font);
		white_text.setForeground(Color.black);
		white_text.setBackground(Color.white);
		black_text = new JTextArea();
		black_text.setEditable(false);
		black_text.setFont(font);
		black_text.setForeground(Color.white);
		black_text.setBackground(Color.black);
		this.add(white_text);
		this.add(black_text);
		timerWhite = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dateWhite -= 1;
				minWhite = dateWhite / 60;
				secWhite = dateWhite % 60;
				if (dateWhite <= 0)
					timeOut(1);
				update();
			}
		});
		timerBlack = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dateBlack -= 1;
				minBlack = dateBlack / 60;
				secBlack = dateBlack % 60;
				if (dateBlack <= 0)
					timeOut(2);
				update();
			}
		});
		update();
	}

	private static volatile DuiyiInfo instance;

	// 获取DuiyiInfo对象的单列模式函数
	public static DuiyiInfo getInstance() {
		if (instance == null) {
			synchronized (DuiyiInfo.class) {
				if (instance == null) {
					instance = new DuiyiInfo();
				}
			}
		}
		return instance;
	}
	public void clearinstance(){
		instance=new DuiyiInfo();
	}

	public void timeOut(int type) {
		SocketConn socketConn=new SocketConn().getInstance();
		Qipan qipan = new Qipan().getInstance();
		qipan.setOver();
		ChatRoom chatRoom = new ChatRoom().getInstance();
		MyInfo myInfo = new MyInfo().getInstance();
		if (type == 1) {
			chatRoom.setSystemMessage("白方（" + player1_name + "）超时");
			chatRoom.setSystemMessage("黑方（" + player2_name + "）胜");
			if(myInfo.getMyNmae().equals(player2_name))
				socketConn.sendWinLose(player2_name,player1_name);
			
		} else {
			chatRoom.setSystemMessage("黑方（" + player2_name + "）超时");
			chatRoom.setSystemMessage("白方（" + player1_name + "）胜");
			if(myInfo.getMyNmae().equals(player1_name))
				socketConn.sendWinLose(player1_name,player2_name);
		}
	}

	public String getCompetitor() {
		MyInfo myInfo = new MyInfo().getInstance();
		return (myInfo.getMyNmae()).equals(player1_name) ? player2_name : player1_name;
	}

	public String getCompetitorWorB() {
		MyInfo myInfo = new MyInfo().getInstance();
		return (myInfo.getMyNmae()).equals(player1_name) ? "黑方" : "白方";
	}

	public String getMyWorB() {
		MyInfo myInfo = new MyInfo().getInstance();
		return (myInfo.getMyNmae()).equals(player1_name) ? "白方" : "黑方";
	}

	public void setEatChessCount(int eatCount) {// 白子正數，黑子負數
		if (eatCount > 400 && eatCount < -400) {
			return;
		} else if (eatCount > 0) {
			countWhite += eatCount;
			update();
		} else if (eatCount < 0) {
			countBlack += eatCount;
			update();
		} else {
		}
	}

	public void timerStop() {
		timerWhite.stop();
		timerBlack.stop();
	}

	public void timerStart(int type) {
		if (type == 1) {
			timerWhite.stop();
			timerBlack.start();
		} else {
			timerBlack.stop();
			timerWhite.start();
		}
	}

	public String setFormal(int min, int sec) {
		if (min < 10 && sec < 10)
			return "0" + min + ":0" + sec;
		else if (min < 10 && sec >= 10)
			return "0" + min + ":" + sec;
		else if (min >= 10 && sec < 10)
			return min + ":0" + sec;
		else
			return min + ":" + sec;
	}

	public void update() {
		whiteTimedown = setFormal(minWhite, secWhite);
		blackTimedown = setFormal(minBlack, secBlack);
		white_text.setText("白   方：" + player1_name + "\n等   级：" + player1_level + "\n倒计时：" + whiteTimedown + "\n提   子："
				+ (-countBlack));
		black_text.setText("黑   方：" + player2_name + "\n等   级：" + player2_level + "\n倒计时：" + blackTimedown + "\n提   子："
				+ countWhite);

	}
}
