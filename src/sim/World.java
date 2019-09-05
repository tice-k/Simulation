package sim;

// Taisuke Miyamoto, August 9, 2019

import java.util.Arrays;
import java.util.Random;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

public class World extends JPanel {

	private static final long serialVersionUID = 1L;

	// Custom settings
	private int slow = 2; // 0 = update / 10 days, 1 = 10 times as fast, 2 = day delay, 3 = tick delay
	private int tickSpeed = 150;
	private boolean autoRestart = false;
	private boolean learning = false;
	private int dayLength = 20; // # of ticks in a day
	private double foodGrowth = 100; // (dimx * dimy / 200.0) / dayLength; 86.4 / 50 = 1.73
	private int initialFood = 0;
	public static boolean closeSpawn = false;
	private boolean debug = false;
	private boolean stableFood = false;
	private boolean showGraph = true;
	private boolean showBoard = true;
	private int graphData = 0; // 0 = pop graph, 1 = rabbit data, 2 = fox data, 3 = plot points
	// rabbit settings
	private int pop1Size = 100;
	private int litterCount = 6; // number of offspring
	public static int defEnergy1 = 300;
	public static int foodMin1 = 1;
	public static int foodCap1 = 3; // will reproduce if hunger reaches this
	public static int maxTimeWOFood1 = 1;
	private int defOE1 = 4;
	private int defSpeed1 = 3;
	private int defRange1 = 8;
	private int defPriority = 0; // 0 = prioritize food, 1 = prioritize running away from threat
	private int defIdle1 = 1; // 0 = move aimlessly OFF, 1 = move aimlessly ON
	public static boolean mutate1 = true;
	private double speedMutation1 = 0.1; // largest possible speed change across 1 generation
	private double rangeMutation1 = 0.1;
	private double OEMutation1 = 0.2;
	// fox settings
	private int pop2Size = 10;
	public static int defEnergy2 = 450;
	public static int foodMin2 = 4; // reach this for survival
	public static int foodCap2 = 9; // reach this for guaranteed replication
	public static int maxTimeWOFood2 = 2;
	private int defOE2 = 10;
	private int defSpeed2 = 5;
	private int defRange2 = 10;
	private int defIdle2 = 0; // 0 = move aimlessly OFF, 1 = move aimlessly ON
	public static boolean mutate2 = true;
	private double speedMutation2 = 0.6; // largest possible speed change across 2 generation
	private double rangeMutation2 = 0.6;
	private double OEMutation2 = 0.2;

	private int noonBrightness = 150; // 255 = white, 51 = darkest
	private int day = 0;
	private int dayLimit = 999;
	private int tick; // split into two parts 1 - movement, 2 - eating
	private boolean pause = false;
	private final int frameXFinal = 720;
	private static int frameX = 720;
	private static int frameY = 600;
	public static int PIXEL = 6;
	public static int dimx = frameX / PIXEL;
	public static int dimy = frameY / PIXEL;
	private ArrayList<Eater1> pop1 = new ArrayList<Eater1>(); // rabbits
	private ArrayList<Eater2> pop2 = new ArrayList<Eater2>(); // foxes
	public static Food[][] foodMap = new Food[dimx][dimy]; // food map; 0 = empty, 1 = free food, 2+ - claimed food
	public static double[][] eater1Map = new double[dimx][dimy]; // cell contains hunger + 1 value
	public static double[][] eater2Map = new double[dimx][dimy]; // cell contains 1 if nonmoving, 2 if moving
	private int graphBorder = 50;
	private int graphX = 360;
	private int graphY = 360; // frameY - 2 * graphBorder;
	private int graphXScale = 4; // x axis scale
	private double graphYScale; // y axis scale, smaller = larger max. max value = graphY / graphYScale
	private double plotYMin = 0;
	private double plotYMax = 20;
	private double plotXMin = 0;
	private double plotXMax = 20;
	@SuppressWarnings("unchecked")
	private ArrayList<Double>[] data = new ArrayList[12];
//	Data contained in array
//	0. pop1 Color.cyan
//	1. pop2 Color.pink
//	2. food Color.yellow
//	3. speed1 Color.blue
//	4. range1 Color.green
//	5. OE1 Color.orange
//	6. priority1 Color.white
//	7. idle1 Color.pink
//	8. speed2 Color.blue
//	9. range2 Color.green
//	10. OE2 Color.orange
//	11. idle2 Color.pink

	private Random rand = new Random();

	private JFrame frame = new JFrame("Simulation");
	private JLabel dataTitle = new JLabel();
	private JLabel graphPop1 = new JLabel();
	private JLabel graphPop2 = new JLabel();
	private JLabel graphFood = new JLabel();
	private JLabel graphSpeed = new JLabel();
	private JLabel graphRange = new JLabel();
	private JLabel graphOE = new JLabel();
	private JLabel graphPriority = new JLabel();
	private JLabel graphIdle = new JLabel();
	private JLabel graphYLeftAxisTop = new JLabel();
	private JLabel graphYLeftAxisMid = new JLabel();
	private JLabel graphYLeftAxisBot = new JLabel();
//	private JLabel graphYRightAxisTop = new JLabel();
//	private JLabel graphYRightAxisMid = new JLabel();
//	private JLabel graphYRightAxisBot = new JLabel();
	private JLabel graphXAxisTop = new JLabel();
	private JLabel graphXAxisBot = new JLabel();
	private JLabel graphDate = new JLabel();

	private Player player1 = new Player();
	private Player player2 = new Player();
	private int playerCount = 0; // # of players

	public static void main(String[] args) {
		new World(false, false, 0,0,0,0,0,0,0,0);
//		new World(false, true, 104, 6, 308, 1, 3, 460, 4, 9);
//		foodGrowth, litterCount, defEnergy1, foodMin1, foodCap1, defEnergy2, foodMin2, foodCap2
	}

