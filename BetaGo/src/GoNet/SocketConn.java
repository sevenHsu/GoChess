package GoNet;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JOptionPane;

import GoView.ChatRoom;
import GoView.Dating;
import GoView.DuiyiInfo;
import GoView.MyFriendsPane;
import GoView.MyInfo;
import GoView.OnlinePlayerPane;
import GoView.Player;
import GoView.Qipan;
import GoView.Room;
import GoView.WarningPane;

//�����ǵ���ģʽ
public class SocketConn {
	private Socket socket;
	private ObjectOutputStream objWriter;
	private Thread readThread;
	private boolean isConnect;
	private boolean hasLogin;// ����Ѿ���¼����Ϊtrue

	public SocketConn() {
	}

	// ����Socket��ip�Ͷ˿ڣ�������socket
	public void SocketConnect(String ipAddress, int port) {
		try {
			socket = new Socket(ipAddress, port);
			objWriter = new ObjectOutputStream(socket.getOutputStream());
			readThread = new Thread(new ReaderListener());
			readThread.start();
			isConnect = true;
		} catch (Exception ev) {
			System.out.println("���ӷ�����ʧ��");
			isConnect = false;
		}
	}

	private static volatile SocketConn instance;

	// ��ȡsocket����ĵ���ģʽ����
	public static SocketConn getInstance() {
		if (instance == null) {
			synchronized (SocketConn.class) {
				if (instance == null) {
					instance = new SocketConn();
				}
			}
		}
		return instance;
	}

	// ���������Ƿ�ɹ�
	public boolean isConnected() {
		return isConnect;
	}

	// ���͵�¼��ע��ʱ���û���Ϣ
	public String RegisterLogin(String name, String password, String operate) {
		try {
			objWriter.writeObject(new InfoRegisterLogin(name, password, operate));
			objWriter.flush();
			while (infoMessageStr == null)
				System.out.print("waiting");
			if (infoMessageStr.equals("���ѵ�¼"))
				hasLogin = true;
			return infoMessageStr;
		} catch (Exception ev) {
			ev.printStackTrace();
			if (operate.equals("Login"))
				return "��¼����";
			else
				return "ע��ʧ��";
		}
	}

	// ��������ͼ��뷿����ս
	public void CreateChallengeRoom(String player1, String player2) {
		MyInfo myInfo = new MyInfo().getInstance();
		Dating dating = new Dating().getInstance();
		try {
			objWriter.writeObject(new InfoCreateChallengeRoom(player1, player2));
			objWriter.flush();
			if (player2 == null) {
				while (infoMessageStr == null)
					System.out.print("waiting");
				new WarningPane(dating, infoMessageStr);
				if (infoMessageStr.endsWith("�ɹ�"))
					myInfo.setMyState(1);
			}
			setInfoMessage();
		} catch (Exception ev) {
			ev.printStackTrace();
			// Dating dating = new Dating().getInstance();
			new WarningPane(dating.getParent(), "����ʧ��");
			setInfoMessage();
		}
	}

	// �������������Ծ�
	public void DeleteRoom(String player1, String player2) {
		MyInfo myInfo = new MyInfo().getInstance();
		Dating dating = new Dating().getInstance();
		try {
			objWriter.writeObject(new InfoDeleteRoom(player1, player2));
			objWriter.flush();
			while (infoMessageStr == null)
				System.out.print("Waiting");
			if (player2 == null)
				new WarningPane(dating, infoMessageStr);
			if (infoMessageStr.endsWith("�ɹ�"))
				myInfo.setMyState(0);
			setInfoMessage();
		} catch (Exception ev) {
			if (player2 == null)
				new WarningPane(dating, "����ʧ��");
		}
	}

	// ���Ӻ�ɾ������
	public void AddDeleteFriend(String friend, String Player, String operate) {
		Dating dating = new Dating().getInstance();
		if (dating.my_friends_pane.isFriend(friend) && operate.equals("Apply")) {
			new WarningPane(dating, friend + "�Ѿ�����ĺ���");
		} else {
			try {
				objWriter.writeObject(new InfoAddDeleteFriend(friend, Player, operate));
				objWriter.flush();
				if (operate.equals("Apply")) {
					new WarningPane(dating, "�ȴ��Է�ͬ��");
				}
				if (operate.equals("DeleteFriend")) {
					while (infoMessageStr == null)
						System.out.print("Waiting");
					new WarningPane(dating, infoMessageStr);
					setInfoMessage();
				}
			} catch (Exception ev) {
				ev.printStackTrace();
				new WarningPane(dating, "����ʧ��");
				setInfoMessage();
			}
		}
	}

