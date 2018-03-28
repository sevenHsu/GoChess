package GoView;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class Duiyi extends JPanel {
	private Qipan qipan;
	private DuiyiInfo duiyiinfo;
	private ChatRoom chatroom;
	private CommandPane commandpane;
	public JSplitPane split1, split2, split3;

	public Duiyi() {
		qipan = new Qipan().getInstance();
		duiyiinfo = new DuiyiInfo().getInstance();
		chatroom = new ChatRoom().getInstance();
		commandpane = new CommandPane();
		split3 = new JSplitPane(0, chatroom, commandpane);
		split3.setDividerLocation(470);
		split2 = new JSplitPane(0, duiyiinfo, split3);
		split1 = new JSplitPane(1, qipan, split2);
		this.setLayout(new BorderLayout());
		this.add(split1);
	}
	public void clearInstace(){
		qipan.clearinstance();
		duiyiinfo.clearinstance();
		chatroom.clearinstance();
	}
}
