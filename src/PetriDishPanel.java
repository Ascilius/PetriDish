import java.awt.BasicStroke;
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

import javax.swing.JPanel;

import Organisms.Agar;
import Organisms.Brain;
import Organisms.Entity;
import Organisms.Organism;

public class PetriDishPanel extends JPanel {

	// debug
	private boolean DEBUG = true;
	private final int figures = 3;

	// screen
	private int screenWidth, screenHeight;
	private double censusWidthScale = 4;
	private double censusHeightScale;
	private BasicStroke REGULAR = new BasicStroke(1f);
	private BasicStroke THICCCC = new BasicStroke(10f);

	// engine
	private double targetFPS = 59.95;
	private long targetTime = (long) (1000 / targetFPS); // ms
	private int periodSteps = 0;
	private long periodTime = 0;
	private double currentFPS = 0;
	// private boolean limit = false; // FPS limiter
	private boolean pause = true;
	private ArrayList<Double> FPShistory = new ArrayList<Double>();
	private int historyLim = 100;

	// dish
	private int r, buffer = 50, osize = 3;
	private PetriDish dish;
	private boolean paths = false;

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
		
		censusHeightScale = (screenHeight - 20) / (4.0 * dish.na);
		// censusHeightScale = (double) dish.historyLimit / dish.na;
		if (DEBUG)
			System.out.printf("DEBUG: censusHeightScale: %f\n", censusHeightScale);
		
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

		// background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, screenWidth, screenHeight);

		// dish
		paintDish(g);
		if (pause == false)
			step();
		else {
			g.setColor(Color.RED);
			g.setStroke(THICCCC);
			g.drawRect(0, 0, screenWidth, screenHeight);
			
			g.setStroke(REGULAR);
		}

		// debug
		if (DEBUG)
			paintDebug(g);

		// end frame
		periodSteps++;
		
		long endTime = System.currentTimeMillis();
		periodTime += endTime - startTime;
		
		// TODO: fix
		/*
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
		*/
		
		// fps calculation (every 1s)
		if (periodTime >= 1000) {
			currentFPS = periodSteps / (periodTime / 1000.0);
			periodSteps = 0;
			periodTime = 0;
			
			// updating FPS history
			FPShistory.add(currentFPS);
			if (FPShistory.size() > historyLim)
				FPShistory.remove(0);
		}

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

