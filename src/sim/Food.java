package sim;

public class Food { // grass

	private int age = 0; // decomposes on the 4th day
	private int food = 0;

	public Food() {

	}

	public void age() {
		if (food > 0)
			age++;
	}

	public int getAge() {
		return age;
	}

	public void reset() {
		age = 0;
		food = 0;
	}

	public void setValue(int f) {
		food = f;
	}

	public void plusOne() {
		food++;
	}

	public int getValue() {
		return food;
	}

}
