import java.awt.Color;

public class Herbivore extends Organism {

	public Herbivore(PetriDish dish, double x, double y, double a, Color color) {
		super(dish, x, y, a, color);
		splitThreshold = 5.0;
		speedCost = 10;
		rangeCost = 10e-4;
		FOVCost = 0.1;
	}

	public Herbivore(Herbivore parent) {
		super(parent);
		splitThreshold = 5.0;
		speedCost = 10;
		rangeCost = 10e-4;
		FOVCost = 0.1;
	}

	public void eatAgar(Agar agar) {
		energy += agar.eaten();
		if (energy >= splitThreshold) {
			energy /= 2;
			dish.addHerb(new Herbivore(this));
		}
	}

	public double eaten() {
		eaten = true;
		return energy * 0.5;
	}

	public boolean wasEaten() {
		return eaten;
	}

}
