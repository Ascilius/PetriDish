
public class Brain {

	double[][][] weights = { new double[3][3], new double[3][3], new double[2][3] };

	public Brain() {
		int il = weights.length;
		for (int i = 0; i < il; i++) {
			int jl = weights[i].length;
			for (int j = 0; j < jl; j++) {
				int kl = weights[i][j].length;
				for (int k = 0; k < kl; k++) {
					weights[i][j][k] = Math.random() - 0.5;
				}
			}
		}
	}

	public Brain(Brain parent) {
		double[][][] parentWeights = parent.getWeights();
		int il = weights.length;
		for (int i = 0; i < il; i++) {
			int jl = weights[i].length;
			for (int j = 0; j < jl; j++) {
				int kl = weights[i][j].length;
				for (int k = 0; k < kl; k++) {
					weights[i][j][k] = parentWeights[i][j][k] + (Math.random() - 0.5) * 0.1;
				}
			}
		}
	}

	public double[] multiply(double[][] weights, double[] inputs) {
		int il = weights.length;
		double[] outputs = new double[il];

		for (int i = 0; i < il; i++) {
			int jl = weights[i].length;
			for (int j = 0; j < jl; j++)
				outputs[i] += weights[i][j] * inputs[j];
		}

		return outputs;
	}

	public double[] doStuff(double[] inputs) {
		// input layer
		double[] layer1 = multiply(weights[0], inputs);

		// middle layers
		double[] layer2 = multiply(weights[1], layer1);

		// output layer
		double[] outputs = multiply(weights[2], layer2);
		return outputs;
	}

	public double[][][] getWeights() {
		return weights;
	}

}
