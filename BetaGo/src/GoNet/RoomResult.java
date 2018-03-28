package GoNet;

import java.io.Serializable;
import java.sql.ResultSet;

public class RoomResult implements Serializable {
	private String[][] resultSets;
	private int count = 0;

	public RoomResult() {

	}

	public RoomResult create(ResultSet Connset) {
		try {
			while (Connset.next())
				count++;
			resultSets = new String[count][2];
			int i = 0, j;
			while (Connset.previous()) {
				for (j = 0; j < 2; j++)
					resultSets[i][j] = Connset.getString(j + 1);
				i++;
			}
			return this;
		} catch (Exception ev) {
			ev.printStackTrace();
			return null;
		}
	}

	private int k = 1;

	public boolean next() {

		if (k > count)
			return false;
		else {
			k++;
			return true;
		}
	}

	public String getString(int pos) {
		return this.resultSets[k - 2][pos - 1];
	}
}
