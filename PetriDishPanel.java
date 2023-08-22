import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
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
	private final boolean debug = true;
	private final int figures = 3;

	// screen
	private int screenWidth, screenHeight;

	// engine
	private double targetFPS = 59.95;
	private double targetTime = 1000.0 / targetFPS; // ms
	private int totalFrames = 0;
	private double totalTime = 0;
	private double frameTime = 0; // ms
	private double currentFPS = 0;
	private boolean limit = true;
	private boolean pause = false;

	// inputs
	private KeyHandler keyHandler; // key inputs
	private MouseHandler mouseHandler; // mouse inputs
	private double mouseX = 0;
	private double mouseY = 0;

	// dish
	private double buffer = 32.0;
	private final double radius;

	// food
	ArrayList<Food> foods = new ArrayList();

	// organisms
	private final int num = 10;
	ArrayList<Organism> organisms = new ArrayList();
	private Organism selected = null;

	public PetriDishPanel(double screenWidth, double screenHeight) {
		// screen
		this.screenWidth = (int) screenWidth;
		this.screenHeight = (int) screenHeight;

		// inputs
		this.keyHandler = new KeyHandler();
		addKeyListener(this.keyHandler);
		this.mouseHandler = new MouseHandler();
		addMouseListener(this.mouseHandler);
		setFocusable(true);

		// dish
		buffer = screenHeight / buffer;
		radius = (screenHeight / 2) - buffer;

		// food
		generateFood();

		// organisms
		generateOrganisms();
	}

	public void generateFood() {
		foods.clear();
		for (int i = 0; i < num; i++) {
			double r = Math.random() * radius;
			double a = Math.random() * 2 * Math.PI;
			double x = r * Math.cos(a);
			double y = r * Math.sin(a);
			foods.add(new Food(x, y));
		}
	}

	public void generateOrganisms() {
		organisms.clear();
		for (int i = 0; i < num; i++) {
			double r = Math.random() * radius;
			double a = Math.random() * 2 * Math.PI;
			double x = r * Math.cos(a);
			double y = r * Math.sin(a);
			double dir = Math.random() * 2 * Math.PI;
			r = Math.random() * 2 + 2;
			organisms.add(new Organism(x, y, r, dir));
		}
	}

	public void paintComponent(Graphics graphics) {
		// anti-aliasing
		Graphics2D g = (Graphics2D) graphics;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// new frame
		final long startTime = System.currentTimeMillis();
		// fps calculation
		if (totalFrames >= targetFPS) {
			currentFPS = totalFrames / (totalTime / 1000);
			totalFrames = 0;
			totalTime = 0.0;
		}

		// background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, screenWidth, screenHeight);

		// dish
		g.translate(screenWidth / 2, screenHeight / 2);
		g.setColor(Color.WHITE);
		g.fillOval((int) (radius * -1), (int) (radius * -1), (int) (radius * 2), (int) (radius * 2));

		// foods
		g.setColor(Color.BLACK);
		for (Food food : foods) {
			g.drawOval((int) (food.getX() - 1), (int) (food.getY() - 1), 2, 2);
		}

		// organisms
		for (Organism organism : organisms) {
			g.setColor(organism.getColor());
			g.fillOval((int) (organism.getSX() - organism.getR()), (int) (organism.getSY() - organism.getR()), (int) (organism.getR() * 2), (int) (organism.getR() * 2));
			// paths
			ArrayList<double[]> path = organism.getPath();
			for (int i = 0; i < path.size() - 1; i++) {
				g.drawLine((int) path.get(i)[0], (int) path.get(i)[1], (int) path.get(i + 1)[0], (int) path.get(i + 1)[1]);
			}
		}
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

		// move
		if (pause == false) {
			for (Organism organism : organisms) {
				organism.move(targetTime, radius);
			}
		}

		// gui
		g.setColor(Color.WHITE);
		g.translate(screenWidth / -2, screenHeight / -2);

		// debug menu
		ArrayList<String> debugMenu = new ArrayList<String>();
		debugMenu.add("Debug Menu:");
		debugMenu.add("");
		debugMenu.add("Target FPS: " + targetFPS);
		debugMenu.add("Target Time (ms): " + round(targetTime));
		debugMenu.add("Total Frames: " + totalFrames);
		debugMenu.add("Frame Time: " + frameTime);
		debugMenu.add("Total Time: " + totalTime);
		debugMenu.add("Current FPS: " + round(currentFPS));
		debugMenu.add("FPS Limit: " + limit);
		debugMenu.add("Paused: " + pause);
		debugMenu.add("");
		debugMenu.add("Last Click: " + mouseX + ", " + mouseY);
		debugMenu.add("Selected: " + (selected != null));
		if (selected != null) {
			debugMenu.add("X: " + selected.getSX());
			debugMenu.add("Y: " + selected.getSY());
			debugMenu.add("R: " + selected.getR());
			debugMenu.add("Direction: " + (selected.getDir() / Math.PI * 180));
			debugMenu.add("Bounds: " + selected.getBounds().toString());
		}
		debugMenu.add("");
		debugMenu.add("Buffer: " + buffer);
		debugMenu.add("Radius: " + radius);
		debugMenu.add("");
		debugMenu.add("Organisms: " + organisms.size());
		// printing
		for (int i = 0; i < debugMenu.size(); i++) {
			g.drawString(debugMenu.get(i), 10, 20 * (i + 1));
		}

		// end frame
		totalFrames++;
		long endTime = System.currentTimeMillis();
		frameTime = endTime - startTime;
		if (limit == true && frameTime < targetTime) {
			try {
				TimeUnit.MILLISECONDS.sleep((long) (targetTime - frameTime));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		endTime = System.currentTimeMillis();
		frameTime = endTime - startTime;
		totalTime += frameTime;
		repaint();
	}

	public double round(double num) {
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
			} else if (e.getKeyCode() == KeyEvent.VK_R) {
				generateOrganisms();
			}
		}
	}

	// mouse inputs
	class MouseHandler extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			Point location = e.getLocationOnScreen();
			mouseX = location.getX() - (screenWidth / 2);
			mouseY = location.getY() - (screenHeight / 2);
			boolean select = false;
			for (Organism organism : organisms) {
				if (organism.clicked(mouseX, mouseY)) {
					selected = organism;
					select = true;
					break;
				}
			}
			if (select == false) {
				selected = null;
			}

		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}
	}
}
