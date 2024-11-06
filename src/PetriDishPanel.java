import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

public class PetriDishPanel extends JPanel {

	// debug
	private boolean debug = true;
	private final int figures = 3;

	// screen
	private int screenWidth, screenHeight;

	// engine
	private double targetFPS = 59.95;
	private long targetTime = (long) (1000 / targetFPS); // ms
	private int totalFrames = 0;
	private long totalTime = 0; // ms
	private long frameTime = 0, waitTime = 0; // ms
	private double currentFPS = 0;
	private boolean limit = true;
	private boolean pause = true;
	private ArrayList<Double> FPShistory = new ArrayList<Double>();

	// dish
	private int r, buffer = 50, osize = 5;
	private PetriDish dish;

	// inputs
	private KeyHandler keyHandler; // key inputs
	private MouseHandler mouseHandler; // mouse inputs
	private double mouseX = 0;
	private double mouseY = 0;

	public PetriDishPanel(double screenWidth, double screenHeight) {
		// screen
		this.screenWidth = (int) screenWidth;
		this.screenHeight = (int) screenHeight;

		// dish
		dish = new PetriDish();
		r = (this.screenHeight - buffer * 2) / 2;

		// inputs
		this.keyHandler = new KeyHandler();
		addKeyListener(this.keyHandler);
		setFocusable(true);
		this.mouseHandler = new MouseHandler();
		addMouseListener(this.mouseHandler);
	}

