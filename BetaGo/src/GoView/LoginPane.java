package GoView;

import GoNet.MyIpAddress;
import GoNet.InfoMessage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import GoNet.SocketConn;

public class LoginPane extends JPanel implements ActionListener {
	private JPanel panel;
	private JTextField field_ipaddress;
	private JTextField field_port;
	private JTextField field_username;
	private JPasswordField field_password;
	private JButton button_login;
	private MyIpAddress ipaddress;
	private int port;
	public JButton button_register;
	public LogDialog logdialog;

	public LoginPane(LogDialog logdialog) {
		this.logdialog = logdialog;
		panel = new JPanel();

		this.add(new JLabel("登录页面"));

		panel.setLayout(new GridLayout(5, 2, 10, 10));

		panel.add(new JLabel("服务器", JLabel.CENTER));
		field_ipaddress = new JTextField(15);// 长度15
		field_ipaddress.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_login.doClick();
				}
			}
		});
		panel.add(field_ipaddress);

		panel.add(new JLabel("端口号", JLabel.CENTER));
		field_port = new JTextField(15);
		field_port.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_login.doClick();
				}
			}
		});
		panel.add(field_port);
		panel.add(new JLabel("用户名", JLabel.CENTER));
		field_username = new JTextField(15);
		field_username.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_login.doClick();
				}
			}
		});
		panel.add(field_username);

		panel.add(new JLabel("密    码", JLabel.CENTER));
		field_password = new JPasswordField(15);
		field_password.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_login.doClick();
				}
			}
		});
		panel.add(field_password);

		button_login = new JButton("登录");
		panel.add(button_login);
		button_login.addActionListener(this);

		button_register = new JButton("新用户？去注册");
		button_register.setForeground(Color.blue);
		button_register.setBackground(Color.blue);
		button_register.setOpaque(false);
		button_register.setBorderPainted(false);
		button_register.addActionListener(this);
		panel.add(button_register);

		this.add(panel);
	}

	public void actionPerformed(ActionEvent ev) {
		if (ev.getActionCommand().equals("登录")) {
			if (field_ipaddress.getText().equals("") || field_port.getText().equals("")
					|| field_username.getText().equals("") || field_password.getText().equals(""))
				new WarningPane(logdialog, "请填写完整的数据");
			else {
				ipaddress = new MyIpAddress(field_ipaddress.getText());
				if (!ipaddress.isIpAddress())
					new WarningPane(logdialog, "请输入正确的服务器(IP)地址");
				else {
					if (!field_port.getText().matches("\\d{1,5}"))
						new WarningPane(logdialog, "请输入正确的端口号");
					else {
						// ip地址
						String ip = this.ipaddress.getIp();
						// 端口号
						this.port = Integer.parseInt(field_port.getText());
						// 此处连接服务器
						SocketConn socketConn = new SocketConn().getInstance();
						socketConn.SocketConnect(ip, port);
						if (!socketConn.isConnected())
							new WarningPane(logdialog, "连接服务器失败");
						else {
							String backStr = socketConn.RegisterLogin(field_username.getText(),
									field_password.getText(), "Login");
							if (backStr.equals("登录成功")) {
								new WarningPane(logdialog, backStr);
								logdialog.dispose();
								// logdialog
								socketConn.setInfoMessage();
								MyInfo myInfo = new MyInfo().getInstance();
								myInfo.setMyInfo(field_username.getText());
								myInfo.setMyState(0);
								socketConn.OnlineDeal(myInfo.getMyNmae(), "LoginSuccess");
								logdialog.frame.setTitle("BetaGo "+field_username.getText());
							} else {
								new WarningPane(logdialog, backStr);
								socketConn.setInfoMessage();
							}
						}
					}

				}
			}
		}
		if (ev.getActionCommand().equals("新用户？去注册")) {
			logdialog.remove(logdialog.loginpane);
			logdialog.add(logdialog.registerpane);
			logdialog.repaint();
			logdialog.setVisible(true);
		}

	}
}
