package sim;

public class Eater1 { // rabbit
	private int blobx = 50;
	private int bloby = 50;
	private int gen; // can live for max of ageMax generations
	private int timeWOFood;
	private double speed;
	private double range;
	private double energy;
	private double hunger = 0;
	private double fullAt; // stop eating when hunger reaches this value
	private boolean dead = false;
	private int priority; // 0 = food, 1 = running away from foxes
	private int idle; // 0 = doesn't move randomly if it sees nothing, 1 = moves randomly

	private int ageMax = 10;

	public Eater1(int x, int y, double spd, double rang, double parentOE, int pri, int idol) {
		if (x == -1 && y == -1) {
			blobx = (int) (World.dimx * Math.random());
			bloby = (int) (World.dimy * Math.random());
		} else {
			blobx = x;
			bloby = y;
		}
		World.eater1Map[blobx][bloby] = 1;
		gen = 0;
		speed = spd;
		range = rang;
		fullAt = parentOE;
		if (World.mutate1) {
			if (Math.random() < 0.9) // keep same priority as parent
				priority = pri;
			else
				priority = 1 - pri;
			if (Math.random() < 0.9) // keep same idle as parent
				idle = idol;
			else
				idle = 1 - idol;
		} else {
			idle = idol;
			priority = pri;
		}
	}

	public void newDay() {
		hunger = 0;
		energy = World.defEnergy2;
		gen++;
		timeWOFood++;
		if (!World.closeSpawn) {
			World.eater1Map[blobx][bloby] = 0;
			blobx = (int) (World.dimx * Math.random());
			bloby = (int) (World.dimy * Math.random());
			World.eater1Map[blobx][bloby] = hunger + 1;
		}
	}

	public int trial() {
		if (dead || gen > ageMax) // death by old age or being eaten
			return -1;
		// double rng = Math.random();
		if (World.foodMin1 > hunger) {
			if (timeWOFood >= World.maxTimeWOFood1)
				return 0; // dies
			else
				return 1;
		} else {
			timeWOFood = 0;
			if (World.foodCap1 <= hunger)
				return 2; // survives w/ offspring
			else
				return 1; // survives w/o offspring
		}
	}

	public int[] scanArea() { // return relative coordinates of closest food
		int[] foodCoords = new int[] { 0, 0 };
		int[] foxCoords = new int[] { 0, 0 };
		for (int x1 = -1 * (int) range; x1 <= (int) range; x1++) {
			for (int y1 = -1 * (int) range; y1 <= (int) range; y1++) {
				if (World.foodMap[(blobx + x1 + World.dimx) % World.dimx][(bloby + y1 + World.dimy) % World.dimy]
						.getValue() > 0) { // food at [blobx + x1][bloby + y1]
					if (((foodCoords[0] == 0 && foodCoords[1] == 0)
							|| (Math.abs(foodCoords[0]) + Math.abs(foodCoords[1]) > Math.abs(x1) + Math.abs(y1)))) {
						foodCoords[0] = x1;
						foodCoords[1] = y1;
					} else if (Math.abs(foodCoords[0]) + Math.abs(foodCoords[1]) == Math.abs(x1) + Math.abs(y1)) {
						if (Math.random() < 0.5) { // randomly chooses this one over the old one
							foodCoords[0] = x1;
							foodCoords[1] = y1;
						}
					}
				}
				// check for fox at [blobx + x1][bloby + y1]
				if (World.eater2Map[(blobx + x1 + World.dimx) % World.dimx][(bloby + y1 + World.dimy)
						% World.dimy] > 1) {
					if (((foxCoords[0] == 0 && foxCoords[1] == 0)
							|| (Math.abs(foxCoords[0]) + Math.abs(foxCoords[1]) > Math.abs(x1) + Math.abs(y1)))) {
						foxCoords[0] = x1;
						foxCoords[1] = y1;
					} else if (Math.abs(foxCoords[0]) + Math.abs(foxCoords[1]) == Math.abs(x1) + Math.abs(y1)) {
						if (Math.random() < 0.5) { // randomly chooses this one over the old one
							foxCoords[0] = x1;
							foxCoords[1] = y1;
						}
					}
				}
			}
		}
		if ((priority == 0 && (foodCoords[0] != 0 || foodCoords[1] != 0)) || (foxCoords[0] == 0 && foxCoords[1] == 0))
			return foodCoords;
		else {
			foxCoords[0] = runAway(foxCoords[0]);
			foxCoords[1] = runAway(foxCoords[1]);
			return foxCoords;
		}
	}

	private int runAway(int c) {
		if (c == 0)
			return 0;
		else
			return (int) (-1 * c / Math.abs(c) * range);
	}

	public void move(int dx, int dy) {
		World.eater1Map[blobx][bloby] = 0;
		blobx = (blobx + dx) % World.dimx;
		while (blobx < 0)
			blobx += World.dimx;
		bloby = (bloby + dy) % World.dimy;
		while (bloby < 0)
			bloby += World.dimy;
		World.eater1Map[blobx][bloby] = hunger + 1.0;
	}

	public void eat(double amount) {
		hunger += amount;
	}

	public void useEnergy(double used) {
		energy -= used;
	}

	public int getIdle() {
		return idle;
	}

	public int getPriority() {
		return priority;
	}

	public double getEnergy() {
		return energy;
	}

	public double getOE() {
		return fullAt;
	}

	public int getEffectiveSpeed() {
		return (int) Math.ceil((speed - hunger / fullAt));
	}

	public boolean eatMore() {
		return hunger < fullAt;
	}

	public boolean died() {
		return dead;
	}

	public void kill() {
		dead = true;
	}

	public double getHunger() {
		return hunger;
	}

	public int getGen() {
		return gen;
	}

	public double getSpeed() {
		return speed;
	}

	public double getRange() {
		return range;
	}

	public int getX() {
		return blobx;
	}

	public int getY() {
		return bloby;
	}
}