	// ��¼�ɹ�������������������������������Ϣ
	public void OnlineDeal(String myName, String operate) {
		try {
			objWriter.writeObject(new InfoMyInfo(myName, operate));
			objWriter.flush();
		} catch (Exception ev) {
			ev.printStackTrace();
		}
	}

	// �����յ��ĺ�������
	public void dealFriendCommand(InfoAddDeleteFriend infoAddDeleteFriend) {
		Dating dating = new Dating().getInstance();
		JOptionPane option_pane = new JOptionPane(0);
		String friend = infoAddDeleteFriend.getFriend();
		String player = infoAddDeleteFriend.getPlayer();
		String operate = infoAddDeleteFriend.getOperate();
		if (operate.equals("Apply")) {
			int agree = option_pane.showConfirmDialog(dating, player + "���������Ϊ���ѣ��Ƿ�ͬ�⣿", "��Ӻ�������",
					JOptionPane.YES_NO_OPTION);
			if (agree == 0) {
				AddDeleteFriend(friend, player, "Agree");
			} else {
				AddDeleteFriend(friend, player, "Refuse");
			}
		} else if (operate.equals("Agree")) {
			new WarningPane(dating, friend + "ͬ������ĺ�������");
			AddDeleteFriend(friend, player, "AddFriend");
		} else if (operate.equals("Refuse")) {
			new WarningPane(dating, friend + "�ܾ�����ĺ�������");
		}
	}

	// �յ���ҡ�������Ϣ������ʾ����ҡ��������
	public void UpdatePlayerFriend(MyResultSet myset) {
		if (myset.getType().equals("Players")) {
			Dating dating = new Dating().getInstance();
			OnlinePlayerPane onlinePlayerPane = dating.online_player_pane;// new
																			// OnlinePlayerPane().getInstance();
			onlinePlayerPane.removeAllRow();
			while (myset.next()) {
				String name = myset.getString(1);
				int level = Integer.parseInt(myset.getString(2));
				int win = Integer.parseInt(myset.getString(3));
				int lose = Integer.parseInt(myset.getString(4));
				int state = Integer.parseInt(myset.getString(5));
				onlinePlayerPane.table_model.addRow(new Player(name, level, win, lose, state).get());
			}
			onlinePlayerPane.SetSelfForeground();
			onlinePlayerPane.repaint();
			System.out.println("������ҳɹ�");
		} else if (myset.getType().equals("Friends")) {
			Dating dating = new Dating().getInstance();
			MyFriendsPane myFriendsPane = dating.my_friends_pane;// new
																	// MyFriendsPane().getInstance();
			myFriendsPane.removeAllRow();
			while (myset.next()) {
				String name = myset.getString(1);
				int level = Integer.parseInt(myset.getString(2));
				int win = Integer.parseInt(myset.getString(3));
				int lose = Integer.parseInt(myset.getString(4));
				int state = Integer.parseInt(myset.getString(5));
				myFriendsPane.table_model.addRow(new Player(name, level, win, lose, state).get());
			}
			myFriendsPane.repaint();
			System.out.println("���º��ѳɹ�");
		} else {
		}
	}

	// ���������ս����Ϣ
	public void sendInviteDuiyi(String player1, String player2, String operate) {
		MyInfo myInfo = new MyInfo().getInstance();
		Dating dating = new Dating().getInstance();
		if (myInfo.getMyState() == 3)
			new WarningPane(dating.getParent(), "�����ڶ�����\n�޷����뷿��������ս");
		else if (myInfo.getMyState() == 1)
			new WarningPane(dating.getParent(), "�����������\n�޷����뷿��������ս");
		else {
			try {
				objWriter.writeObject(new InfoInviteDuiyi(player1, player2, operate));
				objWriter.flush();
				if (operate.equals("Invite"))
					new WarningPane(dating, "�ȴ��Է�ͬ��");
			} catch (Exception ev) {
				new WarningPane(dating, "����ʧ��");
			}
		}
	}

