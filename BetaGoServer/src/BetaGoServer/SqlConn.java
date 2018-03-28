package BetaGoServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class SqlConn {
	private String dbUrl;
	private String dbUser;
	private String dbPwd;
	private java.sql.Statement statement,statement1,statement2;
	private ResultSet rset;

	public SqlConn() {
		this.dbUrl = "jdbc:sqlserver://localhost:1433;DatabaseName=BetaGo";
		this.dbUser = "sa";
		this.dbPwd = "123927";
		try {
			Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
			statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement1=conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement2=conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			} catch (Exception ev) {
			ev.printStackTrace();
		}
	}

	// 执行注册的数据插入
	public String insertPlayer(String name, String pwd) {
		String selectPlayerIdSqlStr = "select player_id from tb_player where player_name='" + name + "'";
		String insertSqlPlayerStr = "insert tb_player(player_name,player_pwd,player_level,player_win,player_lose,player_state)values('"
				+ name + "','" + pwd + "',0,0,0,2)";
		try {
			ResultSet rsetTemp = statement.executeQuery(selectPlayerIdSqlStr);
			int count = 0;
			while (rsetTemp.next())
				count++;
			if (count == 0) {
				statement.executeUpdate(insertSqlPlayerStr);
				creatFriendTable(name);// 调用函数创建玩家好友表和视图
				return "注册成功";
			} else
				return "玩家已存在";
		} catch (Exception ev) {
			ev.printStackTrace();
			return "注册失败";
		}
	}

	// 执行添加为好友的数据插入
	public String insertFriends(String friend_name, String player_name) {
		if (friend_name.equals(player_name))
			return "不能添加自己为好友";
		else {
			String selectPlayerIdSqlStr = "select player_id from tb_player where player_name='" + player_name + "'";
			String selectFriendIdSqlStr = "select player_id from tb_player where player_name='" + friend_name + "'";
			String player_id = null;
			String friend_id = null;
			try {
				ResultSet rsetTemp = statement.executeQuery(selectPlayerIdSqlStr);
				int count = 0;
				while (rsetTemp.next()) {
					player_id = rsetTemp.getString(1);
					count++;
				}
				if (count == 0)
					return "玩家不存在";
				else {
					ResultSet rsetTemp1 = statement.executeQuery(selectFriendIdSqlStr);
					int count1 = 0;
					while (rsetTemp1.next()) {
						friend_id = rsetTemp1.getString(1);
						count1++;
					}
					if (count1 == 0)
						return "好友不存在";
					else {
						String selectIsFriend = "select player_name from tb_player" + player_id + " where player_name='"
								+ friend_name + "'";
						ResultSet rsetTemp2 = statement.executeQuery(selectIsFriend);
						int count2 = 0;
						while (rsetTemp2.next())
							count2++;
						if (count2 == 0) {
							String insertSqlFriendStr = "insert tb_player" + player_id + " values('" + friend_name
									+ "')";
							statement.executeUpdate(insertSqlFriendStr);
							String insertSqlFriendStr1 = "insert tb_player" + friend_id + " values('" + player_name
									+ "')";
							statement.executeUpdate(insertSqlFriendStr1);
							return "成功";
						} else
							return "已经是好友";
					}
				}
			} catch (Exception ev) {
				ev.printStackTrace();
				return "添加失败";
			}
		}
	}

	// 查询玩家表数据
	public ResultSet selectPlayer() {
		String selectsqlstr = "select player_name,player_level,player_win,player_lose,player_state from tb_player where player_state<>"
				+ 2;
		try {
			rset = statement.executeQuery(selectsqlstr);
			return rset;
		} catch (Exception ev) {
			ev.printStackTrace();
			return null;
		}
	}

	// 查询当前玩家的好友,从玩家好友视图查询
	public ResultSet selectFriends(String player_name) {
		String selectPlayerIdSqlStr = "select player_id from tb_player where player_name='" + player_name + "'";
		String player_id = null;
		try {
			ResultSet rsetTemp = statement1.executeQuery(selectPlayerIdSqlStr);
			while (rsetTemp.next())
				player_id = rsetTemp.getString(1);
			String selectFriendsSqlStr = "select * from view_" + player_id;
			rset = statement1.executeQuery(selectFriendsSqlStr);
			return rset;
		} catch (Exception ev) {
			ev.printStackTrace();
			return null;
		}
	}

	// 查询当前玩家的在线好友
	public ResultSet selectOnlineFriends(String player_name) {
		String selectPlayerIdSqlStr = "select player_id from tb_player where player_name='" + player_name + "'";
		String player_id = null;
		try {
			ResultSet rsetTemp = statement.executeQuery(selectPlayerIdSqlStr);
			while (rsetTemp.next())
				player_id = rsetTemp.getString(1);
			String selectFriendsSqlStr = "select * from view_" + player_id + " where player_state<>2";
			rset = statement.executeQuery(selectFriendsSqlStr);
			return rset;
		} catch (Exception ev) {
			ev.printStackTrace();
			return null;
		}
	}

	// 查询玩家用户名和密码，用于登录检测
	public String selectNowPlayer(String player_name, String player_pwd) {
		String selectNameSqlStr = "select player_pwd,player_state from tb_player where player_name='" + player_name
				+ "'";
		try {
			ResultSet rsetN = statement.executeQuery(selectNameSqlStr);
			if (rsetN.next()) {
				if (rsetN.getString(1).equals(player_pwd)) {
					int playerState = Integer.parseInt(rsetN.getString(2));
					if (playerState != 2)
						return "你已登录";
					else
						return "登录成功";
				} else
					return "用户名或密码错误";
			} else {
				return "未注册";
			}
		} catch (Exception ev) {
			ev.printStackTrace();
			return "登录失败";
		}
	}

	// 建立玩家好友表，同时建立其视图
	public String creatFriendTable(String player_name) {
		String selectPlayerIdSqlStr = "select player_id from tb_player where player_name='" + player_name + "'";
		String player_id = null;
		try {
			ResultSet rsetTemp = statement.executeQuery(selectPlayerIdSqlStr);
			while (rsetTemp.next())
				player_id = rsetTemp.getString(1);
			String creatTableSqlStr = "create table tb_player" + player_id
					+ "(player_name varchar(20),primary key(player_name),foreign key (player_name) references tb_player(player_name) on delete cascade)";
			String creatVIewSqlStr = "create view view_" + player_id
					+ " as select player_name,player_level,player_win,player_lose,player_state from tb_player where player_name in(select player_name from tb_player"
					+ player_id + ")";
			statement.executeUpdate(creatTableSqlStr);
			statement.executeUpdate(creatVIewSqlStr);
			return "创建成功";
		} catch (Exception ev) {
			ev.printStackTrace();
			return "创建失败";
		}
	}

	// 删除好友的数据处理
	public String deleteFriends(String friend_name, String player_name) {
		String selectPlayerIdSqlStr = "select player_id from tb_player where player_name='" + player_name + "'";
		String selectFriendIdSqlStr = "select player_id from tb_player where player_name='" + friend_name + "'";
		String player_id = null;
		String friend_id = null;
		try {
			ResultSet rsetTemp = statement.executeQuery(selectPlayerIdSqlStr);
			int count = 0;
			while (rsetTemp.next()) {
				player_id = rsetTemp.getString(1);
				count++;
			}
			if (count == 0)
				return "玩家不存在";
			else {
				String deleteFriendsSqlStr = "delete from tb_player" + player_id + " where player_name='" + friend_name
						+ "'";
				statement.executeUpdate(deleteFriendsSqlStr);
			}
			ResultSet rsetTemp1 = statement.executeQuery(selectFriendIdSqlStr);
			int count1 = 0;
			while (rsetTemp1.next()) {
				friend_id = rsetTemp1.getString(1);
				count1++;
			}
			if (count1 == 0)
				return "好友不存在";
			else {
				String deleteFriendsSqlStr1 = "delete from tb_player" + friend_id + " where player_name='" + player_name
						+ "'";
				statement.executeUpdate(deleteFriendsSqlStr1);
			}
			return "删除成功";
		} catch (Exception ev) {
			ev.printStackTrace();
			return "删除失败";
		}
	}

	// 删除玩家
	public String deletePlayer(String player_name) {
		String selectPlayerIdSqlStr = "select player_id from tb_player where player_name='" + player_name + "'";
		String player_id = null;
		try {
			ResultSet rsetTemp = statement.executeQuery(selectPlayerIdSqlStr);
			int count = 0;
			while (rsetTemp.next()) {
				player_id = rsetTemp.getString(1);
				count++;
			}
			if (count == 0)
				return "玩家不存在";
			else {
				String deleteTableSqlStr = "drop table tb_player" + player_id;
				String deleteViewSqlSTr = "drop view view_" + player_id;
				String deletePlayerSqlStr = "delete from tb_player where player_id='" + player_id + "'";
				statement.execute(deleteTableSqlStr);
				statement.execute(deleteViewSqlSTr);
				statement.execute(deletePlayerSqlStr);
				return "删除成功";
			}
		} catch (Exception ev) {
			ev.printStackTrace();
			return "删除失败";
		}
	}

	// 修改用户信息的数据处理
	public String AlterInfo(int value, String column_name, String player_name) {
		String selectPlayerIdSqlStr = "select player_id from tb_player where player_name='" + player_name + "'";
		String alterSqlStr;
		if (column_name.equals("player_win") || column_name.equals("player_lose"))
			alterSqlStr = "update tb_player set " + column_name + "+=" + value + " where player_name='" + player_name
					+ "'";
		else
			alterSqlStr = "update tb_player set " + column_name + "=" + value + " where player_name='" + player_name
					+ "'";
		try {
			ResultSet rsetTemp = statement.executeQuery(selectPlayerIdSqlStr);
			int count = 0;
			while (rsetTemp.next())
				count++;
			if (count == 0)
				return "玩家不存在";
			else {
				statement.executeUpdate(alterSqlStr);
				return "修改成功";
			}
		} catch (Exception ev) {
			return "修改失败";
		}
	}

	// 设置所有玩家离线，服务器开启时执行
	public void setAllOffline() {
		String updateAllOfflineSqlStr = "update tb_player set player_state=2";
		try {
			statement.executeUpdate(updateAllOfflineSqlStr);
		} catch (Exception ev) {

		}
	}

	// 设置清空所有房间，服务器开启时执行
	public void deleteAllRoom() {
		String deleteAllRoomSqlStr = "delete from tb_Room";
		try {
			statement.executeUpdate(deleteAllRoomSqlStr);
		} catch (Exception ev) {

		}
	}

	// 查询当前所有房间
	public ResultSet selectRoom() {
		String selectRoom = "select * from tb_Room";
		try {
			ResultSet rset = statement.executeQuery(selectRoom);
			return rset;
		} catch (Exception ev) {
			return null;
		}
	}

	// 清除房间
	public String deleteRoom(String player1, String player2) {
		String selectPlayer1State = "select player_state from tb_player where player_name='" + player1 + "'";
		String selectPlayer2State = "select player_state from tb_player where player_name='" + player2 + "'";
		String deleteRoomOnPlayer1 = "delete from tb_Room where player1_name='" + player1 + "' or player1_name='"
				+ player2 + "'";
		try {
			if (player2 == null) {
				ResultSet rset1 = statement2.executeQuery(selectPlayer1State);
				rset1.next();
				if (!rset1.getString(1).equals("2"))
					AlterInfo(0, "player_state", player1);
				statement2.executeUpdate(deleteRoomOnPlayer1);
				return "撤销成功";
			} else {
				ResultSet rset1 = statement.executeQuery(selectPlayer1State);
				rset1.next();
				if (!rset1.getString(1).equals("2"))
					AlterInfo(0, "player_state", player1);
				ResultSet rset2 = statement.executeQuery(selectPlayer2State);
				rset2.next();
				if (!rset2.getString(1).equals("2"))
					AlterInfo(0, "player_state", player2);
				statement2.executeUpdate(deleteRoomOnPlayer1);
				return "删除成功";
			}
		} catch (Exception ev) {
			return "操作失败";
		}
	}

	// 创建房间
	public String insertRoom(String player1, String player2) {
		if (player2 == null) {
			String insertRoomSqlStr = "insert into tb_Room values('" + player1 + "',null)";
			try {
				statement.executeUpdate(insertRoomSqlStr);
				AlterInfo(1, "player_state", player1);
				return "创建成功";
			} catch (Exception ev) {
				return "创建失败";
			}
		} else {
			String selectPlayer1SqlStr = "select player1_name from tb_Room where player1_name='" + player1
					+ "' or player1_name='" + player2 + "'";
			try {
				ResultSet rset = statement.executeQuery(selectPlayer1SqlStr);
				int count = 0;
				while (rset.next())
					count++;
				if (count == 0) {
					try {
						String inviteSqlStr = "insert into tb_Room values('" + player1 + "','" + player2 + "')";
						statement.executeUpdate(inviteSqlStr);
						AlterInfo(3, "player_state", player1);
						AlterInfo(3, "player_state", player2);
						return "邀请成功";
					} catch (Exception ev) {
						return "邀请失败";
					}
				} else {
					try {
						String receiveChallengeSqlStr = "update tb_Room set player2_name='" + player1
								+ "' where player1_name='" + player2 + "'";
						statement.executeUpdate(receiveChallengeSqlStr);
						AlterInfo(3, "player_state", player1);
						AlterInfo(3, "player_state", player2);
						return "加入成功";
					} catch (Exception ev) {
						return "加入失败";
					}

				}
			} catch (Exception ev) {
				return "操作失败";
			}
		}
	}
}
