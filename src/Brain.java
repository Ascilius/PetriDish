
public class Brain {

	double[][] weights = new double[3][2];

	public Brain() {
		weights[0][0] = Math.random() - 0.5;
		weights[0][1] = Math.random() - 0.5;
		weights[1][0] = Math.random() - 0.5;
		weights[1][1] = Math.random() - 0.5;
		weights[2][0] = Math.random() - 0.5;
		weights[2][1] = Math.random() - 0.5;
	}

	public Brain(Brain parent) {
		double[][] parentWeights = parent.getWeights();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 2; j++)
				weights[i][j] = parentWeights[i][j] + (Math.random() - 0.5) * 0.1; // "genetic drift"
		}
	}

	// TOFIX: matrix math
	public double[] doStuff(double[] inputs) {
		// layer 1
		double[] layer1 = new double[2];
		layer1[0] = (inputs[0] + inputs[1]) * weights[0][0];
		if (layer1[0] < 0)
			layer1[0] = 0;
		layer1[1] = (inputs[0] + inputs[1]) * weights[0][1];
		if (layer1[1] < 0)
			layer1[1] = 0; // ReLU

		// layer 2
		double[] layer2 = new double[2];
		layer2[0] = (layer1[0] + layer1[1]) * weights[1][0];
		if (layer2[0] < 0)
			layer2[0] = 0;
		layer2[1] = (layer1[0] + layer1[1]) * weights[1][1];
		if (layer2[1] < 0)
			layer2[1] = 0;

		// output layer
		double[] outputs = new double[2];
		outputs[0] = (layer2[0] + layer2[1]) * weights[2][0];
		if (outputs[0] < 0)
			outputs[0] = 0;
		outputs[1] = (layer2[0] + layer2[1]) * weights[2][1];
		if (outputs[1] < 0)
			outputs[1] = 0;

		return outputs;
	}

	public double[][] getWeights() {
		return weights;
	}

}
