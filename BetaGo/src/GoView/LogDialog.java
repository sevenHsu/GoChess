package GoView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LogDialog extends JDialog {
	public LoginPane loginpane;
	public RegisterPane registerpane;
	public JFrame frame;

	public LogDialog(JFrame frame) {
		super(frame, "连接服务器");
		this.frame = frame;
		loginpane = new LoginPane(this);
		registerpane = new RegisterPane(this);
		this.setSize(400, 300);
		this.setResizable(false);
		this.setModal(true);
		this.add(loginpane);
		this.setLocationRelativeTo(null);
		// 此处设置关掉对话框，程序随之结束
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				System.exit(0);
			}
		});
	}

}
