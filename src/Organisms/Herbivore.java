package Organisms;
import java.awt.Color;

public class Herbivore extends Organism {
	
	public Herbivore(double x, double y, double a, Color color) {
		super(x, y, a, color);
		setup();
		int[] struct = {7, 2}; // input layer, output layer
		int[] acts = {Brain.ReLU, Brain.Tanh}; // activation functions for each layer
		this.brain = new Brain(struct, acts);
	}

	public Herbivore(Herbivore parent) {
		super(parent);
		setup();
	}
	
	private void setup() {
		splitThreshold = 5.0;
		
		minEnergy = 0.1; // minimum energy for organism to survive
		
		speedCost = 0.1;
		rangeCost = 0.001;
		FOVCost = 0.001;
		
		speedCost = metabolism * speedCost;
		rangeCost = metabolism * rangeCost;
		FOVCost = metabolism * FOVCost;
	}
	
	// gains energy from eating agar
	// if splitting threshold is reached, return a new daughter cell
	public Herbivore eatAgar(Agar agar) {
		energy += agar.eaten();
		if (energy >= splitThreshold) {
			energy /= 2;
			return new Herbivore(this);
		}
		else
			return null;
	}

	public double eaten() {
		eaten = true;
		return energy * 0.5;
	}

	public boolean wasEaten() {
		return eaten;
	}

}
