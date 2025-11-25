package Organisms;
import java.awt.Color;

public class Carnivore extends Organism {
	
	public Carnivore(double x, double y, double a, Color color) {
		super(x, y, a, color);
		setup();
		int[] struct = {7, 5, 2}; // input layer, hidden layer, output layer
		int[] acts = {Brain.ReLU, Brain.ReLU, Brain.Tanh};
		this.brain = new Brain(struct, acts);
	}

	public Carnivore(Carnivore parent) {
		super(parent);
		setup();
	}
	
	private void setup() {
		splitThreshold = 20.0;
		
		minEnergy = 0.2;
		
		speedCost = 0.5;
		rangeCost = 0.005;
		FOVCost = 0.005;
		
		speedCost = metabolism * speedCost;
		rangeCost = metabolism * rangeCost;
		FOVCost = metabolism * FOVCost;
	}
	
	// gains energy from eating agar
	// if splitting threshold is reached, return a new daughter cell
	public Carnivore eatPrey(Herbivore prey) {
		energy += prey.eaten();
		if (energy >= splitThreshold) {
			energy /= 2;
			return new Carnivore(this);
		}
		else
			return null;
	}

	// carnivores can't be eaten, for now
	public double eaten() {
		System.out.println("WARNING: Carnivore.eaten() was called somehow");
		return 0;
	}

	public boolean wasEaten() {
		return false;
	}

}
