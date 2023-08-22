import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class PetriDishDriver {
	public static void main(String[] args) {
		JFrame frame = new JFrame("Petri Dish");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // gets resolution
		PetriDishPanel panel = new PetriDishPanel(screenSize.getWidth(), screenSize.getHeight()); // inputs resolution
		frame.add(panel); // panel goes in frame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // window size
		frame.setUndecorated(true); // window bar
		frame.setVisible(true); // window visible
	}
}
