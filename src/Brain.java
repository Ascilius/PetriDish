
public class Brain {

	double[][][] weights = { new double[6][7], new double[5][6], new double[4][5], new double[3][4] };

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

	public void ReLU(double[] layer) {
		int size = layer.length;
		for (int i = 0; i < size; i++) {
			if (layer[i] < 0)
				layer[i] = 0;
		}
	}

	public double[] think(double[] inputs) {
		// input layer
		double[] prevLayer = multiply(weights[0], inputs);
		ReLU(prevLayer);

		// middle layers
		int nml = weights.length - 2; // number of middle layers
		for (int i = 1; i < nml + 1; i++) {
			double[] layer = multiply(weights[i], prevLayer);
			ReLU(layer);
			prevLayer = layer;
		}

		// output layer
		double[] outputs = multiply(weights[weights.length - 1], prevLayer);
		ReLU(outputs);
		return outputs;
	}

	public double[][][] getWeights() {
		return weights;
	}

}
