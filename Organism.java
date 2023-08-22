import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

public class Organism {

	// physics
	private double sx, sy;
	private double r;
	private double dir;
	private double speed = 0.1;
	private double turn = Math.PI / 16;
	private ArrayList<double[]> path = new ArrayList<>();
	private int pathLength = -1; // -1 = unlimited

	// attributes
	private Color color;

	// bounds
	private Rectangle bounds = new Rectangle();

	public Organism(double sx, double sy, double r, double dir) {
		// physics
		this.sx = sx;
		this.sy = sy;
		this.r = r;
		this.dir = dir;

		// attributes
		color = new Color((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256));
	}

	public void move(double timeStep, double bound) {
		double[] previous = { this.sx, this.sy };
		this.path.add(previous);
		if (pathLength != -1 && path.size() > pathLength) {
			path.remove(0);
		}
		this.turn();
		sx += speed * (1000 / timeStep) * Math.cos(dir);
		sy += speed * (1000 / timeStep) * Math.sin(dir);
		// bounds check
		double r = Math.sqrt(Math.pow(sx, 2) + Math.pow(sy, 2));
		double a = Math.atan(sy / sx);
		if (r > bound) {
			if (sx < 0)
				a += Math.PI;
			sx = bound * Math.cos(a);
			sy = bound * Math.sin(a);
		}
	}

	public void turn() {
		if (Math.random() > 0.5) {
			dir += turn;
		}
		if (Math.random() > 0.5) {
			dir -= turn;
		}
		overflow();
	}

	public void overflow() {
		if (dir >= (2 * Math.PI)) {
			dir -= 2 * Math.PI;
		} else if (dir < 0) {
			dir += 2 * Math.PI;
		}
	}

	public boolean clicked(double mouseX, double mouseY) {
		bounds = new Rectangle((int) (sx - r), (int) (sy - r), (int) (r * 2), (int) (r * 2));
		return bounds.contains(new Point((int) mouseX, (int) mouseY));
	}

	public double getSX() {
		return sx;
	}

	public double getSY() {
		return sy;
	}

	public double getR() {
		return r;
	}

	public double getDir() {
		return dir;
	}

	public Color getColor() {
		return color;
	}

	public Rectangle getBounds() {
		return bounds;
	}

	public ArrayList getPath() {
		return path;
	}
}
