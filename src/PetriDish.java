import java.awt.Color;
import java.util.ArrayList;

public class PetriDish {

	// agar
	private int na = 200; // number of agar
	private ArrayList<Agar> agars = new ArrayList<Agar>();

	// herbivores
	private int nh = 20; // min number of herbivores
	private ArrayList<Herbivore> herbs = new ArrayList<Herbivore>();

	// canrivores
	private int nc = 10;
	private ArrayList<Carnivore> carns = new ArrayList<Carnivore>();

	// organisms
	private double range = 0.01; // organism eating range
	private Organism selected = null;
	private ArrayList<int[]> census = new ArrayList<int[]>();
	private int historyLimit = 100;
	private int v = 64; // color variance

	public PetriDish() {
		reset();
	}

	// generates a random location within the r=1 petri dish
	public double[] genRanLoc() {
		double r = Math.random();
		double a = Math.random() * 2 * Math.PI;
		double x = Math.cos(a) * r;
		double y = Math.sin(a) * r;
		double[] loc = { x, y };
		return loc;
	}

	public void reset() {
		// destroying old entities
		agars.clear();
		herbs.clear();
		carns.clear();
		selected = null;

		// new agar
		for (int i = 0; i < na; i++)
			generateAgar();

		// new herbivores
		for (int i = 0; i < nh; i++)
			generateHerb();

		// new carnivores
		for (int i = 0; i < nc; i++)
			generateCarn();

		look(); // organisms get to look around first before the sim starts
	}

	public void generateAgar() {
		double[] loc = genRanLoc(); // generating random location on dish
		agars.add(new Agar(this, loc[0], loc[1]));
	}

	public void generateHerb() {
		// physical
		double[] loc = genRanLoc();
		double x = loc[0];
		double y = loc[1];
		double a = Math.random() * 2 * Math.PI; // generating random starting direction

		// random color
		int R = (int) (Math.random() * v);
		int G = (int) (Math.random() * v);
		int B = (int) (Math.random() * v) + (255 - v);
		Color color = new Color(R, G, B);

		herbs.add(new Herbivore(this, x, y, a, color));
	}

	public void generateCarn() {
		// physical
		double[] loc = genRanLoc();
		double x = loc[0];
		double y = loc[1];
		double a = Math.random() * 2 * Math.PI;

		// random color
		int R = (int) (Math.random() * v) + (255 - v);
		int G = (int) (Math.random() * v);
		int B = (int) (Math.random() * v);
		Color color = new Color(R, G, B);

		carns.add(new Carnivore(this, x, y, a, color));
	}

	// TOREMOVE: @formatter:off
	public void addHerb(Herbivore newHerb) { herbs.add(newHerb); } // a herbivore has split
	public void addCarn(Carnivore newCarn) { carns.add(newCarn); } // a carnivore has split
	// TOREMOVE: @formatter:on

	// step simulation
	public void step(long targetTime) {
		// thinking
		for (Herbivore herb : herbs)
			herb.think();
		for (Carnivore carn : carns)
			carn.think();

		// moving
		for (Herbivore herb : herbs)
			herb.move(targetTime);
		for (Carnivore carn : carns)
			carn.move(targetTime);

		// eating entities
		eat();

		// removing eaten or dead entities
		clean();

		// respawning up to minimum entity counts
		while (agars.size() < na)
			generateAgar();
		while (herbs.size() < nh)
			generateHerb();
		while (carns.size() < nc)
			generateCarn();

		// looking
		look();
	}

	// having organisms eat
	public void eat() {
		// herbivores
		int size = herbs.size();
		for (int i = 0; i < size; i++) {
			Herbivore herb = herbs.get(i);
			for (Agar agar : agars) {
				// gathering location data
				double hx = herb.getX();
				double hy = herb.getY();
				double ax = agar.getX();
				double ay = agar.getY();

				// calculating distance between organism and food
				double dx = ax - hx;
				double dy = ay - hy;
				double d = Math.sqrt(dx * dx + dy * dy);

				// checking if food is within range
				if (d < range)
					herb.eatAgar(agar);
			}
		}

		// carnivores
		size = carns.size();
		for (int i = 0; i < size; i++) {
			Carnivore carn = carns.get(i);
			for (Herbivore herb : herbs) {
				// gathering location data
				double hx = herb.getX();
				double hy = herb.getY();
				double cx = carn.getX();
				double cy = carn.getY();

				// calculating distance between predator and prey
				double dx = cx - hx;
				double dy = cy - hy;
				double d = Math.sqrt(dx * dx + dy * dy);

				// checking if food is within range
				if (d < range)
					carn.eatPrey(herb);
			}
		}
	}

	// remove eaten or dead entities
	public void clean() {
		// removing eaten agars
		for (int i = 0; i < agars.size(); i += 0) {
			Agar agar = agars.get(i);
			if (agar.wasEaten())
				agars.remove(agar);
			else
				i++;
		}

		// removing eaten or dead herbs
		for (int i = 0; i < herbs.size(); i += 0) {
			Herbivore herb = herbs.get(i);
			if (herb.wasEaten() || herb.isDead()) {
				herbs.remove(herb);
				if (herb.equals(selected))
					selected = null;
			} else
				i++;
		}

		// removing dead carns
		for (int i = 0; i < carns.size(); i += 0) {
			Carnivore carn = carns.get(i);
			if (carn.isDead()) {
				carns.remove(carn);
				if (carn.equals(selected))
					selected = null;
			} else
				i++;
		}
	}

	public void look() {
		// herbivores
		for (Herbivore herb : herbs) {
			// clearing previously seen entities
			herb.clearSight();

			// looking at foods
			for (Agar agar : agars)
				herb.seeEntity(agar);

			// looking at other herbs
			for (Herbivore other : herbs) {
				if (!herb.equals(other))
					herb.seeEntity(other);
			}

			// looking at carns
			for (Carnivore carn : carns)
				herb.seeEntity(carn);
		}

		// carnivores
		for (Carnivore carn : carns) {
			// clearing previously seen entities
			carn.clearSight();

			// looking at foods
			for (Agar agar : agars)
				carn.seeEntity(agar);

			// looking at herbs
			for (Herbivore herb : herbs)
				carn.seeEntity(herb);

			// looking at other carns
			for (Carnivore other : carns) {
				if (!carn.equals(other))
					carn.seeEntity(other);
			}
		}

	}

	public void increaseFood() {
		na += 10;
		while (agars.size() < na)
			generateAgar();
	}

	public void decreaseFood() {
		na -= 10;
		if (na < 10)
			na = 10;
		while (agars.size() > na)
			agars.remove(0);
	}

	public void takeCensus() {
		// count organisms
		int[] newCensus = { herbs.size(), carns.size() };
		census.add(newCensus);
		if (census.size() > historyLimit)
			census.remove(0);
	}

	// TOREMOVE: @formatter:off
	public ArrayList<Agar> getAgars() {	return agars; }
	
	public ArrayList<Herbivore> getHerbs() { return herbs; }
	public ArrayList<Carnivore> getCarns() { return carns; }
	public ArrayList<Organism> getOrganisms() {
		ArrayList<Organism> organisms = new ArrayList<Organism>();
		organisms.addAll(getHerbs());
		organisms.addAll(getCarns());
		return organisms;
	}
	
	public Organism getSelected() {	return selected; }
	public void setSelected(Organism selected) { this.selected = selected; }
	public ArrayList<int[]> getCensus() { return census; }

}
