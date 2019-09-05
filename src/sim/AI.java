package sim;

import java.util.Random;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class AI {

	public static void main(String[] args) throws IOException {
		Random rand = new Random();
		
		int dayReached = 0, avg = 0, oldavg = 0;
//		int[] p = new int[] { 200, 5, 1200, 1, 3, 2000, 6, 12 };
		int[] change = new int[] { 200, 5, 1200, 1, 3, 2000, 6, 12 };
		int i, tries = 0;
		boolean progress = false;
		int trialCount = 10;
		int foodGrowth = 200;
		int litterCount = 5;
		int defEnergy1 = 1200;
		int foodMin1 = 1;
		int foodCap1 = 3;
		int defEnergy2 = 2000;
		int foodMin2 = 6;
		int foodCap2 = 12;
//			World test = new World(true, true, foodGrowth, litterCount, defEnergy1, foodMin1, foodCap1, defEnergy2, foodMin2, foodCap2);

		int[] p = new int[] { 125, 6, 1200, 1, 3, 2000, 5, 10 };
		int[] oldp = new int[] { 125, 6, 1200, 1, 3, 2000, 5, 10 };
//		food, litter, energy1, min1, cap1, energy2, min2, cap2
//		93, 5, 907, 1, 3, 2358, 5, 12
//		83, 5, 686, 1, 3, 2507, 4, 13
//		74, 6, 676, 1, 3, 2928, 4, 12, 
//		196: 155, 7, 455, 1, 2, 706, 10, 6
//		94: 179, 7, 498, 1, 2, 1585, 7, 13
//		88: 180, 5, 915, 1, 4, 2181, 8, 15
//		97: 162, 4, 1005, 1, 4, 2957, 9, 13

		String save;
		int reps = 0;
		while (oldavg < 500 && reps < 500) {
			reps++;
			oldp[0] = rand.nextInt(300) + 20;
			oldp[1] = rand.nextInt(8) + 1;
			oldp[5] = rand.nextInt(2000) + 500;
			oldp[4] = rand.nextInt(5) + 2;
			oldp[7] = rand.nextInt(20) + 2;
			oldp[2] = oldp[5] / 2;
			oldp[6] = oldp[7] / 2;
			oldp[3] = oldp[4] / 2;
			oldavg = 0;
			for (i = 0; i < trialCount; i++) {
				World test = new World(true, true, oldp[0], oldp[1], oldp[2], oldp[3], oldp[4], oldp[5], oldp[6],
						oldp[7]);
				oldavg += test.ended();
			}
			oldavg /= trialCount;
			save = "Baseline: " + oldavg + "\nSet: " + oldp[0];
			for (i = 1; i < p.length; i++)
				save += ", " + oldp[i];
			
			tries = 0;
			while (tries < 30) {
				tries++;
				avg = 0;
				if (progress)
					for (i = 0; i < p.length; i++)
						p[i] += change[i];
				else
					for (i = 0; i < p.length; i++) {
						change[i] = (int) Math.round(p[i] * 0.1 * rand.nextGaussian());
						p[i] += change[i];
					}
				for (i = 0; i < trialCount; i++) {
					World test = new World(true, true, p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
					avg += test.ended();
				}
				avg /= trialCount;
//				System.out.println("\n\nThis attempt: " + avg);
//				System.out.print("Current set:");
//				for (i = 0; i < p.length; i++)
//					System.out.print(", " + p[i]);
				if (avg > oldavg) {
					for (i = 0; i < p.length; i++)
						oldp[i] = p[i];
					oldavg = avg;
					progress = true;
				} else {
					progress = false;
//					System.out.println("\nBest attempt: " + oldavg);
//					System.out.print("Best set:");
//					for (i = 0; i < oldp.length; i++)
//						System.out.print(", " + oldp[i]);
					for (i = 0; i < p.length; i++)
						p[i] = oldp[i];
				}
			}
//			writeToFile("Rep finished", true);
			writeToFile("                                                           Best attempt: " + oldavg, true);
			writeToFile("Best set: " + oldp[0], false);
			for (i = 1; i < oldp.length; i++)
				writeToFile(", " + oldp[i], false);
			writeToFile("\n"+save, true);
			writeToFile("\n", false);
		}
	}

	public static void writeToFile(String text, boolean newLine) throws IOException {
		FileWriter write = new FileWriter("Presets", true);
		PrintWriter print = new PrintWriter(write);
		if (newLine)
			print.printf("%s" + "%n", text);
		else
			print.printf("%s", text);
		print.close();
	}
}
