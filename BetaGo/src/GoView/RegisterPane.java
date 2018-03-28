package GoView;

import GoNet.MyIpAddress;
import GoNet.SocketConn;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RegisterPane extends JPanel implements ActionListener {
	private JPanel panel;
	private JTextField field_ipaddress;
	private JTextField field_port;
	private JTextField field_username;
	private JPasswordField field_password;
	private JPasswordField field_confirm;
	private JButton button_login;
	private JButton button_register;
	private LogDialog logdialog;
	private MyIpAddress ipaddress;
	private int port;

	public RegisterPane(LogDialog logdialog) {
		this.logdialog = logdialog;
		panel = new JPanel();

		this.add(new JLabel("注册页面"));

		panel.setLayout(new GridLayout(6, 2, 10, 10));

		panel.add(new JLabel("服务器", JLabel.CENTER));
		field_ipaddress = new JTextField(15);
		field_ipaddress.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_register.doClick();
				}
			}
		});
		panel.add(field_ipaddress);

		panel.add(new JLabel("端口号", JLabel.CENTER));
		field_port = new JTextField(10);
		field_port.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_register.doClick();
				}
			}
		});
		panel.add(field_port);

		panel.add(new JLabel("用户名", JLabel.CENTER));
		field_username = new JTextField(15);
		field_username.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_register.doClick();
				}
			}
		});
		panel.add(field_username);

		panel.add(new JLabel("密码", JLabel.CENTER));
		field_password = new JPasswordField(15);
		field_password.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_register.doClick();
				}
			}
		});
		panel.add(field_password);

		panel.add(new JLabel("确认密码", JLabel.CENTER));
		field_confirm = new JPasswordField(15);
		field_confirm.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_register.doClick();
				}
			}
		});
		panel.add(field_confirm);

		button_register = new JButton("注册");
		panel.add(button_register);
		button_register.addActionListener(this);

		button_login = new JButton("老用户？去登录");
		button_login.setForeground(Color.blue);
		button_login.setBackground(Color.blue);
		button_login.setOpaque(false);
		button_login.setBorderPainted(false);
		button_login.addActionListener(this);
		panel.add(button_login);

		this.add(panel);
	}

	public void actionPerformed(ActionEvent ev) {
		if (ev.getActionCommand().equals("老用户？去登录")) {
			logdialog.remove(logdialog.registerpane);
			logdialog.add(logdialog.loginpane);
			logdialog.repaint();
			logdialog.setVisible(true);
		}
		if (ev.getActionCommand().equals("注册")) {
			if (field_ipaddress.getText().equals("") || field_port.getText().equals("")
					|| field_username.getText().equals("") || field_password.getText().equals("")
					|| field_confirm.getText().equals(""))
				new WarningPane(logdialog, "请填写完整的数据");
			else {
				if (!field_password.getText().equals(field_confirm.getText()))
					new WarningPane(logdialog, "请确认两次密码是否匹配");
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
										field_password.getText(), "Register");
								if (backStr.equals("注册成功")) {
									new WarningPane(logdialog, backStr);
									logdialog.dispose();
									socketConn.setInfoMessage();
									MyInfo myInfo = new MyInfo().getInstance();
									myInfo.setMyInfo(field_username.getText());
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
		}

	}
}
