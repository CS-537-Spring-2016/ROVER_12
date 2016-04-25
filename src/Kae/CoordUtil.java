package Kae;

import common.Coord;

public class CoordUtil extends Coord {
	private int x, y;

	public CoordUtil(int x, int y) {
		super(x, y);
		this.x = xpos;
		this.y = ypos;
	}

	public void incrementX() {
		x += 1;
	}

	public void incrementY() {
		y += 1;
	}

	public void decrementX() {
		x -= 1;
	}

	public void decrementY() {
		y -= 1;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public CoordUtil clone() {
		return new CoordUtil(x, y);
	}

	@Override
	public String toString() {
		return "Coord [x=" + x + ", y=" + y + "]";
	}
}
