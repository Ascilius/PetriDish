
public class LOS { // line of sight

	private Food target;
	private boolean valid, left, right;
	
	public LOS(Food target, boolean valid, boolean left, boolean right) {
		this.target = target;
		this.valid = valid;
		this.left = left;
		this.right = right;
	}
	
	public Food getTarget() {
		return target;
	}
	
	public boolean withinFOV() {
		return valid;
	}
	
	public boolean isLeft() {
		return left;
	}
	
	public boolean isRight() {
		return right;
	}
}