	// ���������ս��Ϣ
	public void dealInviteDuiyi(InfoInviteDuiyi infoInviteDuiyi) {
		Dating dating = new Dating().getInstance();
		JOptionPane option_pane = new JOptionPane(0);
		String player1 = infoInviteDuiyi.getPlayer1();
		String player2 = infoInviteDuiyi.getPlayer2();
		String operate = infoInviteDuiyi.getOperate();
		if (operate.equals("Invite")) {
			int agree = option_pane.showConfirmDialog(dating, player1 + "���������һ�֣��Ƿ������ս��", "��Ӻ�������",
					JOptionPane.YES_NO_OPTION);
			if (agree == 0) {
				new WarningPane(dating, "�ȴ��Է�����");
				sendInviteDuiyi(player1, player2, "Agree");
			} else {
				sendInviteDuiyi(player1, player2, "Refuse");
			}
		} else if (operate.equals("Agree")) {
			new WarningPane(dating, player2 + "�����������ս");
			CreateChallengeRoom(player1, player2);
		} else if (operate.equals("Refuse")) {
			new WarningPane(dating, player2 + "�ܾ��������ս");
		}
	}

	// ��ʾ�Ծ����˫������Ϣ
	public void showCompetetion(InfoPlayerCompetitor infoPlayerCompetitor) {
		MyInfo myInfo = new MyInfo().getInstance();
		String player1 = infoPlayerCompetitor.getPlayer1();
		String player2 = infoPlayerCompetitor.getPlayer2();
		System.out.println(player1 + "," + player2);//
		Dating dating = new Dating().getInstance();
		String player1Level = dating.online_player_pane.getPlayerValue(player1);
		String player2Level = dating.online_player_pane.getPlayerValue(player2);
		System.out.println(player1 + ":" + player1Level + "," + player2 + ":" + player2Level);
		DuiyiInfo duiyiInfo = new DuiyiInfo().getInstance();
		duiyiInfo.setDuiyiInfo(player1, player2, player1Level, player2Level);
		Qipan qipan = new Qipan().getInstance();
		qipan.setMyType(myInfo.getMyNmae().equals(player1) ? 1 : 2);
		dating.openQipan();
	}

