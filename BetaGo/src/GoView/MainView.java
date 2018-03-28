package GoView;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.Toolkit;
import java.awt.*;
import java.awt.event.*;;

public class MainView extends JFrame implements ActionListener {
	private int window_width = Toolkit.getDefaultToolkit().getScreenSize().width;
	private int window_height = Toolkit.getDefaultToolkit().getScreenSize().height;
	private JTabbedPane view_pane;
	private Dating dating;

	private LogDialog log_dialog;
	private Image icon_image;

	public MainView() {
		super("BetaGo");
		this.setBounds(window_width * 1 / 20, window_height * 1 / 20, window_width, window_height * 9 / 10);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setExtendedState(this.MAXIMIZED_BOTH);
		// 设置程序图标
		try {
			icon_image = ImageIO.read(this.getClass().getResource("/image/icon.jpg"));
		} catch (Exception e) {
		}
		this.setIconImage(icon_image);

		Container content = this.getContentPane();
		view_pane = new JTabbedPane();
		dating = new Dating().getInstance();
		view_pane.addTab("大厅", dating);
		content.add(view_pane);
		log_dialog = new LogDialog(this);
		this.setVisible(true);
		dating.split_pane.setDividerLocation(0.76);
		log_dialog.show();
	}

	public void actionPerformed(ActionEvent ev) {
		return;
	}

	public static void main(String[] args) {
		new MainView();
	}
}
