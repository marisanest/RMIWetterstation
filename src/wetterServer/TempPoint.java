package wetterServer;

import java.io.Serializable;

public class TempPoint implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int id;
	private double temp;
	
	public TempPoint(int id, Double temp){
		this.id = id;
		this.temp = temp;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getTemp() {
		return temp;
	}

	public void setTemp(double temp) {
		this.temp = temp;
	}
	
	public String toString(){
		return id+": "+temp;
	}

}
