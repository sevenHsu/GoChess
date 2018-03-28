package GoView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import GoNet.SocketConn;

public class OnlinePlayerPane extends JPanel {
	public JTable online_table;
	public DefaultTableModel table_model;
	private JPopupMenu jmenu = new JPopupMenu();
	private JMenuItem item1, item2;
	private int nowRow;

	public OnlinePlayerPane() {
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
		item2 = new JMenuItem("加为好友");
		// 此处添加加为好友的事件
		item2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent ev) {
				if (ev.getButton() == MouseEvent.BUTTON1) {
					if (item2.isEnabled()) {
						String friendName = (String) table_model.getValueAt(nowRow, 0);
						MyInfo myInfo = new MyInfo().getInstance();
						String playerName = myInfo.getMyNmae();
						SocketConn socketConn = new SocketConn().getInstance();
						socketConn.AddDeleteFriend(friendName, playerName, "Apply");
					} else
						return;
				}
			}
		});
		jmenu.add(item1);
		jmenu.add(item2);
		final Object[] columns = { "棋手", "等级", "胜局", "负局", "状态" };
		Object[][] rowData = {};
		this.setLayout(new BorderLayout());
		table_model = new DefaultTableModel(rowData, columns);
		online_table = new JTable(table_model);
		online_table.doLayout();
		online_table.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent ev) {
				if (ev.getButton() == MouseEvent.BUTTON3) {
					MyInfo myInfo = new MyInfo().getInstance();
					String playerName = myInfo.getMyNmae();
					nowRow = online_table.rowAtPoint(ev.getPoint());
					online_table.setRowSelectionInterval(nowRow, nowRow);
					if (!((String) table_model.getValueAt(nowRow, 4)).equals("空闲")
							|| ((String) table_model.getValueAt(nowRow, 0)).equals(playerName))
						item1.setEnabled(false);
					else
						item1.setEnabled(true);
					if (((String) table_model.getValueAt(nowRow, 0)).equals(playerName))
						item2.setEnabled(false);
					else
						item2.setEnabled(true);
					jmenu.show(container, ev.getX(), ev.getY());
				}
			}
		});
		JScrollPane jscrollpane = new JScrollPane(this.online_table);
		this.add(jscrollpane);
	}

	public void SetSelfForeground() {
		int i;
		String myName = (new MyInfo().getInstance().getMyNmae());
		for (i = 0; i < online_table.getRowCount(); i++) {
			if (online_table.getValueAt(i, 0).equals(myName))
				break;
		}
		online_table.setDefaultRenderer(Object.class, new EvenOddRenderer(i));
	}

	public void removeAllRow() {
		int rowCount = this.table_model.getRowCount();
		// System.out.println(rowCount);
		for (int i = rowCount - 1; i >= 0; i--)
			this.table_model.removeRow(i);
	}

	public String getPlayerValue(String player) {
		for (int i = 0; i < this.table_model.getRowCount(); i++)
			if (((String) this.table_model.getValueAt(i, 0)).equals(player))
				return (String) this.table_model.getValueAt(i, 1);
		return null;
	}

}

// 添加棋手数据到表格，写在大厅中
// online_player_pane.table_model.addRow(new Player("样还",1,12,3,0).get());

class EvenOddRenderer implements TableCellRenderer {
	private int row1;

	public static final DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();

	public EvenOddRenderer(int row1) {
		this.row1 = row1;
		// getTableCellRendererComponent();
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
				column);
		Color foreground, background;

		if (row == row1 && (column == 0 || column == 1 || column == 2 || column == 3 || column == 4)) {
			foreground = Color.red;
		} else {
			foreground = Color.BLACK;
		}
		renderer.setForeground(foreground);
		return renderer;
	}
}