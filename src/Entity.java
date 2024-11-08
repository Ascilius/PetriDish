import java.awt.Color;

public interface Entity {
	// TOREMOVE: @formatter:off
	public PetriDish getDish();

	public double getX();
	public double getY();
	
	public Color getColor();
	
	public double eaten(); // food was eaten
	public boolean wasEaten(); // returns whether food was eaten
}
