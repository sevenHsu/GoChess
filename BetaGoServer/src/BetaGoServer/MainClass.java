package BetaGoServer;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import GoNet.InfoAddDeleteFriend;
import GoNet.InfoChatMessage;
import GoNet.InfoCreateChallengeRoom;
import GoNet.InfoDeleteRoom;
import GoNet.InfoDuiyiCommand;
import GoNet.InfoInviteDuiyi;
import GoNet.InfoMessage;
import GoNet.InfoMyInfo;
import GoNet.InfoPlayerCompetitor;
import GoNet.InfoQiziPos;
import GoNet.InfoRegisterLogin;
import GoNet.InfoWinLose;
import GoNet.MyResultSet;
import GoNet.RoomResult;

public class MainClass {
	private ArrayList<PlayerSocketWriter> playerSocketWriters;
	private SqlConn sqlConn;
	ThreadPoolExecutor executor;

	public MainClass(JTextArea jtextarea) {
		try {
			jtextarea.append("服务器开启\n");
			executor = new ThreadPoolExecutor(4, 8, 1, TimeUnit.DAYS, new ArrayBlockingQueue<Runnable>(4));
			playerSocketWriters = new ArrayList<PlayerSocketWriter>();
			ServerSocket serverSocket = new ServerSocket(10000);
			sqlConn = new SqlConn();
			sqlConn.setAllOffline();
			sqlConn.deleteAllRoom();
			try {
				while (true) {
					Socket socket = serverSocket.accept();
					Mytask myTask = new Mytask(socket,jtextarea);
					executor.execute(myTask);
				}

			} catch (Exception ev) {
				ev.printStackTrace();
			} finally {
				try {
					serverSocket.close();
				} catch (Exception ev) {
					ev.printStackTrace();
				}
			}
		} catch (Exception ev) {

		}
	}

	public void sendApply(String friend, InfoAddDeleteFriend infoAddDeleteFriend) {
		Iterator<PlayerSocketWriter> itPlayerSocketWriter = playerSocketWriters.iterator();
		while (itPlayerSocketWriter.hasNext()) {
			PlayerSocketWriter playerSocketWriter = (PlayerSocketWriter) itPlayerSocketWriter.next();
			if (friend.equals(playerSocketWriter.getPlayer())) {
				try {
					ObjectOutputStream writer = playerSocketWriter.getWriter();
					writer.writeObject(infoAddDeleteFriend);
					writer.flush();
					break;
				} catch (Exception ev) {
					ev.printStackTrace();
					break;
				}
			}
		}
	}

	public void sendInvite(String player, InfoInviteDuiyi infoInviteDuiyi) {
		Iterator<PlayerSocketWriter> itPlayerSocketWriter = playerSocketWriters.iterator();
		while (itPlayerSocketWriter.hasNext()) {
			PlayerSocketWriter playerSocketWriter = (PlayerSocketWriter) itPlayerSocketWriter.next();
			if (player.equals(playerSocketWriter.getPlayer())) {
				try {
					ObjectOutputStream writer = playerSocketWriter.getWriter();
					writer.writeObject(infoInviteDuiyi);
					writer.flush();
					break;
				} catch (Exception ev) {
					ev.printStackTrace();
					break;
				}
			}
		}
	}

	public void tellFriend(String friend) {
		Iterator<PlayerSocketWriter> itPlayerSocketWriter = playerSocketWriters.iterator();
		while (itPlayerSocketWriter.hasNext()) {
			PlayerSocketWriter playerSocketWriter = (PlayerSocketWriter) itPlayerSocketWriter.next();
			if (friend.equals(playerSocketWriter.getPlayer())) {
				try {
					ResultSet rset = sqlConn.selectFriends(friend);
					MyResultSet myResultSet = new MyResultSet().create("Friends", rset);
					ObjectOutputStream writer = playerSocketWriter.getWriter();
					writer.writeObject(myResultSet);
					writer.flush();
					break;
				} catch (Exception ev) {
					ev.printStackTrace();
					break;
				}
			}
		}
	}

	public void tellMyOnlineFriend(String nowPlayer) {
		try {
			ResultSet rset = sqlConn.selectOnlineFriends(nowPlayer);
			if (rset != null) {
				while (rset.next())
					tellFriend(rset.getString(1));
			}
		} catch (Exception ev) {
			ev.printStackTrace();
		}
	}

