
public class Food {

	private double x, y;
	private boolean eaten = false;

	public Food() {
		double r = Math.random();
		double a = Math.random() * 2 * Math.PI;
		this.x = r * Math.cos(a);
		this.y = r * Math.sin(a);
	}

	public void eat() {
		eaten = true;
	}

	public boolean wasEaten() {
		return eaten;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

}
