package GoView;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import GoNet.RoomResult;
import GoNet.SocketConn;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

//����ѡ����
public class Dating extends JPanel implements ActionListener {
	public JSplitPane split_pane, split_pane2;
	private JTabbedPane tabbed_pane;
	public OnlinePlayerPane online_player_pane;
	public MyFriendsPane my_friends_pane;
	private JPanel l_pane, operate_pane;
	private JButton button_room, button_cancel, button_exit;
	JOptionPane option_pane;
	private Duiyi duiyi;
	private ArrayList<Room> rooms;

	public Dating() {
		option_pane = new JOptionPane(0);
		rooms = new ArrayList<Room>();
		l_pane = new JPanel();
		l_pane.setBorder(new TitledBorder("����"));
		l_pane.setLayout(new FlowLayout(FlowLayout.LEFT));
		tabbed_pane = new JTabbedPane();
		online_player_pane = new OnlinePlayerPane();
		my_friends_pane = new MyFriendsPane();
		tabbed_pane.add("��������", online_player_pane);
		tabbed_pane.add("�ҵĺ���", my_friends_pane);
		operate_pane = new JPanel();
		operate_pane.setLayout(new GridLayout(1, 1));
		button_room = new JButton("��������");
		button_cancel = new JButton("��������");
		button_exit = new JButton("�˳�����");
		operate_pane.add(button_room);
		button_room.addActionListener(this);
		operate_pane.add(button_cancel);
		button_cancel.addActionListener(this);
		operate_pane.add(button_exit);
		button_exit.addActionListener(this);
		split_pane2 = new JSplitPane(0, tabbed_pane, operate_pane);
		split_pane2.setDividerSize(5);
		split_pane2.setDividerLocation(660);
		split_pane = new JSplitPane(1, l_pane, split_pane2);
		split_pane.setDividerSize(5);
		this.setLayout(new BorderLayout());
		this.add(split_pane);
	}

	// ������ʾ������Ϣ
	public void updateRooms(RoomResult roomSet) {
		Iterator<Room> it = rooms.iterator();
		while (it.hasNext()) {
			Room itRoom = (Room) it.next();
			it.remove();
			l_pane.remove(itRoom);
		}
		while (roomSet.next()) {
			String player1 = roomSet.getString(1);
			String player2 = roomSet.getString(2);
			if (player2 == null) {
				Room nowRoom = new Room(player1);
				rooms.add(nowRoom);
				l_pane.add(nowRoom);
			} else {
				Room nowRoom = new Room(player1, player2);
				rooms.add(nowRoom);
				l_pane.add(nowRoom);
			}
		}
		this.getParent().repaint();
	}

	public void actionPerformed(ActionEvent ev) {
		if (ev.getActionCommand().equals("��������")) {
			MyInfo myInfo = new MyInfo().getInstance();
			int myState = myInfo.getMyState();
			if (myState == 3)
				new WarningPane(this, "�����ڶ����У��޷�������������");
			else if (myState == 1)
				new WarningPane(this, "����������У��޷�������������");
			else {
				String myName = myInfo.getMyNmae();
				SocketConn socketConn = new SocketConn().getInstance();
				System.out.println("���ʹ���������Ϣ");
				socketConn.CreateChallengeRoom(myName, null);
			}
		} else if (ev.getActionCommand().equals("�˳�����")) {
			int quit = option_pane.showConfirmDialog(this.getParent(), "ȷ���˳���", "�˳�����", JOptionPane.YES_NO_OPTION);
			if (quit == 0)
				System.exit(0);
		} else if (ev.getActionCommand().equals("��������")) {
			MyInfo myInfo = new MyInfo().getInstance();
			int myState = myInfo.getMyState();
			if (myState == 0)
				new WarningPane(this, "�㻹δ�������䣬��ȥ������");
			else if (myState == 3)
				new WarningPane(this, "�����ڶ����У��޷���������");
			else if (myState == 1) {
				int cancel = option_pane.showConfirmDialog(this.getParent(), "ȷ��������", "��������",
						JOptionPane.YES_NO_OPTION);
				if (cancel == 0) {
					SocketConn socketConn = new SocketConn().getInstance();
					socketConn.DeleteRoom(myInfo.getMyNmae(), null);
				}
			}
		}
	}

	public void openQipan() {
		MyInfo myInfo = new MyInfo().getInstance();
		myInfo.setMyState(3);
		duiyi = new Duiyi();
		// duiyi.split3.setDividerLocation(0.2);
		duiyi.split2.setDividerLocation(0.3);
		duiyi.split1.setDividerLocation(0.7);
		this.getParent().add("����", duiyi);
		duiyi.setVisible(true);
		this.getParent().getParent().repaint();
	}

	public void closeQipan() {
		duiyi.clearInstace();
		this.getParent().remove(duiyi);
		this.getParent().getParent().repaint();
	}

	private static volatile Dating instance;

	public static Dating getInstance() {
		if (instance == null) {
			synchronized (Dating.class) {
				if (instance == null) {
					instance = new Dating();
				}
			}
		}
		return instance;
	}
}