	// �����յ����ֵĶ�������
	public void commandDeal(String command, String CommandType) {
		JOptionPane option_pane = new JOptionPane(0);
		ChatRoom chatRoom = new ChatRoom().getInstance();
		DuiyiInfo duiyiInfo = new DuiyiInfo().getInstance();
		Qipan qipan = new Qipan().getInstance();
		MyInfo myInfo = new MyInfo().getInstance();
		String myName = myInfo.getMyNmae();
		String myCompetitor = duiyiInfo.getCompetitor();
		String myWorB = duiyiInfo.getMyWorB();
		String comWorB = duiyiInfo.getCompetitorWorB();
		if (command.equals("����")) {// �Է�����
			chatRoom.setSystemMessage(myCompetitor + "�Ѿ�����");
			chatRoom.setSystemMessage(myWorB + "��" + myName + "��ʤ");
			myInfo.setMyState(0);
			qipan.setOver();
		}
		if (command.equals("�˳�")) {// �Է�����
			chatRoom.setSystemMessage(myCompetitor + "�ӳ�����");
			chatRoom.setSystemMessage(myWorB + "��" + myName + "��ʤ");
			myInfo.setMyState(0);
			qipan.setOver();
		}
		if (command.equals("����")) {// �Է�����
			chatRoom.setSystemMessage(comWorB + "��" + myCompetitor + "������");
			qipan.otherPass();
		}
		if (CommandType.equals("����")) {// �Է�������塢���塢��Ŀ
			qipan.setEnable(false);
			qipan.setTimer(myWorB.equals("�׷�") ? 1 : 2);
			chatRoom.setSystemMessage(comWorB + "��" + myCompetitor + "������" + command);
			if (command.equals("����")) {
				int regreat = option_pane.showConfirmDialog(qipan.getParent(), "�Է�������壬�Ƿ�ͬ�⣿", "�Է���������",
						JOptionPane.YES_NO_OPTION);
				if (regreat == 0) {
					sendDuiyiCommand(1, 2);
					// do����
					qipan.doRegreat();
					qipan.setEnable(false);
					qipan.setTimer(myWorB.equals("�׷�") ? 2 : 1);
					chatRoom.setSystemMessage(comWorB + "��" + myCompetitor + "���ѻ���");
				} else {
					sendDuiyiCommand(1, 3);
					qipan.setEnable(false);
					qipan.setTimer(myWorB.equals("�׷�") ? 2 : 1);
					chatRoom.setSystemMessage("���Ѿܾ��Է��Ļ�������");
				}

			} else if (command.equals("����")) {
				int peace = option_pane.showConfirmDialog(qipan.getParent(), "�Է�������壬�Ƿ�ͬ�⣿��ͬ�����ͶԷ���ʤ����������ı䣩",
						"�Է���������", JOptionPane.YES_NO_OPTION);
				if (peace == 0) {
					chatRoom.setSystemMessage("˫���Ѵ�ɺ���");
					sendDuiyiCommand(2, 2);
					// do����
					qipan.doPeace();
				} else {
					sendDuiyiCommand(2, 3);
					qipan.setEnable(false);
					qipan.setTimer(myWorB.equals("�׷�") ? 2 : 1);
					chatRoom.setSystemMessage("���Ѿܾ��Է��ĺ�������");
				}

			} else if (command.equals("��Ŀ")) {
				int count = option_pane.showConfirmDialog(qipan.getParent(), "�Է�������Ŀ���Ƿ�ͬ�⣿��ȷ������ȡ�Է����ӣ�", "�Է���Ŀ����",
						JOptionPane.YES_NO_OPTION);
				if (count == 0) {
					chatRoom.setSystemMessage("������Ŀ����ʤ��");
					sendDuiyiCommand(3, 2);
					// do��Ŀ
					qipan.doCount();
				} else {
					sendDuiyiCommand(3, 3);
					qipan.setEnable(false);
					qipan.setTimer(myWorB.equals("�׷�") ? 2 : 1);
					chatRoom.setSystemMessage("���Ѿܾ��Է�����Ŀ����");
				}

			}
		} else if (CommandType.equals("ͬ��")) {
			if (command.equals("����")) {
				// do����
				qipan.doRegreat();
				qipan.setEnable(true);
				qipan.setTimer(myWorB.equals("�׷�") ? 1 : 2);
				chatRoom.setSystemMessage("�Է�ͬ�����");
			} else if (command.equals("����")) {
				// do����
				qipan.doPeace();
				chatRoom.setSystemMessage("˫���Ѵ�ɺ���");
			} else if (command.equals("��Ŀ")) {
				// do��Ŀ
				qipan.doCount();
				chatRoom.setSystemMessage("�Է�ͬ����Ŀ");
			}

		} else if (CommandType.equals("�ܾ�")) {
			if (command.equals("����")) {
				chatRoom.setSystemMessage("�Է��ܾ���Ļ���");
				qipan.setEnable(true);
				qipan.setTimer(myWorB.equals("�׷�") ? 1 : 2);

			} else if (command.equals("����")) {
				chatRoom.setSystemMessage("�Է��ܾ�����");
				qipan.setEnable(true);
				qipan.setTimer(myWorB.equals("�׷�") ? 1 : 2);

			} else if (command.equals("��Ŀ")) {
				chatRoom.setSystemMessage("�Է��ܾ���Ŀ");
				qipan.setEnable(true);
				qipan.setTimer(myWorB.equals("�׷�") ? 1 : 2);
			}
		}
	}

	// �l��������Ϣ
	public void sendQiziPos(int i, int j, int type) {
		try {
			objWriter.writeObject(new InfoQiziPos(i, j, type));
			objWriter.flush();
		} catch (Exception ev) {
			ev.printStackTrace();
		}
	}

	// �l��������Ϣ
	public void sendChatMessage(String chatMessage) {
		try {
			objWriter.writeObject(new InfoChatMessage(chatMessage));
			objWriter.flush();
		} catch (Exception ev) {
			ev.printStackTrace();
		}
	}

