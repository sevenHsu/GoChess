package GoView;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import GoNet.SocketConn;

public class CommandPane extends JPanel implements ActionListener {
	private JButton button_jump, button_regreat, button_peace, button_count, button_lose, button_quite;
	private Dating dating;
	JOptionPane option_pane;
	private Qipan qipan;
	private SocketConn socketConn;

	public CommandPane() {
		qipan = new Qipan().getInstance();
		dating = new Dating().getInstance();
		socketConn = new SocketConn().getInstance();
		option_pane = new JOptionPane(0);
		this.setLayout(new GridLayout(2, 3));
		button_jump = new JButton("放弃一手");
		button_jump.addActionListener(this);
		button_regreat = new JButton("请求悔棋");
		button_regreat.addActionListener(this);
		button_peace = new JButton("请求和棋");
		button_peace.addActionListener(this);
		button_count = new JButton("请求数目");
		button_count.addActionListener(this);
		button_lose = new JButton("自愿认输");
		button_lose.addActionListener(this);
		button_quite = new JButton("退出对局");
		button_quite.addActionListener(this);
		this.add(button_jump);
		this.add(button_regreat);
		this.add(button_peace);
		this.add(button_count);
		this.add(button_lose);
		this.add(button_quite);
	}

	// socketConn.sendDuiyiCommand(int command, int type)其中command是指令，type是指令类型
	public void actionPerformed(ActionEvent ev) {
		ChatRoom chatRoom = new ChatRoom().getInstance();
		DuiyiInfo duiyiInfo = new DuiyiInfo().getInstance();
		Qipan qipan = new Qipan().getInstance();
		MyInfo myInfo = new MyInfo().getInstance();
		String myName = myInfo.getMyNmae();
		String myCompetitor = duiyiInfo.getCompetitor();
		String myWorB = duiyiInfo.getMyWorB();
		String comWorB = duiyiInfo.getCompetitorWorB();
		if (ev.getActionCommand().equals("放弃一手")) {
			if (qipan.getEnable() && !qipan.getOver()) {
				qipan.selfPass();
				chatRoom.setSystemMessage(myWorB + "（" + myName + "）过子");
				socketConn.sendDuiyiCommand(0, 1);
			}
		}
		if (ev.getActionCommand().equals("请求悔棋")) {
			if (qipan.getEnable() && !qipan.getOver() && qipan.isStart) {
				int regreat = option_pane.showConfirmDialog(qipan.getParent(), "确定请求悔棋？", "悔棋提醒",
						JOptionPane.YES_NO_OPTION);
				if (regreat == 0) {
					chatRoom.setSystemMessage("等待对方同意悔棋");
					qipan.setEnable(false);
					qipan.setTimer(myWorB.equals("白方") ? 2 : 1);
					socketConn.sendDuiyiCommand(1, 0);
				}
			}
		}
		if (ev.getActionCommand().equals("请求和棋")) {
			if (qipan.getEnable() && !qipan.getOver()) {
				int peace = option_pane.showConfirmDialog(qipan.getParent(), "确定请求和棋？（对方同意后，你和对方的胜负局数不会改变）", "和棋提醒",
						JOptionPane.YES_NO_OPTION);
				if (peace == 0) {
					chatRoom.setSystemMessage("等待对方同意和棋");
					qipan.setEnable(false);
					qipan.setTimer(myWorB.equals("白方") ? 2 : 1);
					socketConn.sendDuiyiCommand(2, 0);
				}
			}
		}
		if (ev.getActionCommand().equals("请求数目")) {
			if (qipan.getEnable() && !qipan.getOver()) {
				int count = option_pane.showConfirmDialog(qipan.getParent(), "确定请求数目？（确定已提取对方死子）", "数目提醒",
						JOptionPane.YES_NO_OPTION);
				if (count == 0) {
					chatRoom.setSystemMessage("等待对方同意数目");
					qipan.setEnable(false);
					qipan.setTimer(myWorB.equals("白方") ? 2 : 1);
					socketConn.sendDuiyiCommand(3, 0);
				}
			}
		}
		if (ev.getActionCommand().equals("自愿认输")) {
			if (qipan.getEnable() && !qipan.getOver()) {
				int lose = option_pane.showConfirmDialog(qipan.getParent(), "确定认输吗？", "认输提醒",
						JOptionPane.YES_NO_OPTION);
				if (lose == 0) {
					socketConn.sendDuiyiCommand(4, 1);
					chatRoom.setSystemMessage(comWorB + "（" + myCompetitor + "）胜");
					myInfo.setMyState(0);
					qipan.setOver();
				}
			}
		}
		if (ev.getActionCommand().equals("退出对局")) {
			if (!qipan.getOver()) {// 对局未结束
				int exit = option_pane.showConfirmDialog(qipan.getParent(), "对局还未结束，确定要退出对局吗？(对局未结束退出，会判你输的)", "退出对局提醒",
						JOptionPane.YES_NO_OPTION);
				if (exit == 0) {
					socketConn.sendDuiyiCommand(5, 1);
					Dating dating = new Dating().getInstance();
					myInfo.setMyState(0);
					dating.closeQipan();
				}
			}
			if (qipan.getOver()) {
				myInfo.setMyState(0);
				Dating dating = new Dating().getInstance();
				dating.closeQipan();
			}

		}

	}
}
