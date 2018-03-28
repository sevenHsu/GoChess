package GoNet;

import java.io.Serializable;

public class InfoQiziPos implements Serializable{
	private int i,j,type;
	public InfoQiziPos(){
		
	}
	public InfoQiziPos(int i,int j,int type){
		this.i=i;
		this.j=j;
		this.type=type;
	}
	public int getI(){
		return this.i;
	}
	public int getJ(){
		return this.j;
	}
	public int getType(){
		return this.type;
	}
}
