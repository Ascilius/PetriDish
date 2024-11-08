import java.awt.Color;

public class Carnivore extends Organism {

	public Carnivore(PetriDish dish, double x, double y, double a, Color color) {
		super(dish, x, y, a, color);
		splitThreshold = 20.0;
		speedCost = 20;
		rangeCost = 20e-4;
		FOVCost = 0.2;
	}

	public Carnivore(Carnivore parent) {
		super(parent);
		splitThreshold = 10.0;
		speedCost = 20;
		rangeCost = 20e-4;
		FOVCost = 0.2;
	}

	public void eatPrey(Herbivore prey) {
		energy += prey.eaten();
		if (energy >= splitThreshold) {
			energy /= 2;
			dish.addCarn(new Carnivore(this));
		}
	}

	// carnivores can't be eaten, for now
	public double eaten() {
		System.out.println("ERROR: Something has gone wrong");
		return 0;
	}

	public boolean wasEaten() {
		return false;
	}

}
