package GoView;

import javax.imageio.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import GoNet.SocketConn;

public class Room extends JPanel {
	private String player1_name, player2_name;
	private Image image_resource1, image_resource2;
	private int state;
	private JPopupMenu menu = new JPopupMenu();
	private JMenuItem jitem;

	// 一个参数情况表示房间处于被挑战状态
	public Room(String name1) {
		final Container container = this;
		this.setPreferredSize(new Dimension(120, 120));
		state = 0;
		player1_name = name1;
		player2_name = null;
		jitem = new JMenuItem("立即挑战");
		jitem.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					MyInfo myInfo = new MyInfo().getInstance();
					SocketConn socketConn = new SocketConn().getInstance();
					socketConn.CreateChallengeRoom(myInfo.getMyNmae(), player1_name);
				}
			}
		});
		menu.add(jitem);
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					int x = e.getX(), y = e.getY();
					if (x >= 10 && x <= 110 && (((x - 60) * (x - 60)) + ((y - 60) * (y - 60))) <= 2500)
						menu.show(container, e.getX(), e.getY());
				}
			}
		});
	}

	public Room(String name1, String name2) {
		final Container container = this;
		this.setPreferredSize(new Dimension(120, 120));
		state = 1;
		player1_name = name1;
		player2_name = name2;
		jitem = new JMenuItem("正在对弈 ");
		jitem.setEnabled(false);
		menu.add(jitem);
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					int x = e.getX(), y = e.getY();
					if (x >= 10 && x <= 110 && (((x - 60) * (x - 60)) + ((y - 60) * (y - 60))) <= 2500)
						menu.show(container, e.getX(), e.getY());
				}
			}
		});
	}

	public void paint(Graphics g) {
		getResource();
		Graphics2D graphics = (Graphics2D) g;
		Font font = new Font("宋体", 14, 15);
		graphics.setFont(font);

		switch (state) {
		case 0:
			graphics.drawImage(image_resource1, 10, 10, 100, 100, this);
			graphics.setColor(Color.blue);
			graphics.drawString(player1_name,
					(this.getWidth() - graphics.getFontMetrics().stringWidth(player1_name)) / 2, 30);
			graphics.drawString("VS", (this.getWidth() - graphics.getFontMetrics().stringWidth("VS")) / 2, 60);
			graphics.drawString("等待挑战", (this.getWidth() - graphics.getFontMetrics().stringWidth("等待挑战")) / 2, 90);

			break;
		case 1:
			graphics.drawImage(image_resource2, 10, 10, 100, 100, this);
			graphics.setColor(Color.red);
			graphics.drawString(player1_name,
					(this.getWidth() - graphics.getFontMetrics().stringWidth(player1_name)) / 2, 30);
			graphics.drawString("VS", (this.getWidth() - graphics.getFontMetrics().stringWidth("VS")) / 2, 60);
			graphics.drawString(player2_name,
					(this.getWidth() - graphics.getFontMetrics().stringWidth(player2_name)) / 2, 90);
			break;
		default:
			break;
		}
	}

	public String getPlayer1Name() {
		return this.player1_name;
	}

	public String getPlayer2Name() {
		return this.player2_name;
	}

	public void getResource() {
		try {
			image_resource1 = ImageIO.read(this.getClass().getResource("/image/room1.png"));
			image_resource2 = ImageIO.read(this.getClass().getResource("/image/room2.png"));
		} catch (Exception e) {
		}
	}
}
