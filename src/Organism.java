import java.awt.Color;
import java.util.ArrayList;

public class Organism {

	// physics
	private double sx, sy;
	// private double r;
	private double dir;
	private double speed = 10e-5;
	private double turn = Math.PI / 16;
	private ArrayList<double[]> path = new ArrayList<>();
	private int pathLength = 50; // -1 = unlimited

	// attributes
	private Color color;
	private double FOV = Math.PI / 3; // radians

	public Organism(double sx, double sy, double dir) {
		// physics
		this.sx = sx;
		this.sy = sy;
		// this.r = r;
		this.dir = dir;

		// attributes
		color = new Color((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256));
	}

	public void move(long timeStep) { // timeStep in ms
		// organism path
		double[] previous = { this.sx, this.sy };
		this.path.add(previous);
		if (pathLength != -1 && path.size() > pathLength) {
			path.remove(0);
		}
		
		// moving
		this.turn(); // random turn
		sx += speed * (1000 / timeStep) * Math.cos(dir);
		sy += speed * (1000 / timeStep) * Math.sin(dir);
		
		// bounds check
		double r = Math.sqrt(Math.pow(sx, 2) + Math.pow(sy, 2));
		double a = Math.atan(sy / sx);
		if (r > 1) {
			if (sx < 0)
				a += Math.PI;
			sx = Math.cos(a);
			sy = Math.sin(a);
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

	public double getSX() {
		return sx;
	}

	public double getSY() {
		return sy;
	}
	
	/*
	public double getR() {
		return r;
	}
	*/

	public double getDir() {
		return dir;
	}

	public Color getColor() {
		return color;
	}

	public ArrayList<double[]> getPath() {
		return path;
	}
	
	public double getFOV() {
		return FOV;
	}
}
