package GoView;

import javax.swing.*;
import java.awt.*;

public class WarningPane extends JOptionPane {
	public WarningPane(Component parent, String message) {
		this.showMessageDialog(parent, message);
	}
}
