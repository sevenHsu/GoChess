package GoView;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import GoNet.SocketConn;

public class MyFriendsPane extends JPanel {
	public JTable friends_table;
	public DefaultTableModel table_model;
	private JPopupMenu jmenu = new JPopupMenu();
	private JMenuItem item1, item2;
	private int nowRow;

	public MyFriendsPane() {
		final Container container = this;
		item1 = new JMenuItem("邀请对战");
		// 此处添加邀请对战的事件
		item1.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent ev) {
				if (ev.getButton() == MouseEvent.BUTTON1) {
					if (item1.isEnabled()) {
						String competitor = (String) table_model.getValueAt(nowRow, 0);
						SocketConn socketConn = new SocketConn().getInstance();
						MyInfo myInfo = new MyInfo().getInstance();
						socketConn.sendInviteDuiyi(myInfo.getMyNmae(), competitor, "Invite");
					}
				}
			}
		});
		item2 = new JMenuItem("删除好友");
		// 此处添加删除好友事件
		item2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent ev) {
				if (ev.getButton() == MouseEvent.BUTTON1) {
					String friendName = (String) table_model.getValueAt(nowRow, 0);
					MyInfo myInfo = new MyInfo().getInstance();
					String playerName = myInfo.getMyNmae();
					SocketConn socketConn = new SocketConn().getInstance();
					socketConn.AddDeleteFriend(friendName, playerName, "DeleteFriend");
				}
			}
		});
		jmenu.add(item1);
		jmenu.add(item2);
		final Object[] columns = { "棋手", "等级", "胜局", "负局", "状态" };
		Object[][] rowData = {};
		this.setLayout(new BorderLayout());
		table_model = new DefaultTableModel(rowData, columns);
		friends_table = new JTable(table_model);
		friends_table.doLayout();
		friends_table.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent ev) {
				if (ev.getButton() == MouseEvent.BUTTON3) {
					nowRow = friends_table.rowAtPoint(ev.getPoint());
					friends_table.setRowSelectionInterval(nowRow, nowRow);
					if (!((String) table_model.getValueAt(nowRow, 4)).equals("空闲"))
						item1.setEnabled(false);
					else
						item1.setEnabled(true);
					jmenu.show(container, ev.getX(), ev.getY());
				}
			}
		});
		JScrollPane jscrollpane = new JScrollPane(this.friends_table);
		this.add(jscrollpane);
	}

	public void removeAllRow() {
		int rowCount = this.table_model.getRowCount();
		for (int i = rowCount - 1; i >= 0; i--)
			this.table_model.removeRow(i);
	}

	public boolean isFriend(String friend) {
		int rowCount = this.table_model.getRowCount();
		boolean find = false;
		for (int i = 0; i < rowCount; i++)
			if (table_model.getValueAt(i, 0).equals(friend)) {
				find = true;
				break;
			}
		return find;
	}

}