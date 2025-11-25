package Organisms;
import java.awt.Color;

public class Agar implements Entity {

	// state
	protected double x, y;
	protected Color color;
	public static double calories = 0.1;
	protected boolean eaten = false;

	public Agar(double x, double y) {
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
		
	// state
	public double getX() { return x; }
	public double getY() { return y; }
	public Color getColor() { return color; }
		
}