	public World(boolean learn, boolean custom, int fG, int lC, int defE1, int fMin1, int fCap1, int defE2, int fMin2, int fCap2) {
		learning = learn;
		if (custom) {
			foodGrowth = fG;
			litterCount = lC;
			defEnergy1 = defE1;
			foodMin1 = fMin1;
			foodCap1 = fCap1;
			defEnergy2 = defE2;
			foodMin2 = fMin2;
			foodCap2 = fCap2;
		}
		if (learn) {
			tickSpeed = 0;
			slow = -1;
		} else
			initBoard();
		setup();
		dayCycle();
	}

	private void initBoard() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		if (frameX + graphX > 1200)
			showGraph = false;
		if (showGraph) {
			frame.setSize(frameX + 14 + graphX + graphBorder * 2, frameY + 37);
		} else {
			frame.setSize(frameX + 14, frameY + 37);
		}
		dataTitle.setBounds(frameX + graphBorder, graphBorder / 2, graphX, 20);
		graphLabelSetup(dataTitle, Color.gray);
		graphYLeftAxisTop.setBounds(frameX + graphBorder * 3 / 10, (int) (graphBorder + graphY * 0.1 - 6),
				graphBorder * 3 / 5, 12);
		// setup axis labels
		graphLabelSetup(graphYLeftAxisTop, Color.gray);
		graphLabelSetup(graphYLeftAxisMid, Color.gray);
		graphLabelSetup(graphYLeftAxisBot, Color.gray);
//		graphYRightAxisTop.setBounds(frameX + graphX + graphBorder * 5 / 4, (int) (graphBorder + graphY * 0.1 - 6),
//				graphBorder * 3 / 4, 12);
//		graphLabelSetup(graphYRightAxisTop, Color.gray);
//		graphYRightAxisMid.setBounds(frameX + graphX + graphBorder * 5 / 4,
//				(int) (graphBorder + graphY * 5.5 * 0.1 - 6), graphBorder * 3 / 4, 12);
//		graphLabelSetup(graphYRightAxisMid, Color.gray);
//		graphYRightAxisBot.setBounds(frameX + graphBorder * 5 / 4 + graphX, (int) graphBorder + graphY - 6,
//				graphBorder * 3 / 4, 12);
//		graphLabelSetup(graphYRightAxisBot, Color.gray);
		graphXAxisTop.setBounds((int) (frameX + graphBorder + graphX * 0.9 - 6), graphY + graphBorder + 10, 40, 12);
		graphLabelSetup(graphXAxisTop, Color.gray);
		graphXAxisBot.setBounds((int) (frameX + graphBorder - 6), graphY + graphBorder + 10, 40, 12);
		graphLabelSetup(graphXAxisBot, Color.gray);

		graphLabelSetup(graphDate, Color.gray);
		graphLabelSetup(graphPop1, Color.cyan);
		graphLabelSetup(graphPop2, Color.pink);
		graphLabelSetup(graphFood, Color.yellow);
		graphLabelSetup(graphSpeed, Color.blue);
		graphLabelSetup(graphRange, Color.green);
		graphLabelSetup(graphOE, Color.orange);
		graphLabelSetup(graphPriority, Color.white);
		graphLabelSetup(graphIdle, Color.pink);
		frame.setTitle("Simulation");
		frame.setResizable(false);
		addKeyListener(new KAdapter());
		setOpaque(false);
		setFocusable(true); // very important, allows keylistener?

		frame.setLocationRelativeTo(null);
//		frame.setLocation(0, 0);

