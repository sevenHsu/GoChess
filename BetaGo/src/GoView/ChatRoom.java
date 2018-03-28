package GoView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.text.DefaultCaret;

import GoNet.SocketConn;

public class ChatRoom extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int window_width = Toolkit.getDefaultToolkit().getScreenSize().width;
	private int window_height = Toolkit.getDefaultToolkit().getScreenSize().height;
	private JTextArea text_receiver;
	private JTextField text_sender;
	private JButton button_send;
	private MyInfo myInfo;
	private String myName;
	private Font font = new Font(null, 15, 15);

	public ChatRoom() {
		this.setPreferredSize(new Dimension(window_width - window_height - 200, window_height * 13 / 20));
		this.setLayout(new BorderLayout());
		text_receiver = new JTextArea();
		text_receiver.setFont(font);
		text_receiver.setEditable(false);
		DefaultCaret caret = (DefaultCaret) text_receiver.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		text_sender = new JTextField(30);
		text_sender.setFont(font);
		text_sender.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					button_send.doClick();
				}
			}
		});
		button_send = new JButton("发送");
		button_send.addActionListener(this);
		this.add(new JScrollPane(text_receiver));
		JToolBar toolbar = new JToolBar();
		toolbar.add(text_sender);
		toolbar.add(button_send);
		this.add(toolbar, "South");
		myInfo = new MyInfo().getInstance();
		myName = myInfo.getMyNmae();
	}

	public void actionPerformed(ActionEvent ev) {
		String sendText = text_sender.getText().trim();
		if (sendText != "") {
			text_receiver.append("我（" + myName + "）：" + sendText + "\n");
			text_sender.setText("");
			SocketConn socketConn = new SocketConn().getInstance();
			socketConn.sendChatMessage(sendText);
		}
	}

	public void setReceivedMessage(String receivedMessage) {
		text_receiver.append(receivedMessage);
	}

	public void setSystemMessage(String systemMessage) {
		text_receiver.setForeground(Color.red);
		text_receiver.append("（系统提示）：" + systemMessage + "\n");
		text_receiver.setForeground(Color.black);
	}

	private static volatile ChatRoom instance;

	// 获取Qipan对象的单列模式函数
	public static ChatRoom getInstance() {
		if (instance == null) {
			synchronized (ChatRoom.class) {
				if (instance == null) {
					instance = new ChatRoom();
				}
			}
		}
		return instance;
	}
	public void clearinstance(){
		instance=new ChatRoom();
	}
}