	public void paintComponent(Graphics graphics) {
		// anti-aliasing
		Graphics2D g = (Graphics2D) graphics;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// new frame
		long startTime = System.currentTimeMillis();
		// fps calculation
		if (totalFrames >= targetFPS) {
			currentFPS = totalFrames / (totalTime / 1000.0);
			totalFrames = 0;
			totalTime = 0;
			// updating FPS history
			FPShistory.add(currentFPS);
			if (FPShistory.size() > 100)
				FPShistory.remove(0);
		}

		// background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, screenWidth, screenHeight);

		// dish
		paintDish(g);
		if (pause == false)
			dish.step(targetTime);

		// debug
		if (debug)
			paintDebug(g);

		// end frame (TOFIX: need more accurate timekeeping)
		totalFrames++;
		long endTime = System.currentTimeMillis();
		frameTime = endTime - startTime;
		waitTime = Math.max(targetTime - frameTime, 0);
		if (limit == true) {
			try {
				TimeUnit.MILLISECONDS.sleep(waitTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		endTime = System.currentTimeMillis();
		frameTime = endTime - startTime - 4;
		totalTime += frameTime;

		// next frame
		repaint();
	}

	private void paintDish(Graphics2D g) {
		// dish
		g.setColor(Color.WHITE);
		int x = screenWidth / 2 - r;
		int y = buffer;
		int w = r * 2;
		int h = r * 2;
		g.fillOval(x, y, w, h);

		// foods
		paintFoods(g);

		// organisms
		paintOrganisms(g);
	}

	// drawing foods
	private void paintFoods(Graphics2D g) {
		// center of dish
		int cx = screenWidth / 2;
		int cy = screenHeight / 2;

		g.setColor(Color.BLACK);
		ArrayList<Food> foods = dish.getFoods();
		for (Food food : foods) {
			int x = (int) Math.round(cx + r * food.getX()) - 1;
			int y = (int) Math.round(cy - r * food.getY()) - 1;
			int w = 2;
			int h = 2;
			g.drawOval(x, y, w, h);
		}

	}

	// drawing organisms
	private void paintOrganisms(Graphics2D g) {
		// center of dish
		int cx = screenWidth / 2;
		int cy = screenHeight / 2;

		ArrayList<Organism> organisms = dish.getOrganisms();
		for (Organism organism : organisms) {
			g.setColor(organism.getColor());
			int x = (int) Math.round(cx + r * organism.getSX()) - osize;
			int y = (int) Math.round(cy - r * organism.getSY()) - osize;
			int w = osize * 2;
			int h = osize * 2;
			g.fillOval(x, y, w, h);

			// paths
			ArrayList<double[]> path = organism.getPath();
			int size = path.size();
			for (int i = 0; i < size - 1; i++) {
				int x1 = (int) Math.round(cx + r * path.get(i)[0]);
				int y1 = (int) Math.round(cy - r * path.get(i)[1]);
				int x2 = (int) Math.round(cx + r * path.get(i + 1)[0]);
				int y2 = (int) Math.round(cy - r * path.get(i + 1)[1]);
				g.drawLine(x1, y1, x2, y2);
			}

			// bounds, FOV, and LOS
			if (debug) {
				// bounds
				g.setColor(Color.BLUE);
				g.drawRect(x, y, w, h);

				// dir
				double dir = organism.getDir();
				int x1 = (int) Math.round(x + osize + Math.cos(dir) * osize);
				int y1 = (int) Math.round(y + osize - Math.sin(dir) * osize);
				int x2 = (int) Math.round(x + osize + Math.cos(dir) * (osize + 50));
				int y2 = (int) Math.round(y + osize - Math.sin(dir) * (osize + 50));
				g.drawLine(x1, y1, x2, y2);

				// FOV
				double FOV = organism.getFOV();
				int x3 = (int) Math.round(x + osize + Math.cos(dir + FOV / 2) * osize);
				int y3 = (int) Math.round(y + osize - Math.sin(dir + FOV / 2) * osize);
				int x4 = (int) Math.round(x + osize + Math.cos(dir + FOV / 2) * (osize + 50));
				int y4 = (int) Math.round(y + osize - Math.sin(dir + FOV / 2) * (osize + 50));
				g.drawLine(x3, y3, x4, y4);
				int x5 = (int) Math.round(x + osize + Math.cos(dir - FOV / 2) * osize);
				int y5 = (int) Math.round(y + osize - Math.sin(dir - FOV / 2) * osize);
				int x6 = (int) Math.round(x + osize + Math.cos(dir - FOV / 2) * (osize + 50));
				int y6 = (int) Math.round(y + osize - Math.sin(dir - FOV / 2) * (osize + 50));
				g.drawLine(x5, y5, x6, y6);

				// selected-specific information
				if (organism == dish.getSelected()) {
					// LOS
					ArrayList<LOS> LOSs = organism.getLOSs();
					for (LOS los : LOSs) {
						// determining validity of sight
						g.setColor(Color.RED);
						if (los.withinFOV()) {
							if (los.isLeft())
								g.setColor(Color.YELLOW);
							else if (los.isRight())
								g.setColor(Color.GREEN);
						}

						// drawing line
						Food target = los.getTarget();
						int x7 = x + osize;
						int y7 = y + osize;
						int x8 = (int) Math.round(cx + r * target.getX());
						int y8 = (int) Math.round(cy - r * target.getY());
						g.drawLine(x7, y7, x8, y8);
					}

					// brain / neural network
					g.setColor(Color.WHITE);
					double[][] weights = organism.getBrain().getWeights();
					int layers = weights.length;
					int neurons = weights[0].length;
					for (int i = 0; i < layers; i++) {
						for (int j = 0; j < neurons; j++) {
							g.drawString(Double.toString(round(weights[i][j])), 1500 + i * 100, 50 + j * 100);
						}
					}
				}
			}
		}
		/*
		if (selected != null) {
			g.setColor(Color.BLACK);
			// box
			int x1 = (int) (selected.getSX() - selected.getR());
			int y1 = (int) (selected.getSY() - selected.getR());
			int l = (int) (selected.getR() * 2);
			int w = (int) (selected.getR() * 2);
			g.drawRect(x1, y1, l, w);
			// vector
			x1 = (int) selected.getSX();
			y1 = (int) selected.getSY();
			int x2 = x1 + (int) (50 * Math.cos(selected.getDir()));
			int y2 = y1 + (int) (50 * Math.sin(selected.getDir()));
			g.drawLine(x1, y1, x2, y2);
		}
		*/
	}

	private void paintDebug(Graphics2D g) {
		// gui
		g.setColor(Color.WHITE);
		// g.translate(screenWidth / -2, screenHeight / -2);

		// debug menu
		ArrayList<String> debugMenu = new ArrayList<String>();
		debugMenu.add("Debug Menu:");
		debugMenu.add("");
		debugMenu.add("Target FPS: " + targetFPS);
		debugMenu.add("Target Time (ms): " + targetTime);
		debugMenu.add("Total Frames: " + totalFrames);
		debugMenu.add("Frame Time: " + frameTime);
		debugMenu.add("Wait Time: " + waitTime);
		debugMenu.add("Total Time: " + totalTime);
		debugMenu.add("Current FPS: " + round(currentFPS));
		debugMenu.add("FPS Limit: " + limit);
		debugMenu.add("Paused: " + pause);
		debugMenu.add("");
		debugMenu.add("Last Click: " + mouseX + ", " + mouseY);
		Organism selected = dish.getSelected();
		if (selected != null) {
			debugMenu.add("Selected: " + selected);
			debugMenu.add("X: " + selected.getSX());
			debugMenu.add("Y: " + selected.getSY());
			// debugMenu.add("R: " + selected.getR());
			debugMenu.add("Direction: " + (selected.getDir() / Math.PI * 180));
			debugMenu.add("Path: " + selected.getPath().size());
			debugMenu.add("LOS: " + selected.getLOSs().size());
			debugMenu.add("Energy: " + selected.getEnergy());
		}
		debugMenu.add("");
		debugMenu.add("Buffer: " + buffer);
		debugMenu.add("Radius: " + r);
		debugMenu.add("");
		debugMenu.add("Organisms: " + dish.getOrganisms().size());
		debugMenu.add("Foods: " + dish.getFoods().size());
		// printing
		int size = debugMenu.size();
		for (int i = 0; i < size; i++)
			g.drawString(debugMenu.get(i), 10, 20 * (i + 1));

		// fps history (TOFIX: fix FPS calculations first)
		/*
		size = FPShistory.size();
		for (int i = 0; i < size - 1; i++) {
			int x1 = 10 + 3 * i;
			int y1 = (int) (screenHeight - 10 - Math.round(FPShistory.get(i)));
			int x2 = x1 + 3;
			int y2 = (int) (screenHeight - 10 - Math.round(FPShistory.get(i + 1)));
			g.drawLine(x1, y1, x2, y2);
		}
		*/

		// population count
		ArrayList<Integer> census = dish.getCensus();
		size = census.size();
		for (int i = 0; i < size - 1; i++) {
			int x1 = 10 + 3 * i;
			int y1 = (int) (screenHeight - 10 - census.get(i));
			int x2 = x1 + 3;
			int y2 = (int) (screenHeight - 10 - census.get(i + 1));
			g.drawLine(x1, y1, x2, y2);
		}
	}

	private double round(double num) {
		return Math.round(num * Math.pow(10, figures)) / Math.pow(10, figures);
	}

	// key inputs
	class KeyHandler extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				pause = !pause;
			}
		}

		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				System.exit(0);
			} else if (e.getKeyCode() == KeyEvent.VK_F3) {
				debug = !debug;
			} else if (e.getKeyCode() == KeyEvent.VK_R) {
				pause = true;
				dish.reset();
			} else if (e.getKeyCode() == KeyEvent.VK_U) {
				limit = !limit; // toggle FPS/sim limiter
			}
		}
	}

	// mouse inputs
	class MouseHandler extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			Point click = e.getLocationOnScreen();
			mouseX = click.getX();
			mouseY = click.getY();
			boolean select = false;
			ArrayList<Organism> organisms = dish.getOrganisms();
			for (Organism organism : organisms) {
				// calculating bounds
				int cx = screenWidth / 2;
				int cy = screenHeight / 2;
				int x = (int) Math.round(cx + r * organism.getSX()) - osize;
				int y = (int) Math.round(cy - r * organism.getSY()) - osize;
				int w = osize * 2;
				int h = osize * 2;
				Rectangle bounds = new Rectangle(x, y, w, h);

				// checking if click was within bounds
				if (bounds.contains(click)) {
					dish.setSelected(organism);
					select = true;
					break;
				}
			}
			if (select == false) // no organism was selected
				dish.setSelected(null);
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}
	}
}
