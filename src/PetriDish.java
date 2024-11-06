import java.util.ArrayList;

public class PetriDish {

	// food
	private final int nf = 250; // number of foods
	private ArrayList<Food> foods = new ArrayList<Food>();

	// organisms
	private final int no = 10; // number of organisms
	private ArrayList<Organism> organisms = new ArrayList<Organism>();
	private Organism selected = null;
	private double range = 0.025; // organism eating range
	private ArrayList<Integer> census = new ArrayList<Integer>();
	private int historyLimit = 100;

	public PetriDish() {
		reset();
	}

	public void reset() {
		regenerateFood();
		regenerateOrganisms();
	}

	public void regenerateFood() {
		foods.clear();
		for (int i = 0; i < nf; i++)
			generateFood();
	}

	public void generateFood() {
		foods.add(new Food());
	}

	public void regenerateOrganisms() {
		organisms.clear();

		for (int i = 0; i < no; i++)
			generateOrganism();

		/*
		organisms.add(new Organism(-0.5, 0.0, 0.0));
		organisms.add(new Organism(0.5, 0.25, Math.PI));
		organisms.add(new Organism(0.5, -0.25, Math.PI));
		
		organisms.add(new Organism(0.0, -0.5, Math.PI / 2));
		organisms.add(new Organism(-0.25, 0.5, Math.PI / -2));
		organisms.add(new Organism(0.25, 0.5, Math.PI / -2));
		*/
	}

	public void generateOrganism() {
		double r = Math.random();
		double a = Math.random() * 2 * Math.PI;
		double x = Math.cos(a) * r;
		double y = Math.sin(a) * r;
		double dir = Math.random() * 2 * Math.PI;
		// r = Math.random() * 2 + 2;
		organisms.add(new Organism(this, x, y, dir));
	}

	public void addOrganism(Organism newOrganism) {
		organisms.add(newOrganism);
	}

	// step simulation
	public void step(long targetTime) {
		// count organisms
		census.add(organisms.size());
		if (census.size() > historyLimit)
			census.remove(0);

		// moving
		for (Organism organism : organisms) {
			organism.move(targetTime);
		}

		// eating
		int os = organisms.size();
		for (int i = 0; i < os; i++) {
			Organism o = organisms.get(i);
			o.clearLOSs();

			int fs = foods.size();
			for (int j = 0; j < fs; j++) {
				Food f = foods.get(j);
				// calculating distance between organism and food
				double d = Math.sqrt(Math.pow(f.getX() - o.getSX(), 2) + Math.pow(f.getY() - o.getSY(), 2));
				if (d < range)
					o.eatFood(f);
			}
		}

		// kill dead organisms
		for (int i = 0; i < organisms.size(); i += 0) {
			Organism o = organisms.get(i);
			if (o.isDead()) {
				organisms.remove(o);
				if (o.equals(selected))
					selected = null;
			} else
				i++;
		}

		// respawn organisms
		while (organisms.size() < no) // minimum population
			generateOrganism();

		// removing eaten foods
		for (int i = 0; i < foods.size(); i += 0) {
			Food f = foods.get(i);
			if (f.wasEaten())
				foods.remove(f);
			else
				i++;
		}

		// respawn foods
		while (foods.size() < nf)
			generateFood();

		// looking
		os = organisms.size();
		for (int i = 0; i < os; i++) {
			Organism oi = organisms.get(i);
			oi.clearLOSs();

			int fs = foods.size();
			for (int j = 0; j < fs; j++) {
				oi.seeFood(foods.get(j));
			}
		}
	}

	// kill organism
	public void kill(Organism toKill) {
		organisms.remove(toKill);
	}

	public ArrayList<Food> getFoods() {
		return foods;
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

	public ArrayList<Integer> getCensus() {
		return census;
	}

}
