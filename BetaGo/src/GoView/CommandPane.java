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
		button_jump = new JButton("����һ��");
		button_jump.addActionListener(this);
		button_regreat = new JButton("�������");
		button_regreat.addActionListener(this);
		button_peace = new JButton("�������");
		button_peace.addActionListener(this);
		button_count = new JButton("������Ŀ");
		button_count.addActionListener(this);
		button_lose = new JButton("��Ը����");
		button_lose.addActionListener(this);
		button_quite = new JButton("�˳��Ծ�");
		button_quite.addActionListener(this);
		this.add(button_jump);
		this.add(button_regreat);
		this.add(button_peace);
		this.add(button_count);
		this.add(button_lose);
		this.add(button_quite);
	}

	// socketConn.sendDuiyiCommand(int command, int type)����command��ָ�type��ָ������
	public void actionPerformed(ActionEvent ev) {
		ChatRoom chatRoom = new ChatRoom().getInstance();
		DuiyiInfo duiyiInfo = new DuiyiInfo().getInstance();
		Qipan qipan = new Qipan().getInstance();
		MyInfo myInfo = new MyInfo().getInstance();
		String myName = myInfo.getMyNmae();
		String myCompetitor = duiyiInfo.getCompetitor();
		String myWorB = duiyiInfo.getMyWorB();
		String comWorB = duiyiInfo.getCompetitorWorB();
		if (ev.getActionCommand().equals("����һ��")) {
			if (qipan.getEnable() && !qipan.getOver()) {
				qipan.selfPass();
				chatRoom.setSystemMessage(myWorB + "��" + myName + "������");
				socketConn.sendDuiyiCommand(0, 1);
			}
		}
		if (ev.getActionCommand().equals("�������")) {
			if (qipan.getEnable() && !qipan.getOver() && qipan.isStart) {
				int regreat = option_pane.showConfirmDialog(qipan.getParent(), "ȷ��������壿", "��������",
						JOptionPane.YES_NO_OPTION);
				if (regreat == 0) {
					chatRoom.setSystemMessage("�ȴ��Է�ͬ�����");
					qipan.setEnable(false);
					qipan.setTimer(myWorB.equals("�׷�") ? 2 : 1);
					socketConn.sendDuiyiCommand(1, 0);
				}
			}
		}
		if (ev.getActionCommand().equals("�������")) {
			if (qipan.getEnable() && !qipan.getOver()) {
				int peace = option_pane.showConfirmDialog(qipan.getParent(), "ȷ��������壿���Է�ͬ�����ͶԷ���ʤ����������ı䣩", "��������",
						JOptionPane.YES_NO_OPTION);
				if (peace == 0) {
					chatRoom.setSystemMessage("�ȴ��Է�ͬ�����");
					qipan.setEnable(false);
					qipan.setTimer(myWorB.equals("�׷�") ? 2 : 1);
					socketConn.sendDuiyiCommand(2, 0);
				}
			}
		}
		if (ev.getActionCommand().equals("������Ŀ")) {
			if (qipan.getEnable() && !qipan.getOver()) {
				int count = option_pane.showConfirmDialog(qipan.getParent(), "ȷ��������Ŀ����ȷ������ȡ�Է����ӣ�", "��Ŀ����",
						JOptionPane.YES_NO_OPTION);
				if (count == 0) {
					chatRoom.setSystemMessage("�ȴ��Է�ͬ����Ŀ");
					qipan.setEnable(false);
					qipan.setTimer(myWorB.equals("�׷�") ? 2 : 1);
					socketConn.sendDuiyiCommand(3, 0);
				}
			}
		}
		if (ev.getActionCommand().equals("��Ը����")) {
			if (qipan.getEnable() && !qipan.getOver()) {
				int lose = option_pane.showConfirmDialog(qipan.getParent(), "ȷ��������", "��������",
						JOptionPane.YES_NO_OPTION);
				if (lose == 0) {
					socketConn.sendDuiyiCommand(4, 1);
					chatRoom.setSystemMessage(comWorB + "��" + myCompetitor + "��ʤ");
					myInfo.setMyState(0);
					qipan.setOver();
				}
			}
		}
		if (ev.getActionCommand().equals("�˳��Ծ�")) {
			if (!qipan.getOver()) {// �Ծ�δ����
				int exit = option_pane.showConfirmDialog(qipan.getParent(), "�Ծֻ�δ������ȷ��Ҫ�˳��Ծ���(�Ծ�δ�����˳������������)", "�˳��Ծ�����",
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
