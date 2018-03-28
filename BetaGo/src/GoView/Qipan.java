package GoView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import GoNet.SocketConn;

public class Qipan extends JPanel {
	private int window_height = Toolkit.getDefaultToolkit().getScreenSize().height;
	private Image qipan_image;
	private int px, py, myType;
	private ArrayQizi arrayqizi;
	private boolean enable = false, over = false; // ��춘�ӛ��ǰ�Ƿ�ԓ�Լ�����
	public boolean isStart = false;

	public Qipan() {
		arrayqizi = new ArrayQizi();
		this.setPreferredSize(new Dimension(window_height, window_height));
		// ����ƶ���ȡ�㲢��Ǹõ�
		this.addMouseMotionListener(new MouseAdapter() {
			public void mouseMoved(MouseEvent ev) {
				if (ev.getX() >= 40 && ev.getX() <= 18 * 38 + 40 && ev.getY() >= 15 && ev.getY() <= 18 * 38 + 15) {

					px = (((ev.getX() - 40) + 19) / 38) * 38 + 40;
					py = (((ev.getY() - 15) + 19) / 38) * 38 + 15;
					repaint();
				} else {
					px = 0;
					py = 0;
					repaint();
				}
			}
		});
		// ������¼�����
		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent ev) {
				if (enable) {
					int i = (py - 15) / 38, j = (px - 40) / 38;
					int count = arrayqizi.setValue(i, j, myType);
					if (count > 400 || count < -400) {
						System.out.println("Υ�����");// 808��ʾ����λ���ѽ����ӣ�909��ʾ����λ���ǽ��c,707��ʾ�ٵ�
					} else {
						repaint();
						isStart = true;
						setEnable(false);
						arrayqizi.back();
						DuiyiInfo duiyiInfo = new DuiyiInfo().getInstance();
						duiyiInfo.timerStart(myType);
						duiyiInfo.setEatChessCount(count);
						SocketConn socketConn = new SocketConn().getInstance();
						socketConn.sendQiziPos(i, j, myType);
					}
				}
			}
		});
		// ���ʧȥ���㲻����ʾ֮ǰ��ǵĵ�
		this.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent ev) {
				px = 0;
				py = 0;
				repaint();
			}
		});
	}

	private static volatile Qipan instance;

	// ��ȡQipan����ĵ���ģʽ����
	public static Qipan getInstance() {
		if (instance == null) {
			synchronized (Qipan.class) {
				if (instance == null) {
					instance = new Qipan();
				}
			}
		}
		return instance;
	}

	public void clearinstance(){
		instance=new Qipan();
	}

	// �����Լ�����������
	public void setMyType(int myType) {
		this.myType = myType;
		arrayqizi.setMytype(myType);
		if (myType == 2) {
			setEnable(true);
		}
	}

	// �Է�����
	public void setOtherType(int i, int j, int type) {
		int count = arrayqizi.setValue(i, j, type);
		repaint();
		arrayqizi.back();
		DuiyiInfo duiyiInfo = new DuiyiInfo().getInstance();
		duiyiInfo.timerStart(type);
		duiyiInfo.setEatChessCount(count);
		setEnable(true);
	}

	// ����enable��ֵ
	public void setEnable(boolean enable) {
		this.enable = enable;
		if (!enable)
			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		else
			this.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	// �յ����������enable��ֵ
	public void setTimer(int type) {
		DuiyiInfo duiyiInfo = new DuiyiInfo().getInstance();
		duiyiInfo.timerStart(type);
	}

	// ���ص�ǰenable��ֵ
	public boolean getEnable() {
		return this.enable;
	}

	// ���öԾֽ���
	public void setOver() {
		enable = false;
		this.over = true;
		DuiyiInfo duiyiInfo = new DuiyiInfo().getInstance();
		duiyiInfo.timerStop();
	}

	// ���ضԾ��Ƿ��Ѿ�����
	public boolean getOver() {
		return this.over;
	}

	// �Լ�����
	public void selfPass() {
		enable = false;
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		DuiyiInfo duiyiInfo = new DuiyiInfo().getInstance();
		duiyiInfo.timerStart(myType);
	}

	// �Է�����
	public void otherPass() {
		enable = true;
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));
		DuiyiInfo duiyiInfo = new DuiyiInfo().getInstance();
		duiyiInfo.timerStart(myType == 1 ? 2 : 1);
	}

	// ����
	public void doRegreat() {
		arrayqizi.setRegreat();
		repaint();
	}

	// ����
	public void doPeace() {
		setOver();
		DuiyiInfo duiyiInfo = new DuiyiInfo().getInstance();
		SocketConn socketConn = new SocketConn().getInstance();
		socketConn.DeleteRoom(duiyiInfo.getName(), duiyiInfo.getCompetitor());
	}

	// ��Ŀ
	public void doCount() {
		setOver();
		int qipan[][]=new int[19][19];
		for(int i=0;i<19;i++)
			for(int j=0;j<19;j++)
		qipan[i][j]=arrayqizi.getValue(i, j);
		int k = 0, c = 0;
		int[] sum = new int[3];
		sum[0] = sum[1] = sum[2] = 0;
		for (int i = 0; i < 19; i++) {
			k = qipan[i][1];
			for (int j = 0; j < 19; j++) {
				c = qipan[j][i];
				switch (c) {
				case 0:
					if (k == 0)
						sum[0]++;
					else
						sum[k]++;
					break;
				case 2:
					if (k == 2)
						sum[c]++;
					else if (k == 0) {
						sum[c] = sum[c] + sum[0] + 1;
						k = c;
						sum[0] = 0;
					} else if (k == 1) {
						sum[c]++;
						k = c;
						sum[0] = 0;
					}
					break;
				case 1:
					if (k == 1)
						sum[c]++;
					else if (k == 0) {
						sum[c] = sum[c] + sum[0] + 1;
						k = c;
						sum[0] = 0;
					} else if (k == 2) {
						sum[c]++;
						k = c;
						sum[0] = 0;
					}
					break;
				}
			}
		}
		SocketConn socketConn=new SocketConn().getInstance();
		DuiyiInfo duiyiInfo=new DuiyiInfo().getInstance();
		MyInfo myInfo=new MyInfo().getInstance();
		String black_player=myType==2?myInfo.getMyNmae():duiyiInfo.getCompetitor();
		String white_player=myType==1?myInfo.getMyNmae():duiyiInfo.getCompetitor();
		ChatRoom chatRoom=new ChatRoom().getInstance();
		if (sum[2] - sum[1] >= 7.5) {
			// ����ʤ
			chatRoom.setSystemMessage("�ڷ���"+black_player+"��ʤ");
			if(myType==2)
				socketConn.sendWinLose(black_player,white_player);
			
		} else {
			//����ʤ
			chatRoom.setSystemMessage("�׷���"+white_player+"��ʤ");
			if(myType==2)
				socketConn.sendWinLose(white_player,black_player);
		}
	}

	public void paint(Graphics g) {
		getImage();
		Graphics2D graphics = (Graphics2D) g;
		graphics.drawImage(qipan_image, 0, 0, window_height, window_height, this);
		graphics.setStroke(new BasicStroke(1.5f));
		int interval = 38;
		int startX = 40, startY = 15, endX = startX + 18 * interval, endY = startY + 18 * interval;
		for (int i = 0; i < 19; i++) {
			graphics.drawLine(startX, startY + i * interval, endX, startY + i * interval);
			graphics.drawLine(startX + i * interval, startY, startX + i * interval, endY);
		}
		for (int k = 3; k <= 15; k += 6)
			for (int l = 3; l <= 15; l += 6)
				graphics.fillOval(k * 38 + 35, l * 38 + 10, 10, 10);

		graphics.setColor(Color.red);
		if (px >= 40 && px <= 18 * 38 + 40 && py >= 15 && py <= 18 * 38 + 15
				&& arrayqizi.getValue((px - 40) / 38, (py - 15) / 38) == 0)
			graphics.drawRect(px - 5, py - 5, 10, 10);
		for (int i = 0; i < 19; i++)
			for (int j = 0; j < 19; j++) {
				int value = arrayqizi.getValue(j, i);
				switch (value) {
				case 0:
					break;
				case 1:
					graphics.setColor(Color.white);
					graphics.fillOval((i * 38 + 40) - 14, (j * 38 + 15) - 14, 28, 28);
					break;
				case 2:
					graphics.setColor(Color.black);
					graphics.fillOval((i * 38 + 40) - 14, (j * 38 + 15) - 14, 28, 28);
					break;
				}
			}
	}

	// ��ȡ���̱���ͼƬ
	public void getImage() {
		try {
			qipan_image = ImageIO.read(this.getClass().getResource("/image/qipan.jpg"));
		} catch (Exception e) {
		}
	}
}