	// ���Ͷ��Ĺ����е�ָ�������ӡ����塢��͡������
	public void sendDuiyiCommand(int command, int type) {
		try {
			objWriter.writeObject(new InfoDuiyiCommand(command, type));
			objWriter.flush();
		} catch (Exception ev) {
			ev.printStackTrace();
		}
	}
	
	//������Ӯ��Ϣ
	public void sendWinLose(String Winer,String Loser){
		try{
			objWriter.writeObject(new InfoWinLose(Winer,Loser));
			objWriter.flush();
		}catch(Exception ev){
			
		}
	}

	// ���x�K�O�÷��������ص���Ϣ
	private String infoMessageStr = null;

	public void setInfoMessage() {
		infoMessageStr = null;
	}

	// �����࣬���̵߳���ʽ����
	public class ReaderListener implements Runnable {
		private ObjectInputStream threadObjReader;

		public ReaderListener() {
			try {
				threadObjReader = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
			} catch (Exception ev) {
				ev.printStackTrace();
			}
		}

		public void run() {
			Object obj;
			try {
				while ((obj = threadObjReader.readObject()) != null) {
					String className = obj.getClass().getName();
					if (className.equals("GoNet.InfoAddDeleteFriend")) {
						InfoAddDeleteFriend infoAddDeleteFriend = (InfoAddDeleteFriend) obj;
						dealFriendCommand(infoAddDeleteFriend);
					}
					if (className.equals("GoNet.InfoPlayerCompetitor")) {
						InfoPlayerCompetitor infoPlayerCompetitor = (InfoPlayerCompetitor) obj;
						System.out.println("�յ��Ծ���Ϣ");
						showCompetetion(infoPlayerCompetitor);
					}
					// ֱ�Ӹ��·��伯��
					if (className.equals("GoNet.RoomResult")) {
						RoomResult roomSet = (RoomResult) obj;
						System.out.println("\n�յ����伯��");
						Dating dating = new Dating().getInstance();
						dating.updateRooms(roomSet);
					}
					if (className.equals("GoNet.InfoChatMessage")) {
						InfoChatMessage infoChatMessage = (InfoChatMessage) obj;
						ChatRoom chatRoom = new ChatRoom().getInstance();
						chatRoom.setReceivedMessage(infoChatMessage.getChatMessage());
					}
					if (className.equals("GoNet.InfoMessage")) {
						InfoMessage infoMessage = (InfoMessage) obj;
						infoMessageStr = infoMessage.getMessage();
					}

					if (className.equals("GoNet.InfoInviteDuiyi")) {
						InfoInviteDuiyi infoInviteDuiyi = (InfoInviteDuiyi) obj;
						dealInviteDuiyi(infoInviteDuiyi);
					}

					if (className.equals("GoNet.MyResultSet")) {
						MyResultSet myset = (MyResultSet) obj;
						UpdatePlayerFriend(myset);
					}
					if (className.equals("GoNet.InfoQiziPos")) {
						InfoQiziPos infoQiziPos = (InfoQiziPos) obj;
						int i = infoQiziPos.getI();
						int j = infoQiziPos.getJ();
						int type = infoQiziPos.getType();
						Qipan qipan = new Qipan().getInstance();
						qipan.setOtherType(i, j, type);
					}
					if (className.equals("GoNet.InfoDuiyiCommand")) {
						InfoDuiyiCommand infoDuiyiCommand = (InfoDuiyiCommand) obj;
						String command = infoDuiyiCommand.getCommand();
						String commandType = infoDuiyiCommand.getCommandType();
						commandDeal(command, commandType);
					}
				}

			} catch (Exception ev) {
				Dating dating = new Dating().getInstance();
				OnlinePlayerPane onlinePlayerPane = dating.online_player_pane;
				if (hasLogin == false) {
					new WarningPane(onlinePlayerPane.getParent().getParent().getParent(), "���Ѷ��ߣ�����������");
					System.exit(0);
				} else
					hasLogin = false;
			} finally {
				try {
					threadObjReader.close();
					objWriter.close();
					socket.close();
				} catch (Exception ev) {

				}
			}
		}
	}
}