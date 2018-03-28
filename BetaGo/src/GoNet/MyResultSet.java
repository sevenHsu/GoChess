package GoNet;

import java.io.Serializable;
import java.sql.ResultSet;

public class MyResultSet implements Serializable {
	private String[][] resultSets;
	private String type;
	private int count = 0;

	public MyResultSet() {

	}

	public MyResultSet create(String type, ResultSet Connset) {
		this.type = type;
		try {
			while (Connset.next())
				count++;
			resultSets = new String[count][5];
			int i = 0, j;
			while (Connset.previous()) {
				for (j = 0; j < 5; j++)
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

	public String getType() {
		return this.type;
	}
}
