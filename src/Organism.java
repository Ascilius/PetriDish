import java.awt.Color;
import java.util.ArrayList;

public class Organism {

	// dish
	private PetriDish dish;
	
	// physics
	private double sx, sy;
	// private double r;
	private double dir;
	private double speed = 6 * 10e-6;
	private double turn = Math.PI / 16;
	private ArrayList<double[]> path = new ArrayList<>();
	private int pathLength = -1; // -1 = unlimited

	// sight
	ArrayList<LOS> LOSs = new ArrayList<LOS>();

	// attributes
	private Color color;
	private double FOV = Math.PI / 3; // radians
	private double energy = 1.0, lossRate = 0.005, calories = 0.5;
	private boolean dead = false;
	private double splitThreshold = 3.0;

	// brain
	private Brain brain = new Brain();

	public Organism(PetriDish dish, double sx, double sy, double dir) {
		// dish
		this.dish = dish;
		
		// physics
		this.sx = sx;
		this.sy = sy;
		// this.r = r;
		this.dir = dir;

		// attributes
		color = new Color((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256));
	}

	public Organism(Organism parent) {
		this.dish = parent.getDish();
		
		this.sx = parent.getSX();
		this.sy = parent.getSY();
		this.dir = Math.random() * 2 * Math.PI;

		this.color = parent.getColor();

		this.brain = new Brain(parent.getBrain());
	}

	public void move(long timeStep) { // timeStep in ms
		// organism path
		double[] previous = { this.sx, this.sy };
		this.path.add(previous);
		if (pathLength != -1 && path.size() > pathLength) {
			path.remove(0);
		}

		// moving
		this.turn(); // random turn
		sx += speed * (1000 / timeStep) * Math.cos(dir);
		sy += speed * (1000 / timeStep) * Math.sin(dir);
		energy -= lossRate; // organism spends energy
		if (energy <= 0.0)
			dead = true; // mark organism as dead;

		// bounds check
		double r = Math.sqrt(Math.pow(sx, 2) + Math.pow(sy, 2));
		double a = Math.atan(sy / sx);
		if (r > 1) {
			if (sx < 0)
				a += Math.PI;
			sx = Math.cos(a);
			sy = Math.sin(a);
		}
	}

	public void turn() {
		// processing information through brain
		int l = 0;
		int r = 0;
		for (LOS los : LOSs) {
			if (los.isLeft())
				l++;
			if (los.isRight())
				r++;
		}
		double[] inputs = { l, r };
		double[] outputs = brain.doStuff(inputs);

		// turning based off of neural network
		dir += outputs[0] - outputs[1];
		overflow();
	}

	private void overflow() {
		if (dir >= (2 * Math.PI)) {
			dir -= 2 * Math.PI;
		} else if (dir < 0) {
			dir += 2 * Math.PI;
		}
	}

	public void clearLOSs() {
		LOSs.clear();
	}

	// checking whether other organism is within FOV and which side of sight it is in
	public void seeFood(Food food) {
		// v1
		double x1 = Math.cos(dir);
		double y1 = Math.sin(dir);
		double v1 = 1;

		// v2
		double x2 = food.getX() - sx;
		double y2 = food.getY() - sy;
		double v2 = Math.sqrt(x2 * x2 + y2 * y2);

		// angle between two vectors
		double t = Math.acos((x1 * x2 + y1 * y2) / (v1 * v2));

		// checking if within FOV
		boolean valid = t < FOV / 2;

		// determining side of sight
		boolean left = false;
		boolean right = false;
		if (valid) {
			double k = x1 * y2 - y1 * x2; // ugh vector math
			// System.out.println("Debug: " + x1 + " * " + y2 + " - " + y1 + " * " + x1 + " = " + k);
			if (k > 0)
				left = true;
			else
				right = true;
		}

		// saving
		LOS los = new LOS(food, valid, left, right);
		LOSs.add(los);
	}

	public void eatFood(Food food) {
		energy += calories;
		if (energy >= splitThreshold) {
			energy /= 2;
			dish.addOrganism(new Organism(this));
		}
		food.eat();
	}
	
	// get the dish this organism is in
	public PetriDish getDish() {
		return dish;
	}

	public double getSX() {
		return sx;
	}

	public double getSY() {
		return sy;
	}

	/*
	public double getR() {
		return r;
	}
	*/

	public double getDir() {
		return dir;
	}

	public Color getColor() {
		return color;
	}

	public ArrayList<double[]> getPath() {
		return path;
	}

	public double getFOV() {
		return FOV;
	}

	public ArrayList<LOS> getLOSs() {
		return LOSs;
	}

	public Brain getBrain() {
		return brain;
	}

	public double getEnergy() {
		return energy;
	}

	public boolean isDead() {
		return dead;
	}
}
