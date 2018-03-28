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

//此类是单列模式
public class SocketConn {
	private Socket socket;
	private ObjectOutputStream objWriter;
	private Thread readThread;
	private boolean isConnect;
	private boolean hasLogin;// 如果已经登录，则为true

	public SocketConn() {
	}

	// 设置Socket的ip和端口，并创建socket
	public void SocketConnect(String ipAddress, int port) {
		try {
			socket = new Socket(ipAddress, port);
			objWriter = new ObjectOutputStream(socket.getOutputStream());
			readThread = new Thread(new ReaderListener());
			readThread.start();
			isConnect = true;
		} catch (Exception ev) {
			System.out.println("连接服务器失败");
			isConnect = false;
		}
	}

	private static volatile SocketConn instance;

	// 获取socket对象的单列模式函数
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

	// 返回连接是否成功
	public boolean isConnected() {
		return isConnect;
	}

	// 发送登录和注册时的用户信息
	public String RegisterLogin(String name, String password, String operate) {
		try {
			objWriter.writeObject(new InfoRegisterLogin(name, password, operate));
			objWriter.flush();
			while (infoMessageStr == null)
				System.out.print("waiting");
			if (infoMessageStr.equals("你已登录"))
				hasLogin = true;
			return infoMessageStr;
		} catch (Exception ev) {
			ev.printStackTrace();
			if (operate.equals("Login"))
				return "登录出错";
			else
				return "注册失败";
		}
	}

