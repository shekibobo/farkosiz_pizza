package farkosizPizza.OrderCenter;

public class Topping {
	String name;
	int id;
	int position;
	
	final int NONE = 0;
	final int RIGHT = 1;
	final int LEFT = 2;
	final int WHOLE = 3;
	
	public Topping(DatabaseHelper db, int id) {
		this.id = id;
		name = db.getToppingName(id + 1); //db uses 1-based index
		position = NONE;
	}
	
	public void setPosition(int pos) {
		this.position = pos;
	}
	
	public void nextPosition() {
		this.position = (this.position + 3) % 4;
	}
	
	public int getToppingPosition() {
		return position;
	}
	
	public String getToppingName() {
		return name;
	}
}
