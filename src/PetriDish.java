import java.awt.Color;
import java.util.ArrayList;

import Organisms.Agar;
import Organisms.Carnivore;
import Organisms.Herbivore;
import Organisms.Organism;

public class PetriDish {
	
	private static boolean DEBUG = true;
	
	// statistics
	private int t = 0;
	
	private ArrayList<Integer> agarCensus = new ArrayList<Integer>();
	private ArrayList<Integer> herbCensus = new ArrayList<Integer>();
	private ArrayList<Integer> carnCensus = new ArrayList<Integer>();
	private int ct = 100; // number of steps before each census
	protected int historyLimit = 100;
	
	// entities
	protected int na = 2000; // number of agar
	protected int nh = 1000;
	protected int nc = 500;
	
	/*
	protected int na = 0;
	protected int nh = 1;
	protected int nc = 1;
	
	protected int nc = 1;
	protected int nh = nc * 7;
	protected int na = nh * 286;
	*/
	
	private ArrayList<Agar> agars = new ArrayList<Agar>();
	
	private double range = 0.005; // organism eating range
	private ArrayList<Herbivore> herbs = new ArrayList<Herbivore>();
	private ArrayList<Carnivore> carns = new ArrayList<Carnivore>();
	
	// other
	private Organism selected = null;
	private boolean selectNew = false; // TODO: optimize
	private int v = 64; // color variance

	public PetriDish() {
		reset();
	}

	// generates a random location within a radius r petri dish
	public double[] genRanLoc(double R) {
		double x = 0.0;
		double y = 0.0;
		
		boolean pass = false;
		while (pass == false) {
			x = Math.random() * 2 - 1;
			y = Math.random() * 2 - 1;
			
			double r = Math.sqrt(x * x + y * y);
			if (r < R)
				pass = true;
		}
		
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
		double[] loc = genRanLoc(1.0); // generating random location on dish
		agars.add(new Agar(loc[0], loc[1]));
	}

	public void generateHerb() {
		// physical
		double[] loc = genRanLoc(1.0);
		double x = loc[0];
		double y = loc[1];
		double a = Math.random() * 2 * Math.PI; // generating random starting direction

		// random color
		int R = (int) (Math.random() * v);
		int G = (int) (Math.random() * v);
		int B = (int) (Math.random() * v) + (255 - v);
		Color color = new Color(R, G, B);

		herbs.add(new Herbivore(x, y, a, color));
	}

	public void generateCarn() {
		// physical
		double[] loc = genRanLoc(1.0);
		double x = loc[0];
		double y = loc[1];
		double a = Math.random() * 2 * Math.PI;

		// random color
		int R = (int) (Math.random() * v) + (255 - v);
		int G = (int) (Math.random() * v);
		int B = (int) (Math.random() * v);
		Color color = new Color(R, G, B);

		carns.add(new Carnivore(x, y, a, color));
	}

	// TOREMOVE: @formatter:off
	public void addHerb(Herbivore newHerb) { herbs.add(newHerb); } // a herbivore has split
	public void addCarn(Carnivore newCarn) { carns.add(newCarn); } // a carnivore has split
	// TOREMOVE: @formatter:on

	// spawn new agar from dead organism
	private void decomposeOrg(Organism dead) {
		double x = dead.getX();
		double y = dead.getY();
		
		double leftover = dead.getEnergy();
		long n = Math.round(leftover / Agar.calories);
		
		/*
		if (DEBUG)
			System.out.printf("DEBUG: l = %f, c = %f, n = %d\n", leftover, Agar.calories, n);
		*/
		
		for (int i = 0; i < n; i++) {
			double[] loc = genRanLoc(0.01);
			loc[0] += x;
			loc[1] += y;
			agars.add(new Agar(loc[0], loc[1]));
		}
	}
	
	// step simulation
	public void step(long targetTime) {
		// looking
		look();
		
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
		
		/*
		// TODO: agar "growth"
		long n = Math.round(1.0 * na / agars.size()) - 1; // more agar grows the less there is (to keep the ecosystem alive?)
		if (DEBUG && n > 0)
			System.out.printf("DEBUG: t: %d, n = %d\n", t, n);
		
		for (int i = 0; i < n; i++)
			generateAgar();
		*/
		
		/*
		// respawning up to minimum entity counts
		while (agars.size() < minAgar)
			generateAgar();
		while (herbs.size() < minHerb)
			generateHerb();
		while (carns.size() < minCarn)
			generateCarn();
		*/

		if (selectNew)
			selectRandom();
		
		t++;
		if (t % ct == 0)
			takeCensus();
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
				if (d < range) {
					Herbivore newHerb = herb.eatAgar(agar);
					if (newHerb != null)
						herbs.add(newHerb);
				}
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
				if (d < range) {
					Carnivore newCarn = carn.eatPrey(herb);
					if (newCarn != null)
						carns.add(newCarn);
				}
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
			if (herb.wasEaten())
				herbs.remove(herb);
			else if (herb.isDead()) {
				herbs.remove(herb);
				decomposeOrg(herb);
			}
			else
				i++;
		}

		// removing dead carns
		for (int i = 0; i < carns.size(); i += 0) {
			Carnivore carn = carns.get(i);
			if (carn.isDead()) {
				carns.remove(carn);
				decomposeOrg(carn);
			}
			else
				i++;
		}
		
		// randomly select new organism if selected was removed
		int nh = herbs.size();
		int nc = carns.size();
		int n = nh + nc;
		
		if (n == 0) {
			selected = null;
			selectNew = true;
		}
		
		else if (selected != null && (selected.wasEaten() || selected.isDead()))
			selectRandom();
	}
	
	private void selectRandom() {
		selectNew = false;
		
		int nh = herbs.size();
		int nc = carns.size();
		int n = nh + nc;
		
		if (n == 0) // don't attempt to select a new org if there are none
			return;
		
		int i = (int) (Math.random() * n);
		if (i < nh)
			selected = herbs.get(i);
		else {
			i -= nh;
			selected = carns.get(i);
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

	// counting entities
	public void takeCensus() {
		agarCensus.add(agars.size());
		herbCensus.add(herbs.size());
		carnCensus.add(carns.size());
		
		if (agarCensus.size() > historyLimit) {
			agarCensus.remove(0);
			herbCensus.remove(0);
			carnCensus.remove(0);
		}
	}

	// TOREMOVE: @formatter:off
	public int getTime() { return t; }
	
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
	public ArrayList<Integer> getAgarCensus() { return agarCensus; }
	public ArrayList<Integer> getHerbCensus() { return herbCensus; }
	public ArrayList<Integer> getCarnCensus() { return carnCensus; }
	
}