	// 创建房间和加入房间挑战
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
				if (infoMessageStr.endsWith("成功"))
					myInfo.setMyState(1);
			}
			setInfoMessage();
		} catch (Exception ev) {
			ev.printStackTrace();
			// Dating dating = new Dating().getInstance();
			new WarningPane(dating.getParent(), "创建失败");
			setInfoMessage();
		}
	}

	// 撤销房间或结束对局
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
			if (infoMessageStr.endsWith("成功"))
				myInfo.setMyState(0);
			setInfoMessage();
		} catch (Exception ev) {
			if (player2 == null)
				new WarningPane(dating, "撤销失败");
		}
	}

	// 增加和删除好友
	public void AddDeleteFriend(String friend, String Player, String operate) {
		Dating dating = new Dating().getInstance();
		if (dating.my_friends_pane.isFriend(friend) && operate.equals("Apply")) {
			new WarningPane(dating, friend + "已经是你的好友");
		} else {
			try {
				objWriter.writeObject(new InfoAddDeleteFriend(friend, Player, operate));
				objWriter.flush();
				if (operate.equals("Apply")) {
					new WarningPane(dating, "等待对方同意");
				}
				if (operate.equals("DeleteFriend")) {
					while (infoMessageStr == null)
						System.out.print("Waiting");
					new WarningPane(dating, infoMessageStr);
					setInfoMessage();
				}
			} catch (Exception ev) {
				ev.printStackTrace();
				new WarningPane(dating, "操作失败");
				setInfoMessage();
			}
		}
	}

	// 登录成功后发送命令给服务器，返回在线玩家信息
	public void OnlineDeal(String myName, String operate) {
		try {
			objWriter.writeObject(new InfoMyInfo(myName, operate));
			objWriter.flush();
		} catch (Exception ev) {
			ev.printStackTrace();
		}
	}

	// 处理收到的好友请求
	public void dealFriendCommand(InfoAddDeleteFriend infoAddDeleteFriend) {
		Dating dating = new Dating().getInstance();
		JOptionPane option_pane = new JOptionPane(0);
		String friend = infoAddDeleteFriend.getFriend();
		String player = infoAddDeleteFriend.getPlayer();
		String operate = infoAddDeleteFriend.getOperate();
		if (operate.equals("Apply")) {
			int agree = option_pane.showConfirmDialog(dating, player + "请求添加你为好友，是否同意？", "添加好友提醒",
					JOptionPane.YES_NO_OPTION);
			if (agree == 0) {
				AddDeleteFriend(friend, player, "Agree");
			} else {
				AddDeleteFriend(friend, player, "Refuse");
			}
		} else if (operate.equals("Agree")) {
			new WarningPane(dating, friend + "同意了你的好友申请");
			AddDeleteFriend(friend, player, "AddFriend");
		} else if (operate.equals("Refuse")) {
			new WarningPane(dating, friend + "拒绝了你的好友申请");
		}
	}

	// 收到玩家、好友信息，并显示在玩家、好友面板
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
			System.out.println("更新玩家成功");
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
			System.out.println("更新好友成功");
		} else {
		}
	}

	// 发送邀请对战的信息
	public void sendInviteDuiyi(String player1, String player2, String operate) {
		MyInfo myInfo = new MyInfo().getInstance();
		Dating dating = new Dating().getInstance();
		if (myInfo.getMyState() == 3)
			new WarningPane(dating.getParent(), "你正在对弈中\n无法加入房间和邀请对战");
		else if (myInfo.getMyState() == 1)
			new WarningPane(dating.getParent(), "你正在组局中\n无法加入房间和邀请对战");
		else {
			try {
				objWriter.writeObject(new InfoInviteDuiyi(player1, player2, operate));
				objWriter.flush();
				if (operate.equals("Invite"))
					new WarningPane(dating, "等待对方同意");
			} catch (Exception ev) {
				new WarningPane(dating, "操作失败");
			}
		}
	}

	// 处理邀请对战信息
	public void dealInviteDuiyi(InfoInviteDuiyi infoInviteDuiyi) {
		Dating dating = new Dating().getInstance();
		JOptionPane option_pane = new JOptionPane(0);
		String player1 = infoInviteDuiyi.getPlayer1();
		String player2 = infoInviteDuiyi.getPlayer2();
		String operate = infoInviteDuiyi.getOperate();
		if (operate.equals("Invite")) {
			int agree = option_pane.showConfirmDialog(dating, player1 + "邀请你对弈一局，是否接受挑战？", "添加好友提醒",
					JOptionPane.YES_NO_OPTION);
			if (agree == 0) {
				new WarningPane(dating, "等待对方开局");
				sendInviteDuiyi(player1, player2, "Agree");
			} else {
				sendInviteDuiyi(player1, player2, "Refuse");
			}
		} else if (operate.equals("Agree")) {
			new WarningPane(dating, player2 + "接受了你的挑战");
			CreateChallengeRoom(player1, player2);
		} else if (operate.equals("Refuse")) {
			new WarningPane(dating, player2 + "拒绝了你的挑战");
		}
	}

	// 显示对局玩家双方的信息
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

	// 处理收到对手的对弈命令
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
		if (command.equals("认输")) {// 对方认输
			chatRoom.setSystemMessage(myCompetitor + "已经认输");
			chatRoom.setSystemMessage(myWorB + "（" + myName + "）胜");
			myInfo.setMyState(0);
			qipan.setOver();
		}
		if (command.equals("退出")) {// 对方逃跑
			chatRoom.setSystemMessage(myCompetitor + "逃出赛局");
			chatRoom.setSystemMessage(myWorB + "（" + myName + "）胜");
			myInfo.setMyState(0);
			qipan.setOver();
		}
		if (command.equals("过子")) {// 对方过子
			chatRoom.setSystemMessage(comWorB + "（" + myCompetitor + "）过子");
			qipan.otherPass();
		}
		if (CommandType.equals("申请")) {// 对方申请悔棋、和棋、数目
			qipan.setEnable(false);
			qipan.setTimer(myWorB.equals("白方") ? 1 : 2);
			chatRoom.setSystemMessage(comWorB + "（" + myCompetitor + "）申请" + command);
			if (command.equals("悔棋")) {
				int regreat = option_pane.showConfirmDialog(qipan.getParent(), "对方请求悔棋，是否同意？", "对方悔棋申请",
						JOptionPane.YES_NO_OPTION);
				if (regreat == 0) {
					sendDuiyiCommand(1, 2);
					// do悔棋
					qipan.doRegreat();
					qipan.setEnable(false);
					qipan.setTimer(myWorB.equals("白方") ? 2 : 1);
					chatRoom.setSystemMessage(comWorB + "（" + myCompetitor + "）已悔棋");
				} else {
					sendDuiyiCommand(1, 3);
					qipan.setEnable(false);
					qipan.setTimer(myWorB.equals("白方") ? 2 : 1);
					chatRoom.setSystemMessage("你已拒绝对方的悔棋申请");
				}

			} else if (command.equals("和棋")) {
				int peace = option_pane.showConfirmDialog(qipan.getParent(), "对方请求和棋，是否同意？（同意后，你和对方的胜负局数不会改变）",
						"对方和棋申请", JOptionPane.YES_NO_OPTION);
				if (peace == 0) {
					chatRoom.setSystemMessage("双方已达成和棋");
					sendDuiyiCommand(2, 2);
					// do和棋
					qipan.doPeace();
				} else {
					sendDuiyiCommand(2, 3);
					qipan.setEnable(false);
					qipan.setTimer(myWorB.equals("白方") ? 2 : 1);
					chatRoom.setSystemMessage("你已拒绝对方的和棋申请");
				}

			} else if (command.equals("数目")) {
				int count = option_pane.showConfirmDialog(qipan.getParent(), "对方请求数目，是否同意？（确定已提取对方死子）", "对方数目申请",
						JOptionPane.YES_NO_OPTION);
				if (count == 0) {
					chatRoom.setSystemMessage("正在数目计算胜负");
					sendDuiyiCommand(3, 2);
					// do数目
					qipan.doCount();
				} else {
					sendDuiyiCommand(3, 3);
					qipan.setEnable(false);
					qipan.setTimer(myWorB.equals("白方") ? 2 : 1);
					chatRoom.setSystemMessage("你已拒绝对方的数目申请");
				}

			}
		} else if (CommandType.equals("同意")) {
			if (command.equals("悔棋")) {
				// do悔棋
				qipan.doRegreat();
				qipan.setEnable(true);
				qipan.setTimer(myWorB.equals("白方") ? 1 : 2);
				chatRoom.setSystemMessage("对方同意悔棋");
			} else if (command.equals("和棋")) {
				// do和棋
				qipan.doPeace();
				chatRoom.setSystemMessage("双方已达成和棋");
			} else if (command.equals("数目")) {
				// do数目
				qipan.doCount();
				chatRoom.setSystemMessage("对方同意数目");
			}

		} else if (CommandType.equals("拒绝")) {
			if (command.equals("悔棋")) {
				chatRoom.setSystemMessage("对方拒绝你的悔棋");
				qipan.setEnable(true);
				qipan.setTimer(myWorB.equals("白方") ? 1 : 2);

			} else if (command.equals("和棋")) {
				chatRoom.setSystemMessage("对方拒绝和棋");
				qipan.setEnable(true);
				qipan.setTimer(myWorB.equals("白方") ? 1 : 2);

			} else if (command.equals("数目")) {
				chatRoom.setSystemMessage("对方拒绝数目");
				qipan.setEnable(true);
				qipan.setTimer(myWorB.equals("白方") ? 1 : 2);
			}
		}
	}

	// 發送棋子信息
	public void sendQiziPos(int i, int j, int type) {
		try {
			objWriter.writeObject(new InfoQiziPos(i, j, type));
			objWriter.flush();
		} catch (Exception ev) {
			ev.printStackTrace();
		}
	}

	// 發送聊天信息
	public void sendChatMessage(String chatMessage) {
		try {
			objWriter.writeObject(new InfoChatMessage(chatMessage));
			objWriter.flush();
		} catch (Exception ev) {
			ev.printStackTrace();
		}
	}

	// 发送对弈过程中的指令，例如过子、悔棋、求和、认输等
	public void sendDuiyiCommand(int command, int type) {
		try {
			objWriter.writeObject(new InfoDuiyiCommand(command, type));
			objWriter.flush();
		} catch (Exception ev) {
			ev.printStackTrace();
		}
	}
	
	//发送输赢信息
	public void sendWinLose(String Winer,String Loser){
		try{
			objWriter.writeObject(new InfoWinLose(Winer,Loser));
			objWriter.flush();
		}catch(Exception ev){
			
		}
	}

	// 定義並設置服務器返回的信息
	private String infoMessageStr = null;

	public void setInfoMessage() {
		infoMessageStr = null;
	}

	// 监听类，以线程的形式运行
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
						System.out.println("收到对局信息");
						showCompetetion(infoPlayerCompetitor);
					}
					// 直接更新房间集合
					if (className.equals("GoNet.RoomResult")) {
						RoomResult roomSet = (RoomResult) obj;
						System.out.println("\n收到房间集合");
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
					new WarningPane(onlinePlayerPane.getParent().getParent().getParent(), "你已断线，请重新连接");
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