	public void tellMyCompetitor(PlayerSocketWriter myPSW) {
		try {
			String player = myPSW.getPlayer();
			String competitor = myPSW.getCompetitor();
			ObjectOutputStream coWriter = myPSW.getCompetitorObjWriter();
			sqlConn.AlterInfo(1, "player_lose", player);
			sqlConn.AlterInfo(1, "player_win", competitor);
			coWriter.writeObject(new InfoDuiyiCommand(5, 1));
			coWriter.flush();
			sqlConn.deleteRoom(player, competitor);
			tellMyOnlineFriend(competitor);
		} catch (Exception ev) {

		}

	}

	public void tellEveryOne() {
		Iterator<PlayerSocketWriter> itPlayerSocketWriter = playerSocketWriters.iterator();
		ResultSet rset = sqlConn.selectPlayer();
		MyResultSet myset = new MyResultSet().create("Players", rset);
		while (itPlayerSocketWriter.hasNext()) {
			try {
				ObjectOutputStream writer = itPlayerSocketWriter.next().getWriter();
				writer.writeObject(myset);
				writer.flush();
			} catch (Exception ev) {
				ev.printStackTrace();
			}
		}
	}

	public void tellEveryOneRooms() {
		ResultSet rset = sqlConn.selectRoom();
		if (rset != null) {
			RoomResult roomSet = new RoomResult().create(rset);
			Iterator<PlayerSocketWriter> itPlayerSocketWriter = playerSocketWriters.iterator();
			while (itPlayerSocketWriter.hasNext()) {
				try {
					ObjectOutputStream writer = itPlayerSocketWriter.next().getWriter();
					writer.writeObject(roomSet);
					writer.flush();
				} catch (Exception ev) {
					ev.printStackTrace();
				}
			}
		}
	}

	public void removePlayerSocketWriter(String nowPlayer) {
		Iterator<PlayerSocketWriter> itSocket = playerSocketWriters.iterator();
		while (itSocket.hasNext()) {
			String player = itSocket.next().getPlayer();
			if (player.equals(nowPlayer)) {
				itSocket.remove();
				break;
			}
		}

	}

	public PlayerSocketWriter getMyPSW(String player) {
		Iterator<PlayerSocketWriter> itSocket = playerSocketWriters.iterator();
		while (itSocket.hasNext()) {
			PlayerSocketWriter nowPSW = (PlayerSocketWriter) itSocket.next();
			if (player.equals(nowPSW.getPlayer()))
				return nowPSW;
		}
		return null;
	}

	public class Mytask implements Runnable {
		private Socket socket;
		private ObjectOutputStream objWriter;
		private ObjectInputStream objReader;
		private String nowPlayer;
		private PlayerSocketWriter myPSW;
		private JTextArea jtextarea;

		public Mytask(Socket socket,JTextArea jtextarea) {
			try {
				this.jtextarea=jtextarea;
				this.socket = socket;
				objWriter = new ObjectOutputStream(socket.getOutputStream());
				objReader = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
			} catch (Exception ev) {
				ev.printStackTrace();
			}
		}