// �����࣬��������Υ���жϣ����ӣ����㣬��ٵĴ���
class ArrayQizi {
	private int A[][], C[][], D[][], E[][], pos[][];// AΪ������Ӽ��ϣ�C\D\EΪ���ݼ������ڻ��壬pos���ڴ�ųԵ��Է����ӵĵ�һ��λ�ã�����ĸ���
	private boolean B[][], Jie;// B[][]Ϊ������������ǵı�ǣ�trueΪ�ѱ�����Jie���ڱ���Ƿ���ܴ��ڴ��
	private int eatCount = 0, back = -1, myType;
	private int Jie_i, Jie_j, Jie_type;// �ֱ��ʾ��ٵ��λ�õ�������
	private int Now_i, Now_j, Now_type;// �ֱ��ʾ��ǰ���ӵ��λ�ú�����
	private Toolkit toolkit;

	public ArrayQizi() {
		toolkit = Toolkit.getDefaultToolkit();
		this.A = new int[19][19];
		this.B = new boolean[19][19];
		this.C = new int[19][19];
		this.D = new int[19][19];
		this.E = new int[19][19];
		this.pos = new int[4][2];
		arrayInital(A);
		arrayInital(C);
		arrayInital(D);
		arrayInital(E);
	}

	// �����ҵ��������ͣ����ڴ�ٴ����ж�
	public void setMytype(int type) {
		this.myType = type;
	}

