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

		this.add(new JLabel("ע��ҳ��"));

		panel.setLayout(new GridLayout(6, 2, 10, 10));

		panel.add(new JLabel("������", JLabel.CENTER));
		field_ipaddress = new JTextField(15);
		field_ipaddress.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_register.doClick();
				}
			}
		});
		panel.add(field_ipaddress);

		panel.add(new JLabel("�˿ں�", JLabel.CENTER));
		field_port = new JTextField(10);
		field_port.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_register.doClick();
				}
			}
		});
		panel.add(field_port);

		panel.add(new JLabel("�û���", JLabel.CENTER));
		field_username = new JTextField(15);
		field_username.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_register.doClick();
				}
			}
		});
		panel.add(field_username);

		panel.add(new JLabel("����", JLabel.CENTER));
		field_password = new JPasswordField(15);
		field_password.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_register.doClick();
				}
			}
		});
		panel.add(field_password);

		panel.add(new JLabel("ȷ������", JLabel.CENTER));
		field_confirm = new JPasswordField(15);
		field_confirm.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_register.doClick();
				}
			}
		});
		panel.add(field_confirm);

		button_register = new JButton("ע��");
		panel.add(button_register);
		button_register.addActionListener(this);

		button_login = new JButton("���û���ȥ��¼");
		button_login.setForeground(Color.blue);
		button_login.setBackground(Color.blue);
		button_login.setOpaque(false);
		button_login.setBorderPainted(false);
		button_login.addActionListener(this);
		panel.add(button_login);

		this.add(panel);
	}

	public void actionPerformed(ActionEvent ev) {
		if (ev.getActionCommand().equals("���û���ȥ��¼")) {
			logdialog.remove(logdialog.registerpane);
			logdialog.add(logdialog.loginpane);
			logdialog.repaint();
			logdialog.setVisible(true);
		}
		if (ev.getActionCommand().equals("ע��")) {
			if (field_ipaddress.getText().equals("") || field_port.getText().equals("")
					|| field_username.getText().equals("") || field_password.getText().equals("")
					|| field_confirm.getText().equals(""))
				new WarningPane(logdialog, "����д����������");
			else {
				if (!field_password.getText().equals(field_confirm.getText()))
					new WarningPane(logdialog, "��ȷ�����������Ƿ�ƥ��");
				else {
					ipaddress = new MyIpAddress(field_ipaddress.getText());
					if (!ipaddress.isIpAddress())
						new WarningPane(logdialog, "��������ȷ�ķ�����(IP)��ַ");
					else {
						if (!field_port.getText().matches("\\d{1,5}"))
							new WarningPane(logdialog, "��������ȷ�Ķ˿ں�");
						else {
							// ip��ַ
							String ip = this.ipaddress.getIp();
							// �˿ں�
							this.port = Integer.parseInt(field_port.getText());
							// �˴����ӷ�����
							SocketConn socketConn = new SocketConn().getInstance();
							socketConn.SocketConnect(ip, port);
							if (!socketConn.isConnected())
								new WarningPane(logdialog, "���ӷ�����ʧ��");
							else {
								String backStr = socketConn.RegisterLogin(field_username.getText(),
										field_password.getText(), "Register");
								if (backStr.equals("ע��ɹ�")) {
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
