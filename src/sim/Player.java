package sim;

public class Player {

	private int xpos; // left side
	private int ypos; // top side
	private int size = 2;
	private double trueSize = 2;
	private boolean cm = true;

	public Player() {
		relocate();
	}

	public void move(int dx, int dy) {
		if (cm) {
			xpos = (xpos + dx + World.dimx) % World.dimx;
			ypos = (ypos + dy + World.dimy) % World.dimy;
			cm = false;
		}
	}

	public void relocate() {
		xpos = size * (int) (Math.random() * World.dimx / size);
		ypos = size * (int) (Math.random() * World.dimy / size);
	}

	public void grow(double a) {
		trueSize += a;
		size = (int) trueSize;
	}

	public void setCM(boolean m) {
		cm = m;
	}

	public int getBigness() {
		return size;
	}

	public int getX() {
		return xpos;
	}

	public int getY() {
		return ypos;
	}

}
