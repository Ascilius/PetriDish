import java.util.ArrayList;

public class PetriDish {

	// food
	ArrayList<Food> foods = new ArrayList();

	// organisms
	private final int num = 10;
	ArrayList<Organism> organisms = new ArrayList();
	private Organism selected = null;

	public PetriDish() {
		// food
		// generateFood();

		// organisms
		regenerateOrganisms();
	}

	/*
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
	*/

	public void regenerateOrganisms() {
		organisms.clear();
		for (int i = 0; i < num; i++) {
			double r = Math.random();
			double a = Math.random() * 2 * Math.PI;
			double x = Math.cos(a) * r;
			double y = Math.sin(a) * r;
			double dir = Math.random() * 2 * Math.PI;
			// r = Math.random() * 2 + 2;
			organisms.add(new Organism(x, y, dir));
		}
	}

	// step simulation
	public void step(long targetTime) {
		for (Organism organism : organisms) {
			organism.move(targetTime);
		}
	}

	public ArrayList<Organism> getOrganisms() {
		return organisms;
	}

	public Organism getSelected() {
		return selected;
	}
	
	public void setSelected(Organism selected) {
		this.selected = selected;
	}

}