		public void run() {
			Object obj;
			try {
				while ((obj = objReader.readObject()) != null) {
					String className = obj.getClass().getName();
					// 收到登录注册信息
					if (className.equals("GoNet.InfoRegisterLogin")) {
						InfoRegisterLogin infoRegisterLogin = (InfoRegisterLogin) obj;
						if (infoRegisterLogin.getOperate().equals("Login")) {
							String player_name = infoRegisterLogin.getName();
							String player_pwd = infoRegisterLogin.getPassword();
							jtextarea.append("登录信息：" + player_name+"\n");//
							String backStr = sqlConn.selectNowPlayer(player_name, player_pwd);
							jtextarea.append("登录结果：" + backStr+"\n");//
							InfoMessage backMessage = new InfoMessage(backStr);
							objWriter.writeObject(backMessage);
							objWriter.flush();
							if (backStr.equals("你已登录"))
								break;
						}
						if (infoRegisterLogin.getOperate().equals("Register")) {
							String player_name = infoRegisterLogin.getName();
							String player_pwd = infoRegisterLogin.getPassword();
							jtextarea.append("注册信息：" + player_name + "\n" );//
							String backStr = sqlConn.insertPlayer(player_name, player_pwd);
							jtextarea.append("注册结果：" + backStr+"\n");//
							InfoMessage backMessage = new InfoMessage(backStr);
							objWriter.writeObject(backMessage);
							objWriter.flush();
						}

					}
					// 收到创建房间和挑战信息
					if (className.equals("GoNet.InfoCreateChallengeRoom")) {
						InfoCreateChallengeRoom infoRoom = (InfoCreateChallengeRoom) obj;
						String player1 = infoRoom.getPlayer1();
						String player2 = infoRoom.getPlayer2();
						jtextarea.append("创建房间信息：" + player1 + "," + player2+"\n");
						String backStr = sqlConn.insertRoom(player1, player2);
						InfoMessage backMessage = new InfoMessage(backStr);
						objWriter.writeObject(backMessage);
						objWriter.flush();
						if (backStr.endsWith("成功")) {
							tellEveryOneRooms();
							tellEveryOne();
							tellMyOnlineFriend(nowPlayer);
							if (player2 != null) {
								tellMyOnlineFriend(player2);
								PlayerSocketWriter coPSW = getMyPSW(player2);
								ObjectOutputStream coObjWriter = coPSW.getWriter();
								myPSW.setCompetitor(player2);
								myPSW.setCompetitorObjWriter(coObjWriter);
								coPSW.setCompetitor(player1);
								coPSW.setCompetitorObjWriter(objWriter);
								InfoPlayerCompetitor infoPlayerCompetitor;
								int random = (new Random().nextInt(10));
								if (random >= 5) {
									infoPlayerCompetitor = new InfoPlayerCompetitor(player1, player2);
								} else {
									infoPlayerCompetitor = new InfoPlayerCompetitor(player2, player1);
								}
								objWriter.writeObject(infoPlayerCompetitor);
								objWriter.flush();
								coObjWriter.writeObject(infoPlayerCompetitor);
								coObjWriter.flush();
							}
						}
					}
					// 收到删除房间信息
					if (className.equals("GoNet.InfoDeleteRoom")) {
						InfoDeleteRoom infoDeleteRoom = (InfoDeleteRoom) obj;
						String player1 = infoDeleteRoom.getPlayer1();
						String player2 = infoDeleteRoom.getPlayer2();
						jtextarea.append("删除房间信息：" + player1 + "," + player2+"\n");
						String backStr = sqlConn.deleteRoom(player1, player2);
						if (backStr.endsWith("成功")) {
							tellEveryOneRooms();
							tellEveryOne();
							tellMyOnlineFriend(nowPlayer);
							if (player2 != null)
								tellMyOnlineFriend(player2);
						}
						InfoMessage backMessage = new InfoMessage(backStr);
						objWriter.writeObject(backMessage);
						objWriter.flush();
					}
					// 收到增删好友信息
					if (className.equals("GoNet.InfoAddDeleteFriend")) {
						InfoAddDeleteFriend infoAddDeleteFriend = (InfoAddDeleteFriend) obj;
						if (infoAddDeleteFriend.getOperate().equals("Apply")) {
							String friend_name = infoAddDeleteFriend.getFriend();
							sendApply(friend_name, infoAddDeleteFriend);
						}
						if (infoAddDeleteFriend.getOperate().equals("Agree")
								|| infoAddDeleteFriend.getOperate().equals("Refuse")) {
							String player_name = infoAddDeleteFriend.getPlayer();
							sendApply(player_name, infoAddDeleteFriend);
						}
						if (infoAddDeleteFriend.getOperate().equals("AddFriend")) {
							String friend_name = infoAddDeleteFriend.getFriend();
							String player_name = infoAddDeleteFriend.getPlayer();
							jtextarea.append("添加好友信息(好友|玩家)：" + friend_name + "|" + player_name+"\n");//
							String backStr = sqlConn.insertFriends(friend_name, player_name);
							jtextarea.append("添加好友结果：" + backStr+"\n");//
							if (backStr.endsWith("成功")) {
								ResultSet rset = sqlConn.selectFriends(nowPlayer);
								MyResultSet myResultSet = new MyResultSet().create("Friends", rset);
								objWriter.writeObject(myResultSet);
								objWriter.flush();
								tellFriend(friend_name);
							}
						}
						if (infoAddDeleteFriend.getOperate().equals("DeleteFriend")) {
							String friend_name = infoAddDeleteFriend.getFriend();
							String player_name = infoAddDeleteFriend.getPlayer();
							jtextarea.append("删除好友信息(好友|玩家)：" + friend_name + "|" + player_name+"\n");//
							String backStr = sqlConn.deleteFriends(friend_name, player_name);
							jtextarea.append("删除好友结果：" + backStr+"\n");//
							InfoMessage backMessage = new InfoMessage(backStr);
							objWriter.writeObject(backMessage);
							objWriter.flush();
							if (backStr.endsWith("成功")) {
								ResultSet rset = sqlConn.selectFriends(nowPlayer);
								MyResultSet myResultSet = new MyResultSet().create("Friends", rset);
								objWriter.writeObject(myResultSet);
								objWriter.flush();
								tellFriend(friend_name);
							}
						}
					}
					// 收到对战邀请
					if (className.equals("GoNet.InfoInviteDuiyi")) {
						InfoInviteDuiyi infoInviteDuiyi = (InfoInviteDuiyi) obj;
						if (infoInviteDuiyi.getOperate().equals("Invite")) {
							String player2 = infoInviteDuiyi.getPlayer2();
							sendInvite(player2, infoInviteDuiyi);
							jtextarea.append("邀请对战信息(发起者|被邀者)："+nowPlayer+"|"+player2+"\n");
						} else {
							String player1 = infoInviteDuiyi.getPlayer1();
							sendInvite(player1, infoInviteDuiyi);
						}
					}
					// 收到聊天信息
					if (className.equals("GoNet.InfoChatMessage")) {
						InfoChatMessage infoChatMessage = (InfoChatMessage) obj;
						try {
							ObjectOutputStream coWriter = myPSW.getCompetitorObjWriter();
							String sendMessage = infoChatMessage.getChatMessage();
							sendMessage = "对手（" + nowPlayer + "）：" + sendMessage + "\n";
							coWriter.writeObject(new InfoChatMessage(sendMessage));
							coWriter.flush();
						} catch (Exception ev) {
							// 對手離綫
						}
					}
					// 收到棋子位子
					if (className.equals("GoNet.InfoQiziPos")) {
						try {
							InfoQiziPos infoQiziPos = (InfoQiziPos) obj;
							ObjectOutputStream coWriter = myPSW.getCompetitorObjWriter();
							coWriter.writeObject(infoQiziPos);
							coWriter.flush();
						} catch (Exception ev) {
							// 對手離綫
						}
					}
					// 收到对弈指令
					if (className.equals("GoNet.InfoDuiyiCommand")) {
						InfoDuiyiCommand infoDuiyiCommand = (InfoDuiyiCommand) obj;
						String command = infoDuiyiCommand.getCommand();
						String type = infoDuiyiCommand.getCommandType();
						ObjectOutputStream coWriter = myPSW.getCompetitorObjWriter();
						String competitor = myPSW.getCompetitor();
						if (type.equals("申请") || type.equals("同意") || type.equals("拒绝")) {
							coWriter.writeObject(infoDuiyiCommand);
							coWriter.flush();
						} else {
							if (command.equals("过子")) {
								coWriter.writeObject(infoDuiyiCommand);
								coWriter.flush();
							} else if (command.equals("认输")) {
								sqlConn.AlterInfo(1, "player_lose", nowPlayer);
								sqlConn.AlterInfo(1, "player_win", competitor);
								coWriter.writeObject(infoDuiyiCommand);
								coWriter.flush();
								sqlConn.deleteRoom(nowPlayer, competitor);
								tellEveryOneRooms();
								tellEveryOne();
								tellMyOnlineFriend(nowPlayer);
								tellMyOnlineFriend(competitor);
								myPSW.setCompetitor(null);
								myPSW.setCompetitorObjWriter(null);
								PlayerSocketWriter coPSW=getMyPSW(competitor);
								coPSW.setCompetitor(null);
								coPSW.setCompetitorObjWriter(null);
								
							} else if (command.equals("退出")) {
								sqlConn.AlterInfo(1, "player_lose", nowPlayer);
								sqlConn.AlterInfo(1, "player_win", competitor);
								coWriter.writeObject(infoDuiyiCommand);
								coWriter.flush();
								sqlConn.deleteRoom(nowPlayer, competitor);
								tellEveryOneRooms();
								tellEveryOne();
								tellMyOnlineFriend(nowPlayer);
								tellMyOnlineFriend(competitor);
								myPSW.setCompetitor(null);
								myPSW.setCompetitorObjWriter(null);
								PlayerSocketWriter coPSW=getMyPSW(competitor);
								coPSW.setCompetitor(null);
								coPSW.setCompetitorObjWriter(null);
							}
						}
					}
					// 收到登录成功确认信息
					if (className.equals("GoNet.InfoMyInfo")) {
						InfoMyInfo infoMyInfo = (InfoMyInfo) obj;
						if (infoMyInfo.getOperate().equals("LoginSuccess")) {
							nowPlayer = infoMyInfo.getMyName();
							myPSW = new PlayerSocketWriter(nowPlayer, objWriter);
							playerSocketWriters.add(myPSW);
							sqlConn.AlterInfo(0, "player_state", infoMyInfo.getMyName());
							ResultSet rset = sqlConn.selectFriends(infoMyInfo.getMyName());
							if (rset != null) {
								MyResultSet myResultSet = new MyResultSet().create("Friends", rset);
								objWriter.writeObject(myResultSet);
								objWriter.flush();
								tellEveryOne();
								tellEveryOneRooms();
							}
							tellMyOnlineFriend(nowPlayer);
						}
					}
					//收到胜负信息
					if(className.equals("GoNet.InfoWinLose")){
						InfoWinLose infoWinLose=(InfoWinLose)obj;
						String winer=infoWinLose.getWiner();
						String loser=infoWinLose.getLoser();
						sqlConn.AlterInfo(1, "player_lose", loser);
						sqlConn.AlterInfo(1, "player_win", winer);
						sqlConn.deleteRoom(winer, loser);
						tellEveryOneRooms();
						tellEveryOne();
						tellMyOnlineFriend(winer);
						tellMyOnlineFriend(loser);
						myPSW.setCompetitor(null);
						myPSW.setCompetitorObjWriter(null);
						PlayerSocketWriter coPSW=getMyPSW(nowPlayer.equals(winer)?loser:winer);
						coPSW.setCompetitor(null);
						coPSW.setCompetitorObjWriter(null);
					}
				}
			} catch (Exception ev) {
				if (nowPlayer != null) {

					jtextarea.append(nowPlayer + "离线\n");
					if (myPSW.getCompetitor() != null)
						tellMyCompetitor(myPSW);
					sqlConn.AlterInfo(2, "player_state", nowPlayer);
					removePlayerSocketWriter(nowPlayer);
					tellEveryOne();
					tellMyOnlineFriend(nowPlayer);
					sqlConn.deleteRoom(nowPlayer, myPSW.getCompetitor());
					tellEveryOneRooms();
				}
			} finally {
				try {
					objWriter.close();
					objReader.close();
					socket.close();
				} catch (Exception ev) {

				}
			}
		}
	}

	public class PlayerSocketWriter {
		private String player, competitor;
		private ObjectOutputStream objWriter, competitorObjWriter;

		public PlayerSocketWriter(String player, ObjectOutputStream objWriter) {
			this.player = player;
			this.objWriter = objWriter;
		}

		public String getPlayer() {
			return this.player;
		}

		public ObjectOutputStream getWriter() {
			return this.objWriter;
		}

		public void setCompetitor(String competitor) {
			this.competitor = competitor;
		}

		public void setCompetitorObjWriter(ObjectOutputStream competitorObjWriter) {
			this.competitorObjWriter = competitorObjWriter;
		}

		public String getCompetitor() {
			return this.competitor;
		}

		public ObjectOutputStream getCompetitorObjWriter() {
			return this.competitorObjWriter;
		}
	}

	public static void main(String[] args) {
		JFrame jframe=new JFrame("服务器");
		jframe.setBounds(100,100,400,600);
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JTextArea jtextArea=new JTextArea();
		jtextArea.setEditable(false);
		DefaultCaret caret=(DefaultCaret)jtextArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		jframe.add(jtextArea);
		jframe.setVisible(true);
		new MainClass(jtextArea);
	}
}
