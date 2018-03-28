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

		this.add(new JLabel("��¼ҳ��"));

		panel.setLayout(new GridLayout(5, 2, 10, 10));

		panel.add(new JLabel("������", JLabel.CENTER));
		field_ipaddress = new JTextField(15);// ����15
		field_ipaddress.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_login.doClick();
				}
			}
		});
		panel.add(field_ipaddress);

		panel.add(new JLabel("�˿ں�", JLabel.CENTER));
		field_port = new JTextField(15);
		field_port.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_login.doClick();
				}
			}
		});
		panel.add(field_port);
		panel.add(new JLabel("�û���", JLabel.CENTER));
		field_username = new JTextField(15);
		field_username.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_login.doClick();
				}
			}
		});
		panel.add(field_username);

		panel.add(new JLabel("��    ��", JLabel.CENTER));
		field_password = new JPasswordField(15);
		field_password.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_login.doClick();
				}
			}
		});
		panel.add(field_password);

		button_login = new JButton("��¼");
		panel.add(button_login);
		button_login.addActionListener(this);

		button_register = new JButton("���û���ȥע��");
		button_register.setForeground(Color.blue);
		button_register.setBackground(Color.blue);
		button_register.setOpaque(false);
		button_register.setBorderPainted(false);
		button_register.addActionListener(this);
		panel.add(button_register);

		this.add(panel);
	}

	public void actionPerformed(ActionEvent ev) {
		if (ev.getActionCommand().equals("��¼")) {
			if (field_ipaddress.getText().equals("") || field_port.getText().equals("")
					|| field_username.getText().equals("") || field_password.getText().equals(""))
				new WarningPane(logdialog, "����д����������");
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
									field_password.getText(), "Login");
							if (backStr.equals("��¼�ɹ�")) {
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
		if (ev.getActionCommand().equals("���û���ȥע��")) {
			logdialog.remove(logdialog.loginpane);
			logdialog.add(logdialog.registerpane);
			logdialog.repaint();
			logdialog.setVisible(true);
		}

	}
}
