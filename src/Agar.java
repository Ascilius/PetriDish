import java.awt.Color;

public class Agar implements Entity {

	// dish
	protected PetriDish dish;

	// state
	protected double x, y;
	protected Color color;
	protected double calories = 0.5;
	protected boolean eaten = false;

	public Agar(PetriDish dish, double x, double y) {
		this.dish = dish;

		this.x = x;
		this.y = y;
		this.color = Color.GREEN;
	}

	public double eaten() {
		eaten = true;
		return calories;
	}

	public boolean wasEaten() {
		return eaten;
	}

	// TOREMOVE: @formatter:off
	// dish
	public PetriDish getDish() { return dish; }
		
	// state
	public double getX() { return x; }
	public double getY() { return y; }
	public Color getColor() { return color; }
		
}
