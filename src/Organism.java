import java.awt.Color;
import java.util.ArrayList;

public class Organism implements Entity {

	// dish
	protected PetriDish dish;

	// physics
	protected double x, y;
	protected double a; // radians

	// path
	protected ArrayList<double[]> path = new ArrayList<>();
	protected int pathLength = 100; // -1 = unlimited

	// attributes
	protected Color color;
	protected double energy = 1.0, splitThreshold;
	protected boolean eaten = false, dead = false;

	// movement
	protected double speed = 3 * 10e-6, speedCost = 16;
	protected double turn = Math.PI / 16;

	// sight
	protected double sightRange = 0.25, rangeCost = 10e-4;
	protected double FOV = Math.PI / 3, FOVCost = 0.1;
	protected ArrayList<Entity> leftSight = new ArrayList<Entity>(); // entities the organism sees on its left
	protected ArrayList<Entity> rightSight = new ArrayList<Entity>();

	// brain
	protected Brain brain = new Brain();
	protected double[] inputs = new double[7]; // hunger (1), avg left color (rgb) (3), avg right color (rgb) (3), total 7
	protected double[] outputs = new double[3]; // speed, left turn, right turn (3)

	public Organism(PetriDish dish, double x, double y, double a, Color color) {
		this.dish = dish;

		this.x = x;
		this.y = y;
		this.a = a;

		this.color = color;
	}

	public Organism(Organism parent) {
		this.dish = parent.getDish();

		this.x = parent.getX();
		this.y = parent.getY();
		this.a = Math.random() * 2 * Math.PI;

		this.color = parent.getColor();

		this.speed = parent.getSpeed() + (Math.random() - 0.5) * 10e-6;
		this.sightRange = parent.getSightRange() + (Math.random() - 0.5) * 0.05;

		this.brain = new Brain(parent.getBrain());
	}

	// processing world information through brain
	public void think() {
		// hunger
		inputs[0] = energy;

		// left sight
		inputs[1] = 0;
		inputs[2] = 0;
		inputs[3] = 0;
		int size = leftSight.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				Color entityColor = leftSight.get(i).getColor();
				inputs[1] += entityColor.getRed();
				inputs[2] += entityColor.getGreen();
				inputs[3] += entityColor.getBlue();
			}
			/*
			inputs[1] /= size;
			inputs[2] /= size;
			inputs[3] /= size;
			*/
		}

		// right sight
		inputs[4] = 0;
		inputs[5] = 0;
		inputs[6] = 0;
		size = rightSight.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				Color entityColor = rightSight.get(i).getColor();
				inputs[4] += entityColor.getRed();
				inputs[5] += entityColor.getGreen();
				inputs[6] += entityColor.getBlue();
			}
			/*
			inputs[4] /= size;
			inputs[5] /= size;
			inputs[6] /= size;
			*/
		}

		// thinking
		outputs = brain.think(inputs);
		/*
		System.out.println("Debug: " + inputs[0] + ", " + inputs[1] + ", " + inputs[2] + ", " + inputs[3] + ", " + inputs[4] + ", " + inputs[5] + ", " + inputs[6]);
		System.out.println("Debug: " + outputs[0] + ", " + outputs[1] + ", " + outputs[2] + "\n");
		*/
	}

	public void move(long timeStep) { // timeStep in ms
		// path
		double[] previous = { x, y };
		this.path.add(previous);
		if (pathLength != -1 && path.size() > pathLength)
			path.remove(0);

		// turning
		turn();

		// moving
		// double desiredSpeed = speed;
		double desiredSpeed = speed * (1 / (1 + Math.pow(Math.E, -4 * outputs[0]))); // brain determines speed
		double cos = Math.cos(a);
		double dx = desiredSpeed * (1000 / timeStep) * Math.cos(a);
		double sin = Math.sin(a);
		double dy = desiredSpeed * (1000 / timeStep) * Math.sin(a);
		/*
		System.out.println("Debug: speed: " + speed);
		System.out.println("Debug: a: " + a);
		System.out.println("Debug: cos: " + cos);
		System.out.println("Debug: sin: " + sin);
		System.out.println("Debug: timeStep: " + timeStep);
		System.out.println("Debug: desiredSpeed: " + desiredSpeed);
		System.out.println("Debug: timeStep: " + timeStep);
		System.out.println("Debug: ( " + dx + ", " + dy + " )\n");
		*/
		x += dx;
		y += dy;

		// bounds check
		double r = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		double a = Math.atan(y / x);
		if (r > 1) {
			if (x < 0)
				a += Math.PI;
			x = Math.cos(a);
			y = Math.sin(a);
		}

		// use energy
		spendEnergy(desiredSpeed);
	}

	public void turn() {
		// brain determines turn
		a += outputs[1] - outputs[2];

		// over 360
		while (a >= (2 * Math.PI))
			a -= 2 * Math.PI;

		// below 0
		while (a < 0)
			a += 2 * Math.PI;
	}

	private void spendEnergy(double desiredSpeed) {
		// organism spends based on speed
		energy -= desiredSpeed * speedCost;

		// more sight requires more energy
		energy -= sightRange * rangeCost;

		// ran out of energy
		if (energy <= 0.0)
			dead = true; // mark organism as dead
	}

	public void clearSight() {
		leftSight.clear();
		rightSight.clear();
	}

	// locates other entities
	public void seeEntity(Entity other) {
		// getting other information
		double ox = other.getX();
		double oy = other.getY();

		// maximum sight range
		double dx = ox - x;
		double dy = oy - y;
		double d = Math.sqrt(dx * dx + dy * dy);
		if (d < sightRange) {

			// v1
			double x1 = Math.cos(a);
			double y1 = Math.sin(a);
			double v1 = 1;

			// v2
			double x2 = dx;
			double y2 = dy;
			double v2 = Math.sqrt(x2 * x2 + y2 * y2);

			// angle between two vectors
			double t = Math.acos((x1 * x2 + y1 * y2) / (v1 * v2));

			// checking if within FOV
			if (t < FOV / 2) {

				// determining side of sight
				double k = x1 * y2 - y1 * x2;
				if (k > 0)
					leftSight.add(other); // left side sight
				else
					rightSight.add(other); // right
			}
		}
	}

	// eaten functions are specified within respective organism files
	public double eaten() {
		System.out.println("ERROR: Something has gone wrong");
		return 0;
	}

	// TOREMOVE: @formatter:off
	// dish
	public PetriDish getDish() { return dish; }
	
	// physics
	public double getX() { return x; }
	public double getY() { return y; }
	public double getDir() { return a; }
	
	// other
	public Color getColor() { return color; }
	public ArrayList<double[]> getPath() { return path; }

	// state
	public double getEnergy() { return energy; }
	public boolean wasEaten() { return eaten; }
	public boolean isDead() { return dead; }
	
	// movement
	public double getSpeed() { return speed; }
	
	// sight
	public double getSightRange() { return sightRange; }
	public double getFOV() { return FOV; }
	public ArrayList<Entity> getLeftSight() { return leftSight; }
	public ArrayList<Entity> getRightSight() { return rightSight; }

	// brain
	public Brain getBrain() { return brain; }
	public double[] getInputs() { return inputs; }
	public double[] getOutputs() { return outputs; }
	// TOREMOVE: @formatter: on

}