	// ��������
	public void back() {
		if (back == -1) {
			arrayCopy(C, A);
			System.out.println("C:" + back);
			printChessState(C);
		} else if (back == 0) {
			arrayCopy(D, A);
			System.out.println("D:" + back);
			printChessState(D);
		} else {
			arrayCopy(E, A);
			System.out.println("E:" + back);
			printChessState(E);
		}
		if (back < 1)
			back += 1;
		else
			back = -1;
		System.out.println("back:" + back);
	}

	// ����
	public void setRegreat() {
		if (back == -1) {
			arrayCopy(A, C);
			System.out.println("C:" + back);
			printChessState(C);
		} else if (back == 0) {
			arrayCopy(A, D);
			System.out.println("D:" + back);
			printChessState(D);
		} else {
			arrayCopy(A, E);
			System.out.println("E:" + back);
			printChessState(E);
		}
	}

	// �趨ָ��λ����������
	public int setValue(int i, int j, int type) {
		this.Now_i = i;
		this.Now_j = j;
		this.Now_type = type;
		if (A[i][j] != 0)
			return 808;// 808��ʾ��̎��������
		else {
			A[i][j] = type;
			int count = eatenChesscount(i, j, type);
			return count;
		}
	}

	// ��ȡָ��λ����������
	public int getValue(int i, int j) {
		return this.A[i][j];
	}

	// ��ʼ������B��Ϊfalse
	public void initFlagMatrix() {
		for (int i = 0; i < 19; i++) {
			for (int j = 0; j < 19; j++)
				B[i][j] = false;
		}
	}

	// ͨ������������ҵ�ǰ�����Ƿ�����
	public boolean hasAir(int i, int j, int type) {
		if (A[i][j] == 0)
			return true;
		if (A[i][j] != type)
			return false;
		eatCount++;

		B[i][j] = true;
		if (i > 0 && !B[i - 1][j] && hasAir(i - 1, j, type))
			return true;
		else if (i < 18 && !B[i + 1][j] && hasAir(i + 1, j, type))
			return true;
		else if (j > 0 && !B[i][j - 1] && hasAir(i, j - 1, type))
			return true;
		else if (j < 18 && !B[i][j + 1] && hasAir(i, j + 1, type))
			return true;
		else
			return false;
	}

