package GoNet;

import java.io.Serializable;

public class InfoWinLose implements Serializable{
	private String winer,loser;
	public InfoWinLose(){
		
	}
	public InfoWinLose(String winer,String loser){
		this.winer=winer;
		this.loser=loser;
	}
	public String getWiner()
	{
		return this.winer;
	}
	public String getLoser(){
		return this.loser;
	}
}
