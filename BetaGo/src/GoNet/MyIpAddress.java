package GoNet;

public class MyIpAddress {
	private String ipaddress;

	public MyIpAddress(String ipaddress) {
		this.ipaddress = ipaddress;
		this.clearSpace();
		this.isIpAddress();
	}

	public void clearSpace() {
		while (ipaddress.startsWith(" ")) {
			ipaddress = ipaddress.substring(1, ipaddress.length()).trim();
		}
		while (ipaddress.endsWith(" ")) {
			ipaddress = ipaddress.substring(1, ipaddress.length()).trim();
		}
	}

	public boolean isIpAddress() {
		if (ipaddress.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
			String s[] = ipaddress.split("\\.");
			if (Integer.parseInt(s[0]) < 255)
				if (Integer.parseInt(s[1]) < 255)
					if (Integer.parseInt(s[2]) < 255)
						if (Integer.parseInt(s[3]) < 255)
							return true;
		}
		return false;
	}

	public String getIp() {
		return this.ipaddress;
	}

}