	// ����A[i][j]��������ͬ���͵�����ȫ���Ե�
	public void eatChess(int i, int j, int type) {
		if (A[i][j] != type)
			return;
		if (eatCount == 1 && type == myType) {// ��ſ�����ɴ�ٵĵ��λ��
			Jie_i = i;
			Jie_j = j;
			Jie_type = type;
			Jie = true;
		}
		if (eatCount == 1 && Jie_type == myType && Now_i == Jie_i && Now_j == Jie_j && Jie) {// ȷ����ٲ�����
			A[Now_i][Now_j] = 0;
			eatCount = 707;
			toolkit.beep();
			return;
		}
		A[i][j] = 0; // �Ե���
		if (i > 0)
			eatChess(i - 1, j, type);
		if (i < 18)
			eatChess(i + 1, j, type);
		if (j > 0)
			eatChess(i, j - 1, type);
		if (j < 18)
			eatChess(i, j + 1, type);
	}

	// �жϵ�ǰ���ӵ�����������ĸ�����ĶԷ������Ƿ�������ȷ���ĸ�����Է����ӵ�λ�ô����pos[][]��
	public boolean hasAirOfType(int type, int p, int q, int[][] pos) {
		int i = 0, j = 0, k = 0;
		boolean flag = true;
		int count = 0;
		for (int h = 0; h < 4; h++) {
			switch (h) {
			case 0:
				i = p - 1;
				j = q;
				break;
			case 1:
				i = p;
				j = q + 1;
				break;
			case 2:
				i = p + 1;
				j = q;
				break;
			case 3:
				i = p;
				j = q - 1;
				break;
			}
			if (i < 0 || j < 0 || i > 18 || j > 18)
				continue;
			if (A[i][j] != type || B[i][j])
				continue;
			eatCount = 0;
			if (!hasAir(i, j, type)) {
				switch (k) {
				case 0:
					pos[0][0] = i;
					pos[0][1] = j;
					k++;
					flag = false;
					break;
				case 1:
					pos[1][0] = i;
					pos[1][1] = j;
					k++;
					flag = false;
					break;
				case 2:
					pos[2][0] = i;
					pos[2][1] = j;
					k++;
					flag = false;
					break;
				case 3:
					pos[3][0] = i;
					pos[3][1] = j;
					k++;
					flag = false;
					break;
				}
				count += eatCount;
			}
		}
		eatCount = count;
		return flag;
	}

	// ��λ��[i,j]�Ÿ��ڰ��ӵ�ʱ��Ե��ӵĸ���
	int eatenChesscount(int i, int j, int type) {
		if (i != Jie_i || j != Jie_j) {
			Jie = false;
			Jie_i = 20;
			Jie_j = 20;
		}
		initFlagMatrix();
		boolean self_hasAir = hasAir(i, j, type);
		eatCount = 0;
		for (int l = 0; l < 4; l++)
			for (int m = 0; m < 2; m++)
				pos[l][m] = -1;

		int other_type = (type == 1 ? 2 : 1);
		initFlagMatrix();
		boolean other_hasAir = hasAirOfType(other_type, i, j, pos);

		if (!self_hasAir && other_hasAir) // ��ɱ
		{
			A[i][j] = 0;
			toolkit.beep();
			return 909;// 909��ʾ�˴�����
		}
		if (!other_hasAir) {
			for (int n = 0; n < 4; n++)
				if (pos[n][0] != -1)
					eatChess(pos[n][0], pos[n][1], other_type);
			if (other_type == 1) // ��������
				return eatCount;
			else // ���Ӹ���
				return 0 - eatCount;
		}
		return 0;
	}

	public void arrayInital(int[][] L) {
		for (int i = 0; i < 19; i++)
			for (int j = 0; j < 19; j++)
				L[i][j] = 0;
	}

	public void arrayCopy(int[][] K, int[][] M) {
		for (int i = 0; i < 19; i++)
			for (int j = 0; j < 19; j++)
				K[i][j] = M[i][j];
	}

	public void printChessState(int[][] K) {
		for (int i = 0; i < 19; i++) {
			for (int j = 0; j < 19; j++) {
				System.out.print(K[i][j] + " ");
			}
			System.out.println();
		}
	}
}