		ArrayList<Agar> agars = dish.getAgars();
		for (Agar agar : agars) {
			int x = (int) Math.round(cx + r * agar.getX()) - 1;
			int y = (int) Math.round(cy - r * agar.getY()) - 1;
			int w = 2;
			int h = 2;
			g.setColor(agar.getColor());
			g.fillOval(x, y, w, h);
			g.setColor(Color.BLACK);
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
			int x = (int) Math.round(cx + r * organism.getX()) - osize;
			int y = (int) Math.round(cy - r * organism.getY()) - osize;
			int w = osize * 2;
			int h = osize * 2;
			g.setColor(organism.getColor());
			g.fillOval(x, y, w, h);
			g.setColor(Color.BLACK);
			g.drawOval(x, y, w, h);

			// paths
			if (paths) {
				g.setColor(organism.getColor());
				ArrayList<double[]> path = organism.getPath();
				int size = path.size();
				for (int i = 0; i < size - 1; i++) {
					int x1 = (int) Math.round(cx + r * path.get(i)[0]);
					int y1 = (int) Math.round(cy - r * path.get(i)[1]);
					int x2 = (int) Math.round(cx + r * path.get(i + 1)[0]);
					int y2 = (int) Math.round(cy - r * path.get(i + 1)[1]);
					g.drawLine(x1, y1, x2, y2);
				}
			}

			// bounds, FOV, and LOS
			if (DEBUG && organism.equals(dish.getSelected())) {
				// bounds
				g.setColor(Color.BLUE);
				g.drawRect(x, y, w, h);

				// dir
				double dir = organism.getDir();
				double sightRange = organism.getSightRange();
				int x1 = (int) Math.round(x + osize + Math.cos(dir) * osize);
				int y1 = (int) Math.round(y + osize - Math.sin(dir) * osize);
				int x2 = (int) Math.round(x + osize + Math.cos(dir) * r * sightRange);
				int y2 = (int) Math.round(y + osize - Math.sin(dir) * r * sightRange);
				g.drawLine(x1, y1, x2, y2);

				// FOV
				double FOV = organism.getFOV();
				int x3 = (int) Math.round(x + osize + Math.cos(dir + FOV / 2) * osize);
				int y3 = (int) Math.round(y + osize - Math.sin(dir + FOV / 2) * osize);
				int x4 = (int) Math.round(x + osize + Math.cos(dir + FOV / 2) * r * sightRange);
				int y4 = (int) Math.round(y + osize - Math.sin(dir + FOV / 2) * r * sightRange);
				g.drawLine(x3, y3, x4, y4);
				int x5 = (int) Math.round(x + osize + Math.cos(dir - FOV / 2) * osize);
				int y5 = (int) Math.round(y + osize - Math.sin(dir - FOV / 2) * osize);
				int x6 = (int) Math.round(x + osize + Math.cos(dir - FOV / 2) * r * sightRange);
				int y6 = (int) Math.round(y + osize - Math.sin(dir - FOV / 2) * r * sightRange);
				g.drawLine(x5, y5, x6, y6);

				// left sight
				ArrayList<Entity> leftSight = organism.getLeftSight();
				g.setColor(Color.YELLOW);
				for (Entity entity : leftSight) {
					int x7 = x + osize;
					int y7 = y + osize;
					int x8 = (int) Math.round(cx + r * entity.getX());
					int y8 = (int) Math.round(cy - r * entity.getY());
					g.drawLine(x7, y7, x8, y8);
				}

				// right sight
				ArrayList<Entity> rightSight = organism.getRightSight();
				g.setColor(Color.GREEN);
				for (Entity entity : rightSight) {
					int x9 = x + osize;
					int y9 = y + osize;
					int x10 = (int) Math.round(cx + r * entity.getX());
					int y10 = (int) Math.round(cy - r * entity.getY());
					g.drawLine(x9, y9, x10, y10);
				}

				// sight range
				g.setColor(Color.RED);
				x = (int) Math.round(cx + r * (organism.getX() - sightRange));
				y = (int) Math.round(cy - r * (organism.getY() + sightRange));
				w = (int) Math.round(r * sightRange * 2);
				h = w;
				g.drawOval(x, y, w, h);
			}
		}
	}

	public String toString(int[] arr) {
		String str = "[ ";
		int size = arr.length;
		for (int i = 0; i < size; i++) {
			if (i != 0)
				str += ", ";
			str += arr[i];
		}
		str += " ]";
		return str;
	}
	
	public String toString(double[] arr) {
		String str = "[ ";
		int size = arr.length;
		for (int i = 0; i < size; i++) {
			if (i != 0)
				str += ", ";
			str += round(arr[i]);
		}
		str += " ]";
		return str;
	}

	private void paintDebug(Graphics2D g) {
		// gui
		g.setColor(Color.WHITE);
		// g.translate(screenWidth / -2, screenHeight / -2);

		// debug menu
		ArrayList<String> debugMenu = new ArrayList<String>();
		debugMenu.add("Debug Menu:");
		debugMenu.add("");
		// debugMenu.add("Target FPS: " + targetFPS);
		debugMenu.add("dt (ms): " + targetTime);
		debugMenu.add("Period Frames: " + periodSteps);
		debugMenu.add("Period Time: " + periodTime);
		debugMenu.add("Current FPS: " + round(currentFPS));
		debugMenu.add("Total Steps: " + dish.getTime());
		debugMenu.add("Paused: " + pause);
		debugMenu.add("");
		debugMenu.add("Last Click: " + mouseX + ", " + mouseY);
		Organism selected = dish.getSelected();
		if (selected != null)
			debugMenu.addAll(printSelected(selected));
		debugMenu.add("");
		debugMenu.add("Buffer: " + buffer);
		debugMenu.add("Radius: " + r);
		debugMenu.add("Paths: " + paths);
		debugMenu.add("");
		debugMenu.add("Foods: " + dish.getAgars().size());
		debugMenu.add("Herbivores: " + dish.getHerbs().size());
		debugMenu.add("Carnivores: " + dish.getCarns().size());
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
		ArrayList<Integer> agars = dish.getAgarCensus();
		plotLine(g, agars, Color.GREEN);
		
		ArrayList<Integer> herbs = dish.getHerbCensus();
		plotLine(g, herbs, Color.BLUE);
		
		ArrayList<Integer> carns = dish.getCarnCensus();
		plotLine(g, carns, Color.RED);
	}
	
	private void plotLine(Graphics2D g, ArrayList<Integer> d, Color c) {
		g.setColor(c);
		
		int size = d.size();
		for (int i = 0; i < size - 1; i++) {
			int x1 = (int) (10 + censusWidthScale * i);
			int y1 = (int) (screenHeight - 10 - censusHeightScale * d.get(i));
			int x2 = (int) (x1 + censusWidthScale);
			int y2 = (int) (screenHeight - 10 - censusHeightScale * d.get(i + 1));
			g.drawLine(x1, y1, x2, y2);
		}
	}
	
	private ArrayList<String> printSelected(Organism selected) {
		ArrayList<String> selectedMenu = new ArrayList<String>();
		selectedMenu.add("Selected: " + selected);
		selectedMenu.add("X: " + selected.getX());
		selectedMenu.add("Y: " + selected.getY());
		selectedMenu.add("Direction: " + round(selected.getDir() / Math.PI * 180));
		selectedMenu.add("Energy: " + round(selected.getEnergy()));
		selectedMenu.add("Speed: " + selected.getSpeed());
		selectedMenu.add("Sight Range: " + round(selected.getSightRange()));
		
		Brain selectedBrain = selected.getBrain();
		int numLayers = selectedBrain.numLayers();
		
		selectedMenu.add("structure: " + toString(selectedBrain.getStruct()));
		selectedMenu.add("acts: " + toString(selectedBrain.getActs()));
		selectedMenu.add("numLayers: " + numLayers);
		selectedMenu.add("inputs: " + toString(selected.getInputs()));
		selectedMenu.add("outputs: " + toString(selected.getOutputs()));
		
		double[][][] weights = selectedBrain.getWeights();
		
		for (int l = 1; l <= numLayers; l++) {
			double[][] wl = weights[l-1];
			int dj = wl.length;
			int di = wl[0].length;
			
			String raw = "W%d: %dx%d";
			String formatted = String.format(raw, l, dj, di);
			
			selectedMenu.add(formatted);
			
			for (int j = 0; j < dj; j++) {
				String line = "";
				for (int i = 0; i < di; i++) {
					raw = "  %.2f";
					formatted = String.format(raw, wl[j][i]);
					line += formatted;
				}
				selectedMenu.add(line);
			}
		}
		
		return selectedMenu;
	}

	private double round(double num) {
		return Math.round(num * Math.pow(10, figures)) / Math.pow(10, figures);
	}

	// step simulation
	private void step() {
		dish.step(targetTime);
	}

	// key inputs
	class KeyHandler extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			// pause sim
			if (e.getKeyCode() == KeyEvent.VK_SPACE)
				pause = !pause;

			// step simulation
			else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
				step();
		}

		public void keyReleased(KeyEvent e) {
			int keyCode = e.getKeyCode();

			// quit program
			if (keyCode == KeyEvent.VK_ESCAPE)
				System.exit(0);

			// toggle debug menu
			else if (keyCode == KeyEvent.VK_F3)
				DEBUG = !DEBUG;

			// reset dish
			else if (keyCode == KeyEvent.VK_R) {
				pause = true;
				dish.reset();
			}

			/*
			// toggle FPS/sim limiter
			else if (keyCode == KeyEvent.VK_U)
				limit = !limit;
			*/
			
			// toggle paths
			else if (keyCode == KeyEvent.VK_P)
				paths = !paths;

			// modify amount of food
			else if (keyCode == KeyEvent.VK_F)
				dish.increaseFood();
			else if (keyCode == KeyEvent.VK_V)
				dish.decreaseFood();

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
				int x = (int) Math.round(cx + r * organism.getX()) - osize;
				int y = (int) Math.round(cy - r * organism.getY()) - osize;
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