		frame.add(this);
		frame.setVisible(true);
	}

	private void setup() {
		for (int i = 0; i < data.length; i++)
			data[i] = new ArrayList<Double>();
		for (int r = 0; r < dimx; r++)
			for (int c = 0; c < dimy; c++)
				foodMap[r][c] = new Food();
		for (int r = 0; r < dimx; r++)
			for (int c = 0; c < dimy; c++)
				eater1Map[r][c] = 0;
		for (int r = 0; r < dimx; r++)
			for (int c = 0; c < dimy; c++)
				eater2Map[r][c] = 0;
		for (int i = 0; i < pop1Size; i++)
			if (mutate1)
				pop1.add(new Eater1(-1, -1, defSpeed1 + rand.nextGaussian() * speedMutation1,
						defRange1 + rand.nextGaussian() * rangeMutation1, defOE1 + rand.nextGaussian() * OEMutation1,
						defPriority, defIdle1));
			else
				pop1.add(new Eater1(-1, -1, defSpeed1, defRange1, defOE1, defPriority, defIdle1));
		for (int i = 0; i < pop2Size; i++)
			if (mutate2)
				pop2.add(new Eater2(-1, -1, defSpeed2 + rand.nextGaussian() * speedMutation2,
						defRange2 + rand.nextGaussian() * rangeMutation2, defOE2 + rand.nextGaussian() * OEMutation2,
						defIdle2));
			else
				pop2.add(new Eater2(-1, -1, defSpeed2, defRange2, defOE2, defIdle2));
	}

	public void dayCycle() {
		int max = 0, min = pop1Size, trueMin = pop1Size, minDay = 1, trueMinDay = 1, maxDay = 1;
		int popCumSum = 0, food = 0, bgColor, leftovers, decomposed;
		do {
			if (!pause) {
				day++;
				tick = 0;
				food = 0;
				leftovers = 0;
				decomposed = 0;
				popCumSum += pop1Size;
				if (pop1Size > max) {
					maxDay = day;
					max = pop1Size;
				}
				if (pop1Size < min && pop1Size > 0) {
					minDay = day;
					min = pop1Size;
				}
				if (minDay != day) {
					trueMinDay = minDay;
					trueMin = min;
				}
				if (day == 1)
					decomposed = initialFood; // starting food

				// check for rotten food
				for (Food[] row : foodMap)
					for (Food f : row) {
						if (stableFood)
							f.reset();
						else if (f.getValue() == 1) {
							f.age();
							if (f.getAge() >= 3) {
								decomposed++;
								f.reset();
							} else
								leftovers++;
						}
					}

				// plant food
				if (stableFood)
					food = plantFood(food, (int) Math.round(foodGrowth), leftovers, decomposed);
				else // (amount planted each tick decreases linearly to zero)
					food = plantFood(food, (int) Math.round(foodGrowth / dayLength * 2), leftovers, decomposed);

				// reset everyone
				for (Eater1 b : pop1)
					b.newDay();
				for (Eater2 b : pop2) {
					b.newDay();
				}

				if (debug) {
					System.out.println("\nDay " + day + " Population: " + pop1Size);
					System.out.println("Food available: " + food);
				}

				updateGraphs(food);

				if ((slow == 0 && day % 10 == 0) || slow != 0) {
					repaint();
					if (slow != 1)
						delay(tickSpeed);
					else
						delay(tickSpeed / 10);
				}
				if (slow == 3) {
					frame.getContentPane().setBackground(new Color(30, 30, 30));
					for (int wait = 0; wait < 10; wait++) {
						if (slow < 3)
							wait = 10;
						else
							delay(tickSpeed);
					}
				} else
					frame.getContentPane().setBackground(Color.lightGray);

				// actual day
				while (tick < dayLength) {
					if (!pause) {
						if (slow == 3) {
							player1.setCM(true);
							player2.setCM(true);
							bgColor = (int) (noonBrightness - Math.abs(tick - (double) dayLength / 2)
									/ ((double) dayLength / 2) * (noonBrightness - 50));
							frame.getContentPane().setBackground(new Color(bgColor, bgColor, bgColor));
						}
						move();
						food = eat(food);
						if (slow == 3) {
							if (playerCount >= 1)
								checkAte(player1);
							if (playerCount >= 2)
								checkAte(player2);
							repaint();
							delay(tickSpeed);
						}
						tick++;
						if (!stableFood)
							food = plantFood(food,
									(int) Math.round(foodGrowth / dayLength * 2 * (1 - tick / (dayLength + 1.0))), 0,
									0);
					} else
						delay(10);
				}

				survive();
			} else {
				delay(10);
			}
		} while ((((pop1Size > 0 || pop2Size > 0) && !learning) || (pop2Size > 0 && learning)) && day < dayLimit);

		if (!learning) {
			if (pop1Size == 0)
				System.out.println("\nExtinction on day " + day);
			else if (day == dayLimit)
				System.out.println("Timeout");
			System.out.println(
					"\nAverage Population Size: " + new DecimalFormat("#.##").format((double) popCumSum / day));
			System.out.println("Max Population Size: " + max + " on day " + maxDay);
			System.out.println("Min Population Size: " + trueMin + " on day " + trueMinDay);
			if (showGraph) {
				leftovers = 0;
				decomposed = 0;
				for (Food[] row : foodMap)
					for (Food f : row)
						if (f.getValue() == 1)
							if (f.getAge() < 3)
								leftovers++;
				if (stableFood)
					updateGraphs((int) foodGrowth);
				else
					updateGraphs(leftovers);
				pause = true;
			}
			if (!autoRestart)
				while (pause)
					repaint();
					delay(10);
			frame.dispose();
			if (autoRestart)
				new World(false, false, 0, 0, 0, 0, 0, 0, 0, 0);
		} else {
//			frame.dispose();
		}
	}

	private void updateGraphs(int food) {
		double[] avg1 = new double[5];
		double[] avg2 = new double[4];
		Arrays.fill(avg1, 0); // speedAvg, rangeAvg, OEAvg, priAvg, idleAvg;
		Arrays.fill(avg2, 0); // speedAvg, rangeAvg, OEAvg, idleAvg;
		for (Eater1 b : pop1) {
			avg1[0] += b.getSpeed();
			avg1[1] += b.getRange();
			avg1[2] += b.getOE();
			avg1[3] += b.getPriority();
			avg1[4] += b.getIdle();
		}
		for (Eater2 b : pop2) {
			avg2[0] += b.getSpeed();
			avg2[1] += b.getRange();
			avg2[2] += b.getOE();
			avg2[3] += b.getIdle();
		}
		if (pop1Size > 0)
			for (int i = 0; i < avg1.length; i++)
				avg1[i] /= pop1Size;
		if (pop2Size > 0)
			for (int i = 0; i < avg2.length; i++)
				avg2[i] /= pop2Size;
		if ((slow == 0 && day % 10 == 0) || slow != 0) {
			if (data[0].size() > graphX / graphXScale) {
				for (ArrayList<Double> array : data)
					array.remove(0);
			}
			data[0].add((double) pop1Size);
			data[1].add((double) pop2Size);
			data[2].add((double) food);
			for (int i = 0; i < avg1.length; i++)
				data[i + 3].add(avg1[i]);
			for (int i = 0; i < avg2.length; i++)
				data[i + 8].add(avg2[i]);
		}
	}

	private int plantFood(int food, int growth, int leftovers, int decomposed) {
		int x, y;
		double rand1 = 0.4 * Math.random() + 0.8;
		double rand2 = Math.random() + 0.5;
		if (stableFood) {
			food = growth;
		} else
			food = (int) (growth * rand1 + decomposed * rand2);
		if (food + leftovers > dimx * dimy * 0.5)
			food = (int) (0.5 * dimx * dimy) - leftovers;
		for (int f = 0; f < food;) {
			x = (int) (dimx * Math.random());
			y = (int) (dimy * Math.random());
			if (foodMap[x][y].getValue() == 0) {
				foodMap[x][y].reset();
				foodMap[x][y].setValue(1);
				f++;
			}
		}
		return food + leftovers;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void paintComponent(Graphics g) {
//		super.paintComponent(g);

		if (showBoard) {
			// draw playing board
			for (int r = 0; r < dimy; r++)
				for (int c = 0; c < dimx; c++) {
					if (foodMap[c][r].getValue() > 0) {
						switch (foodMap[c][r].getAge()) {
						case 0:
							g.setColor(new Color(255, 230, 0));
							break;
						case 1:
							g.setColor(new Color(255, 255, 0));
							break;
						case 2:
							g.setColor(new Color(255, 255, 150));
							break;
						default:
							g.setColor(Color.cyan);
						}
						g.fillRect(c * PIXEL, r * PIXEL, PIXEL, PIXEL);
					}
				}
			for (int j = 0; j < pop1.size(); j++) // draw rabbits
				if (pop1.get(j).died()) {
					pop1.remove(j);
					pop1Size--;
					j--;
					System.out.println("unexpected death");
				} else {
					if (pop1.get(j).getHunger() < 1)
						g.setColor(Color.red);
					else if (pop1.get(j).getHunger() < 2)
						g.setColor(new Color(102, 0, 153)); // RGB values for purple
					else if (pop1.get(j).getHunger() < foodCap1)
						g.setColor(Color.blue);
					else
						g.setColor(Color.cyan);
					g.fillRect(pop1.get(j).getX() * PIXEL, pop1.get(j).getY() * PIXEL, PIXEL, PIXEL);
					if (pop1.get(j).getEnergy() <= 0) {
						g.setColor(Color.black);
						g.fillRect(pop1.get(j).getX() * PIXEL, pop1.get(j).getY() * PIXEL + PIXEL / 3, PIXEL,
								PIXEL / 3);
					}
				}
			for (int j = 0; j < pop2.size(); j++) { // draw foxes
				if (pop2.get(j).getHunger() < pop2.get(j).getFoodMin())
					g.setColor(Color.red);
				else if (pop2.get(j).getIncubation() >= 1)
					g.setColor(Color.orange);
				else
					g.setColor(Color.pink);
				g.fillRect(pop2.get(j).getX() * PIXEL, pop2.get(j).getY() * PIXEL, PIXEL * 2, PIXEL * 2);
				if (pop2.get(j).getEnergy() <= 0) {
					g.setColor(Color.black);
					g.fillRect(pop2.get(j).getX() * PIXEL, pop2.get(j).getY() * PIXEL + PIXEL * 2 / 3, PIXEL * 2,
							PIXEL * 2 / 3);
				}
			}
		}
		// draw graph
		double max;
		if (showGraph) {
			graphDate.setText("Day " + String.valueOf(day));
			// re-scaling graphs
			switch (graphData) {
			case 0:
				graphYScale = lineGraphRescaleY(graphYScale, new ArrayList[] { data[0], data[1], data[2] });
				break;
			case 1:
				graphYScale = lineGraphRescaleY(graphYScale, new ArrayList[] { data[3], data[4], data[5] });
				break;
			case 2:
				graphYScale = lineGraphRescaleY(graphYScale, new ArrayList[] { data[8], data[9], data[10] });
			}
			max = 0;
			for (Double a : data[3])
				if (a > max)
					max = a;
			if (max * 1.2 > plotYMax)
				plotYMax++;
			else if (max * 1.6 < plotYMax && plotYMax > 20)
				plotYMax--;
			for (Double a : data[4])
				if (a > max)
					max = a;
			if (max * 1.2 > plotXMax)
				plotXMax++;
			else if (max * 1.6 < plotXMax && plotXMax > 20)
				plotXMax--;

			g.setColor(Color.black);
			g.fillRect(frameX, 0, graphX + graphBorder * 2, frameY); // clear graph area
//			g.fillRect(frameX + graphBorder, graphBorder, dataXDim, frameY - 2 * graphBorder); // just the graph
			g.setColor(Color.lightGray); // draw axes
			g.drawLine(frameX + graphBorder, graphBorder, frameX + graphBorder, graphY + graphBorder);
			g.drawLine(frameX + graphBorder + graphX, graphBorder, frameX + graphBorder + graphX, graphY + graphBorder);
			g.drawLine(frameX + graphBorder, graphY + graphBorder, frameX + graphBorder + graphX, graphY + graphBorder);
			switch (graphData) {
			case 0: // overall population line graph
				dataTitle.setText("Population vs. Time (~" + foodGrowth + " food every day) Mode: " + slow);
				g.drawLine(frameX + graphBorder, (int) (graphBorder + graphY * 0.1), frameX + graphBorder + graphX,
						(int) (graphBorder + graphY * 0.1));
				g.drawLine(frameX + graphBorder, (int) (graphBorder + graphY * 0.55), frameX + graphBorder + graphX,
						(int) (graphBorder + graphY * 0.55));
				for (int i = 0; i < data[0].size() - 1; i++) {
					g.setColor(Color.cyan);
					lineGraphNextPoint(g, i, data[0], graphYScale);
					g.setColor(Color.pink);
					lineGraphNextPoint(g, i, data[1], graphYScale);
					g.setColor(Color.yellow);
					lineGraphNextPoint(g, i, data[2], graphYScale);
//					g.setColor(Color.blue);
//					lineGraphNextPoint(g, i, speedGraph, graphYScale1);
//					g.setColor(Color.green);
//					lineGraphNextPoint(g, i, rangeGraph, graphYScale1);
//					g.setColor(Color.orange);
//					lineGraphNextPoint(g, i, OEGraph, graphYScale1);
				}

				lineGraphUpdateLabels(graphPop1, data[0], graphYScale, "#");
				lineGraphUpdateLabels(graphPop2, data[1], graphYScale, "#");
				lineGraphUpdateLabels(graphFood, data[2], graphYScale, "#");
				graphYLeftAxisTop.setText(String.valueOf((int) Math.round(graphY / graphYScale * 0.9)));
				graphYLeftAxisMid.setText(String.valueOf((int) Math.round(graphY / graphYScale * 0.45)));
				graphYLeftAxisMid.setBounds(frameX + graphBorder * 2 / 5, (int) (graphBorder + graphY * 5.5 * 0.1 - 6),
						graphBorder / 2, 12);
				graphYLeftAxisBot.setText("0");
				graphYLeftAxisBot.setBounds(frameX + graphBorder * 3 / 5, graphY + graphBorder - 6, graphBorder / 2,
						12);
//				graphYRightAxisTop.setText(new DecimalFormat("#.##").format(graphY / graphYScale1 * 0.9));
//				graphYRightAxisMid.setText(new DecimalFormat("#.##").format(graphY / graphYScale1 * 0.45));
//				graphYRightAxisBot.setText("0");
				graphDate.setBounds(frameX + data[0].size() * graphXScale + graphBorder / 2, graphY + graphBorder + 10,
						60, 20);
				break;
			case 1: // rabbit population averages graph
				dataTitle.setText(
						"Rabbit Population Traits vs. Time (~" + foodGrowth + " food every day) Mode: " + slow);
				g.drawLine(frameX + graphBorder, (int) (graphBorder + graphY * 0.1), frameX + graphBorder + graphX,
						(int) (graphBorder + graphY * 0.1));
				g.drawLine(frameX + graphBorder, (int) (graphBorder + graphY * 0.55), frameX + graphBorder + graphX,
						(int) (graphBorder + graphY * 0.55));
				for (int i = 0; i < data[0].size() - 1; i++) {
					g.setColor(Color.blue);
					lineGraphNextPoint(g, i, data[3], graphYScale);
					g.setColor(Color.green);
					lineGraphNextPoint(g, i, data[4], graphYScale);
					g.setColor(Color.orange);
					lineGraphNextPoint(g, i, data[5], graphYScale);
					g.setColor(Color.white);
					lineGraphNextPoint(g, i, data[6], graphYScale);
					g.setColor(Color.pink);
					lineGraphNextPoint(g, i, data[7], graphYScale);
				}
				lineGraphUpdateLabels(graphSpeed, data[3], graphYScale, "#.##");
				lineGraphUpdateLabels(graphRange, data[4], graphYScale, "#.##");
				lineGraphUpdateLabels(graphOE, data[5], graphYScale, "#.##");
				lineGraphUpdateLabels(graphPriority, data[6], graphYScale, "#.##");
				lineGraphUpdateLabels(graphIdle, data[7], graphYScale, "#.##");
				graphYLeftAxisTop.setText(new DecimalFormat("#.##").format(graphY / graphYScale * 0.9));
				graphYLeftAxisMid.setText(new DecimalFormat("#.##").format(graphY / graphYScale * 0.45));
				graphYLeftAxisMid.setBounds(frameX + graphBorder * 2 / 5, (int) (graphBorder + graphY * 5.5 * 0.1 - 6),
						graphBorder / 2, 12);
				graphYLeftAxisBot.setText("0");
				graphYLeftAxisBot.setBounds(frameX + graphBorder * 3 / 5, graphY + graphBorder - 6, graphBorder / 2,
						12);
				graphDate.setBounds(frameX + data[0].size() * graphXScale + graphBorder / 2, graphY + graphBorder + 10,
						60, 20);
				break;
			case 2: // fox line graph
				dataTitle.setText("Fox Population Traits vs. Time (~" + foodGrowth + " food every day) Mode: " + slow);
				g.drawLine(frameX + graphBorder, (int) (graphBorder + graphY * 0.1), frameX + graphBorder + graphX,
						(int) (graphBorder + graphY * 0.1));
				g.drawLine(frameX + graphBorder, (int) (graphBorder + graphY * 0.55), frameX + graphBorder + graphX,
						(int) (graphBorder + graphY * 0.55));
				for (int i = 0; i < data[0].size() - 1; i++) {
					g.setColor(Color.blue);
					lineGraphNextPoint(g, i, data[8], graphYScale);
					g.setColor(Color.green);
					lineGraphNextPoint(g, i, data[9], graphYScale);
					g.setColor(Color.orange);
					lineGraphNextPoint(g, i, data[10], graphYScale);
					g.setColor(Color.pink);
					lineGraphNextPoint(g, i, data[11], graphYScale);
				}
				lineGraphUpdateLabels(graphSpeed, data[8], graphYScale, "#.##");
				lineGraphUpdateLabels(graphRange, data[9], graphYScale, "#.##");
				lineGraphUpdateLabels(graphOE, data[10], graphYScale, "#.##");
				lineGraphUpdateLabels(graphIdle, data[11], graphYScale, "#.##");
				graphYLeftAxisTop.setText(new DecimalFormat("#.##").format(graphY / graphYScale * 0.9));
				graphYLeftAxisMid.setText(new DecimalFormat("#.##").format(graphY / graphYScale * 0.45));
				graphYLeftAxisMid.setBounds(frameX + graphBorder * 2 / 5, (int) (graphBorder + graphY * 5.5 * 0.1 - 6),
						graphBorder / 2, 12);
				graphYLeftAxisBot.setText("0");
				graphYLeftAxisBot.setBounds(frameX + graphBorder * 3 / 5, graphY + graphBorder - 6, graphBorder / 2,
						12);
				graphDate.setBounds(frameX + data[0].size() * graphXScale + graphBorder / 2, graphY + graphBorder + 10,
						60, 20);
				break;
			case 3: // plot point graph
				dataTitle.setText("Speed vs. Range (~" + foodGrowth + " food every day) Mode: " + slow);
				g.setColor(Color.lightGray);
				g.drawLine(frameX + graphBorder, graphBorder, frameX + graphBorder + graphX, graphBorder);
				graphDate.setBounds(frameX + graphX + graphXScale + graphBorder / 2, graphY + graphBorder + 10, 60, 20);
				g.setColor(Color.cyan);
				for (Eater1 e : pop1)
					g.fillRect((int) (frameX + graphBorder + (e.getRange() - plotXMin) * (graphX / plotXMax)) - 2,
							(int) (graphBorder + graphY - (e.getSpeed() - plotYMin) * (graphY / plotYMax)) - 2, 4, 4);
				g.setColor(Color.pink);
				for (Eater2 e : pop2)
					g.fillRect((int) (frameX + graphBorder + (e.getRange() - plotXMin) * (graphX / plotXMax)) - 2,
							(int) (graphBorder + graphY - (e.getSpeed() - plotYMin) * (graphY / plotYMax)) - 2, 4, 4);
				graphXAxisTop.setText(new DecimalFormat("#.##").format(plotXMax));
				graphXAxisBot.setText(new DecimalFormat("#.##").format(plotXMin));
				graphYLeftAxisTop.setText(new DecimalFormat("#.##").format(plotYMax));
				graphYLeftAxisMid.setText(new DecimalFormat("#.##").format((plotYMax + plotYMin) / 2));
				graphYLeftAxisMid.setBounds(frameX + graphBorder * 3 / 10, (int) (graphBorder + graphY * 5.5 * 0.1 - 6),
						graphBorder * 3 / 5, 12);
				graphYLeftAxisBot.setText(new DecimalFormat("#.##").format(plotYMin));
				break;
			}
		}
		if (slow >= 3) {
			if (playerCount >= 1) {
				g.setColor(Color.white);
				g.fillRect(player1.getX() * PIXEL, player1.getY() * PIXEL, player1.getBigness() * PIXEL,
						player1.getBigness() * PIXEL);
				g.setColor(Color.black);
				g.fillRect((int) (player1.getX() * PIXEL + player1.getBigness() * PIXEL * 0.4),
						(int) (player1.getY() * PIXEL + player1.getBigness() * PIXEL * 0.1),
						(int) (player1.getBigness() * PIXEL * 0.2), (int) (player1.getBigness() * PIXEL * 0.8));
			}
			if (playerCount >= 2) {
				g.setColor(Color.white);
				g.fillRect(player2.getX() * PIXEL, player2.getY() * PIXEL, player2.getBigness() * PIXEL,
						player2.getBigness() * PIXEL);
				g.setColor(Color.black);
				g.fillRect((int) (player2.getX() * PIXEL + player2.getBigness() * PIXEL * 0.2),
						(int) (player2.getY() * PIXEL + player2.getBigness() * PIXEL * 0.1),
						(int) (player2.getBigness() * PIXEL * 0.2), (int) (player2.getBigness() * PIXEL * 0.8));
				g.fillRect((int) (player2.getX() * PIXEL + player2.getBigness() * PIXEL * 0.6),
						(int) (player2.getY() * PIXEL + player2.getBigness() * PIXEL * 0.1),
						(int) (player2.getBigness() * PIXEL * 0.2), (int) (player2.getBigness() * PIXEL * 0.8));
			}
		}
	}

	private void lineGraphNextPoint(Graphics g, int i, ArrayList<Double> drawGraph, double scaleY) {
		g.fillRect(frameX + graphBorder + (i + 1) * graphXScale - 1,
				(int) (graphY + graphBorder - drawGraph.get(i + 1) * scaleY) - 1, 2, 2);
		g.drawLine(frameX + graphBorder + i * graphXScale, (int) (graphY + graphBorder - drawGraph.get(i) * scaleY),
				frameX + graphBorder + (i + 1) * graphXScale,
				(int) (graphY + graphBorder - drawGraph.get(i + 1) * scaleY));
	}

	private double lineGraphRescaleY(double scale, ArrayList<Double>[] set) {
		double max = 0;
		for (ArrayList<Double> s : set)
			for (Double a : s)
				if (a > max)
					max = a;
		if (max > 0)
			if (Math.abs(graphY / max * 0.9 - scale) < 0.7)
				return graphY / max * 0.9;
			else
				return scale + (graphY / max * 0.9 - scale) * 0.7;
		return 0;
	}

	private void lineGraphUpdateLabels(JLabel l, ArrayList<Double> s, double scale, String format) {
		l.setText(new DecimalFormat(format).format(s.get(s.size() - 1)));
		l.setBounds(s.size() * graphXScale + frameX + graphBorder,
				(int) (graphY + graphBorder - s.get(s.size() - 1) * scale - 10), 50, 20);
	}

	private void move() {
		int[] target = new int[2];
		int[] rip = new int[] { 0, 0 };
		int randx, randy, randRatio;
		Eater1 b;
		for (int k = 0; k < pop1Size; k++) {
			b = pop1.get(k);
			if (eater1Map[b.getX()][b.getY()] == 0) {
				pop1.remove(k);
				pop1Size--;
				k--;
			} else {
				if (foodMap[b.getX()][b.getY()].getValue() == 0
						|| (foodMap[b.getX()][b.getY()].getValue() > 0 && !b.eatMore())) {
					if (b.getEnergy() > 0) {
						target = b.scanArea();
						b.useEnergy(b.getRange()); // costs energy to scan
						if (!b.eatMore() || Arrays.equals(target, rip)) {
							if (b.getIdle() == 1) {
								randx = (int) (2 * Math.round(Math.random()) - 1);
								randy = (int) (2 * Math.round(Math.random()) - 1);
								randRatio = (int) ((b.getEffectiveSpeed() + 1) * Math.random());
								b.move(randx * randRatio, randy * (b.getEffectiveSpeed() - randRatio));
								b.useEnergy(b.getSpeed() * 4);
							} else
								b.move(0, 0);
						} else { // move toward food
							for (int i = 0; i < b.getEffectiveSpeed(); i++) {
								if (target[0] != 0 || target[1] != 0)
									if (Math.abs(target[0]) > Math.abs(target[1])) {
										b.move(target[0] / Math.abs(target[0]), 0);
										target[0] -= target[0] / Math.abs(target[0]);
									} else if (Math.abs(target[1]) > 0) {
										b.move(0, target[1] / Math.abs(target[1]));
										target[1] -= target[1] / Math.abs(target[1]);
									}
								b.useEnergy(4); // cost to move 1 tile
							}
						}
					}
				}
				if (foodMap[b.getX()][b.getY()].getValue() > 0 && b.eatMore())
					foodMap[b.getX()][b.getY()].plusOne(); // stake claim on food
			}
		}
		for (Eater2 f : pop2) {
			if (f.atFood() == 0 || (f.atFood() > 0 && !f.eatMore())) {
				if (f.getEnergy() > 0) {
					target = f.scanArea();
					f.useEnergy(f.getRange());
					if (!f.eatMore() || Arrays.equals(target, rip)) {
						if (f.getIdle() == 1) {
							randx = (int) (2 * Math.round(Math.random()) - 1);
							randy = (int) (2 * Math.round(Math.random()) - 1);
							randRatio = (int) ((f.getEffectiveSpeed() + 1) * Math.random());
							f.move(randx * randRatio, randy * (f.getEffectiveSpeed() - randRatio));
							f.useEnergy(f.getSpeed() * 4);
						} else
							f.move(0, 0);
					} else { // move toward food
						for (int i = 0; i < f.getEffectiveSpeed(); i++) {
							if (target[0] != 0 || target[1] != 0)
								if (Math.abs(target[0]) > Math.abs(target[1])) {
									f.move(target[0] / Math.abs(target[0]), 0);
									target[0] -= target[0] / Math.abs(target[0]);
								} else if (Math.abs(target[1]) > 0) {
									f.move(0, target[1] / Math.abs(target[1]));
									target[1] -= target[1] / Math.abs(target[1]);
								}
							f.useEnergy(4);
						}
					}
				}
			}
		}
	}

	private int eat(int food) {
		for (Eater1 b : pop1) {
			if (foodMap[b.getX()][b.getY()].getValue() > 0 && b.eatMore()) // food
				b.eat(1 / (foodMap[b.getX()][b.getY()].getValue() - 1));
		}
		for (Eater2 b : pop2) {
			if (b.atFood() > 0 && b.eatMore()) { // food
				b.eat(b.atFood());
				eater1Map[b.getX()][b.getY()] = 0;
				eater1Map[(b.getX() + 1 + dimx) % dimx][b.getY()] = 0;
				eater1Map[(b.getX() + 1 + dimx) % dimx][(b.getY() + 1 + dimy) % dimy] = 0;
				eater1Map[b.getX()][(b.getY() + 1 + dimy) % dimy] = 0;
			}
		}
		for (int r = 0; r < dimx; r++)
			for (int c = 0; c < dimy; c++)
				if (foodMap[r][c].getValue() > 1) { // already eaten
					foodMap[r][c].reset();
					food--;
				}
		return food;
	}

	private void survive() {
		int nextGen1 = 0, nextGen2 = 0, surv;
		int born1 = 0, DBS1 = 0, DBA1 = 0, born2 = 0, DBS2 = 0, DBA2 = 0;
		Eater1 e1;
		Eater2 e2;
		for (int i = pop1.size() - 1; i >= 0; i--) {
			e1 = pop1.get(i);
			surv = pop1.get(i).trial();
			if (surv == -1) { // death by old age
				eater1Map[e1.getX()][e1.getY()] = 0;
				pop1.remove(i);
				DBA1++;
			} else if (surv == 0) {
				eater1Map[e1.getX()][e1.getY()] = 0;
				pop1.remove(i);
				DBS1++;
			} else if (surv == 1)
				nextGen1++;
			else if (surv == 2) {
				nextGen1 += 1 + litterCount;
				for (int k = 0; k < litterCount; k++) {
					born1++;
					if (mutate1)
						pop1.add(new Eater1(
								(e1.getX() + (int) (3 * e1.getRange() * Math.random() - e1.getRange() / 2) + dimx)
										% dimx,
								(e1.getY() + (int) (3 * e1.getRange() * Math.random() - e1.getRange() / 2) + dimy)
										% dimy,
								e1.getSpeed() + rand.nextGaussian() * speedMutation1,
								e1.getRange() + rand.nextGaussian() * rangeMutation1,
								e1.getOE() + rand.nextGaussian() * OEMutation1, e1.getPriority(), e1.getIdle()));
					else
						pop1.add(new Eater1(
								(e1.getX() + (int) (3 * e1.getRange() * Math.random() - e1.getRange() / 2) + dimx)
										% dimx,
								(e1.getY() + (int) (3 * e1.getRange() * Math.random() - e1.getRange() / 2) + dimy)
										% dimy,
								defSpeed1, defRange1, e1.getOE(), e1.getPriority(), e1.getIdle()));
				}
			}
		}
		pop1Size = nextGen1;
		for (int i = pop2.size() - 1; i >= 0; i--) {
			e2 = pop2.get(i);
			surv = pop2.get(i).trial();
			nextGen2 += surv;
			if (surv == -1) { // death by old age
				nextGen2++;
				pop2.remove(i);
				DBA2++;
			} else if (surv == 0) {
				pop2.remove(i);
				DBS2++;
			} else if (surv == 2) {
				born2++;
				if (mutate2)
					pop2.add(new Eater2(
							(e2.getX() + (int) (3 * e2.getRange() * Math.random() - e2.getRange() / 2) + dimx) % dimx,
							(e2.getY() + (int) (3 * e2.getRange() * Math.random() - e2.getRange() / 2) + dimy) % dimy,
							e2.getSpeed() + rand.nextGaussian() * speedMutation2,
							e2.getRange() + rand.nextGaussian() * rangeMutation2,
							e2.getOE() + rand.nextGaussian() * OEMutation2, e2.getIdle()));
				else
					pop2.add(new Eater2(
							(e2.getX() + (int) (3 * e2.getRange() * Math.random() - e2.getRange() / 2) + dimx) % dimx,
							(e2.getY() + (int) (3 * e2.getRange() * Math.random() - e2.getRange() / 2) + dimy) % dimy,
							defSpeed2, defRange2, e2.getOE(), e2.getIdle()));
			}
		}
		pop2Size = nextGen2;
		if (debug) {
			System.out.print(born1 + " rabbits born. ");
			System.out.println((DBA1 + DBS1) + " rabbits died (" + DBA1 + " by age, " + DBS1 + " starved).");
			System.out.print(born2 + " foxes born. ");
			System.out.println((DBA2 + DBS2) + " rabbits died (" + DBA2 + " by age, " + DBS2 + " starved).");
		}
	}

	private void checkAte(Player p) {
		for (Eater1 b : pop1)
			if (b.getX() - p.getX() >= 0 && b.getX() - p.getX() < p.getBigness() && b.getY() - p.getY() >= 0
					&& b.getY() - p.getY() < p.getBigness()) {
				b.kill();
				p.grow(b.getHunger() / 10.0);
			}
	}

	private void delay(int d) {
		try {
			Thread.sleep(d);
		} catch (InterruptedException e) {
			System.out.println("Exception occurred");
			System.exit(1);
		}
	}

	private void graphLabelSetup(JLabel l, Color c) {
		l.setForeground(c);
		l.setOpaque(false);
		frame.add(l);
	}

	public int ended() {
		if (pop2.size() > 0)
			return 0;
		else
			return day;
	}

	private class KAdapter extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_B:
				if (showBoard) {
					showBoard = false;
					frameX = 0;
					frame.setSize(graphX + graphBorder * 2 + 14, frameY + 37);
				} else {
					showBoard = true;
					frameX = frameXFinal;
					frame.setSize(frameX + 14 + graphX + graphBorder * 2, frameY + 37);
				}
				break;
			case KeyEvent.VK_G:
				switch (graphData) {
				case 0: // population
					graphPop1.setVisible(false);
					graphPop2.setVisible(false);
					graphFood.setVisible(false);
					graphSpeed.setVisible(true);
					graphRange.setVisible(true);
					graphOE.setVisible(true);
					graphPriority.setVisible(true);
					graphIdle.setVisible(true);
					break;
				case 1: // rabbit traits
					graphPriority.setVisible(false);
					break;
				case 2: // fox traits
					graphSpeed.setVisible(false);
					graphRange.setVisible(false);
					graphOE.setVisible(false);
					graphIdle.setVisible(false);
					graphXAxisTop.setVisible(true);
					graphXAxisBot.setVisible(true);
					break;
				case 3: // plot point graph
					graphXAxisTop.setVisible(false);
					graphXAxisBot.setVisible(false);
					graphPop1.setVisible(true);
					graphPop2.setVisible(true);
					graphFood.setVisible(true);
				}
				graphData = (graphData + 1) % 4;
				break;
			case KeyEvent.VK_H:
				if (showGraph) {
					showGraph = false;
					frame.setSize(frameX + 14, frameY + 37);
				} else {
					showGraph = true;
					frame.setSize(frameX + 14 + graphX + graphBorder * 2, frameY + 37);
				}
				break;
			case KeyEvent.VK_M:
				foodGrowth += 10;
				break;
			case KeyEvent.VK_N:
				if (foodGrowth > 10)
					foodGrowth -= 10;
				break;
			case KeyEvent.VK_O:
				break;
			case KeyEvent.VK_P:
				break;
			case KeyEvent.VK_0:
				slow = 0;
				break;
			case KeyEvent.VK_1:
				slow = 1;
				break;
			case KeyEvent.VK_2:
				slow = 2;
				break;
			case KeyEvent.VK_3:
				slow = 3;
				break;
			case KeyEvent.VK_ENTER:
				pause = !pause;
				autoRestart = true;
				break;
			case KeyEvent.VK_SPACE:
				pause = !pause;
				break;
			case KeyEvent.VK_UP:
				player1.move(0, -1 * player1.getBigness());
				break;
			case KeyEvent.VK_DOWN:
				player1.move(0, player1.getBigness());
				break;
			case KeyEvent.VK_LEFT:
				player1.move(-1 * player1.getBigness(), 0);
				break;
			case KeyEvent.VK_RIGHT:
				player1.move(player1.getBigness(), 0);
				break;
			case KeyEvent.VK_W:
				player2.move(0, -1 * player2.getBigness());
				break;
			case KeyEvent.VK_S:
				player2.move(0, player2.getBigness());
				break;
			case KeyEvent.VK_A:
				player2.move(-1 * player2.getBigness(), 0);
				break;
			case KeyEvent.VK_D:
				player2.move(player2.getBigness(), 0);
				break;
			}
		}
	}
}