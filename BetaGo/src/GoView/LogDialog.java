package GoView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LogDialog extends JDialog {
	public LoginPane loginpane;
	public RegisterPane registerpane;
	public JFrame frame;

	public LogDialog(JFrame frame) {
		super(frame, "���ӷ�����");
		this.frame = frame;
		loginpane = new LoginPane(this);
		registerpane = new RegisterPane(this);
		this.setSize(400, 300);
		this.setResizable(false);
		this.setModal(true);
		this.add(loginpane);
		this.setLocationRelativeTo(null);
		// �˴����ùص��Ի��򣬳�����֮����
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				System.exit(0);
			}
		});
	}

}
