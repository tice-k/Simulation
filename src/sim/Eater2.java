package sim;

public class Eater2 { // fox
	private int blobx;
	private int bloby;
	private int gen; // can live for max of ageMax generations
	private int timeWOFood = 0;
	private int incubation;
	private double speed;
	private double range;
	private double energy;
	private double hunger = 0;
	private double fullAt; // stop eating when hunger reaches this value
	private int idle = 0; // 0 = doesn't move randomly if it sees nothing, 1 = moves randomly

	private int ageMax = 20; // about twice Eater1

	public Eater2(int x, int y, double spd, double rang, double parentOE, int idol) {
		if (x == -1 && y == -1) {
			blobx = (int) (World.dimx * Math.random());
			bloby = (int) (World.dimy * Math.random());
		} else {
			blobx = x;
			bloby = y;
		}
		setArea(1);
		gen = 0;
		fullAt = parentOE;
		speed = spd;
		range = rang;
		if (World.mutate2)
			if (Math.random() < 0.9) // keep same idle as parent
				idle = idol;
			else
				idle = 1 - idol;
		else
			idle = idol;
	}

	public void newDay() {
		if (!World.closeSpawn) {
			setArea(0);
			blobx = (int) (World.dimx * Math.random());
			bloby = (int) (World.dimy * Math.random());
			setArea(1);
		}
		timeWOFood++;
		hunger = 0;
		energy = World.defEnergy2;
		gen++;
	}

	public int trial() {
		if (gen > ageMax) // death by old age
			return -1;
		if (World.foodMin2 > hunger) { // didn't eat enough
			incubation = 0;
			if (timeWOFood >= World.maxTimeWOFood2)
				return 0; // dies
			else
				return 1;
		} else {
			timeWOFood = 0;
			if (World.foodCap2 <= hunger && incubation == 0)
				incubation = 1;
			else {
				if (incubation == 2) { // pregnancy lasts 3 days
					incubation = 0;
					return 2; // offspring is born
				} else if (incubation >= 1)
					incubation++;
			}
			return 1; // survives w/o offspring
		}
	}

	public void move(int dx, int dy) {
		if (dx == 0 && dy == 0) {
			setArea(1);
		} else {
			setArea(0);
			blobx = (blobx + dx + World.dimx) % World.dimx;
			while (blobx < 0)
				blobx += World.dimx;
			bloby = (bloby + dy + World.dimy) % World.dimy;
			while (bloby < 0)
				bloby += World.dimy;
			setArea(2);
		}
	}

	public void eat(double amount) {
		hunger += amount;
	}

	public int[] scanArea() {
		int[] crds = new int[] { 0, 0 };
		for (int x1 = -1 * (int) range; x1 <= (int) range + 1; x1++) {
			for (int y1 = -1 * (int) range; y1 <= (int) range + 1; y1++) {
				if (World.eater1Map[(blobx + x1 + World.dimx) % World.dimx][(bloby + y1 + World.dimy)
						% World.dimy] > 0) { // food at [blobx + x1][bloby + y1]
					if (((crds[0] == 0 && crds[1] == 0)
							|| (Math.abs(crds[0]) + Math.abs(crds[1]) > Math.abs(x1) + Math.abs(y1)))) {
						crds[0] = x1;
						crds[1] = y1;
					} else if (Math.abs(crds[0]) + Math.abs(crds[1]) > Math.abs(x1) + Math.abs(y1)) {
						crds[0] = x1;
						crds[1] = y1;
					} else if (Math.abs(crds[0]) + Math.abs(crds[1]) == Math.abs(x1) + Math.abs(y1)) {
						if (Math.random() < 0.5) { // randomly chooses this one over the old one
							crds[0] = x1;
							crds[1] = y1;
						}
					}
				}
			}
		}
		return crds;
	}

	public void setArea(int value) {
		World.eater2Map[blobx][bloby] = value;
		World.eater2Map[(blobx + 1 + World.dimx) % World.dimx][bloby] = value;
		World.eater2Map[(blobx + 1 + World.dimx) % World.dimx][(bloby + 1 + World.dimy) % World.dimy] = value;
		World.eater2Map[blobx][(bloby + 1 + World.dimy) % World.dimy] = value;
	}

	public double atFood() {
		return World.eater1Map[blobx][bloby] + World.eater1Map[(blobx + 1 + World.dimx) % World.dimx][bloby]
				+ World.eater1Map[(blobx + 1 + World.dimx) % World.dimx][(bloby + 1 + World.dimy) % World.dimy]
				+ World.eater1Map[blobx][(bloby + 1 + World.dimy) % World.dimy];
	}

	public void useEnergy(double used) {
		energy -= used;
	}

	public int getIdle() {
		return idle;
	}

	public double getEnergy() {
		return energy;
	}

	public int getIncubation() {
		return incubation;
	}

	public int getFoodMin() {
		return World.foodMin2;
	}

	public double getOE() {
		return fullAt;
	}

	public int getEffectiveSpeed() {
		return (int) Math.ceil((speed - hunger / fullAt));
	}

	public boolean eatMore() {
		if (incubation > 0)
			return hunger < World.foodMin2;
		else
			return hunger < fullAt;
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
