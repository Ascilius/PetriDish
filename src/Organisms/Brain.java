package Organisms;

public class Brain {
	
	private static boolean DEBUG = true;
	
	private static double MUT = 0.1; // maximum change for each weight
	protected static int Iden = 0, ReLU = 1, Sigm = 2, Tanh = 3;
	
	private int[] structure;
	private int[] acts;
	private int numLayers;
	private double[][][] weights; // for each space between layers, have a matrix representing the weights
	
	private static void showWeights(Brain brain) {
		int numLayers = brain.numLayers();
		double[][][] weights = brain.getWeights();
		
		for (int l = 1; l <= numLayers; l++) {
			double[][] wl = weights[l-1];
			int dj = wl.length;
			int di = wl[0].length;
			System.out.printf("DEBUG: W%d: %dx%d\n", l, dj, di);
			
			for (int j = 0; j < dj; j++) {
				for (int i = 0; i < di; i++)
					System.out.printf("%.2f\t", wl[j][i]);
				System.out.println();
			}
		}
	}
	
	public Brain(int[] struct, int[] acts) {
		structure = struct;
		this.acts = acts;
		numLayers = struct.length - 1;
		weights = new double[numLayers][][];
		
		for (int l = 1; l <= numLayers; l++) {
			int di = struct[l-1] + 1; // +1 for bias node
			int dj = struct[l];
			
			double[][] wl = new double[dj][di];
			for (int i = 0; i < di; i++) {
				for (int j = 0; j < dj; j++)
					wl[j][i] = Math.random() - 0.5;
			}
			
			weights[l-1] = wl;
		}
		
		/*
		if (DEBUG)
			showWeights(this);
		*/
	}

	public Brain(Brain parent) {
		structure = parent.getStruct();
		this.acts = parent.getActs();
		numLayers = parent.numLayers();
		weights = new double[numLayers][][];
		double[][][] parentWeights = parent.getWeights();
		
		for (int l = 1; l <= numLayers; l++) {
			double[][] pl = parentWeights[l-1];
			int di = pl[0].length;
			int dj = pl.length;
			
			double[][] wl = new double[dj][di];
			for (int i = 0; i < di; i++) {
				for (int j = 0; j < dj; j++)
					wl[j][i] = parentWeights[l-1][j][i] + (Math.random() - 0.5) * MUT;
			}
			
			weights[l-1] = wl;
		}
	}

	public double dotProduct(double[] weights, double[] inputs) {
		int wl = weights.length;
		int il = inputs.length;
		if (wl != il) {
			System.out.printf("ERROR: Brain.dotProduct(): vector lengths are not equal (%d != %d)\n", wl, il);
			System.exit(1);
		}
		
		double dp = 0.0; 
		for (int i = 0; i < il; i++)
			dp += weights[i] * inputs[i];

		return dp;
	}

	public double ReLU(double x) {
		if (x < 0)
			return 0;
		else
			return x;
	}
	
	public double Sigm(double x) { return 1 / (1 + Math.exp(-x)); }
	
	public double Tanh(double x) {
		double r = Math.tanh(x);
			
		if (false && DEBUG)
			System.out.printf("DEBUG: x = %f -> %f\n", x, r);
		
		return r;
	}

	public double[] think(double[] nextInputs) {
		for (int l = 1; l <= numLayers; l++) {
			// adding 1 element for bias
			int len = nextInputs.length;
			double[] augInputs = new double[++len];
			augInputs[0] = 1;
			for (int i = 1; i < len; i++)
				augInputs[i] = nextInputs[i - 1];
			
			nextInputs = augInputs;
			
			// computing the layer
			double[][] wl = weights[l-1]; // weight matrix
			int act = acts[l-1]; // activation function ID
			
			int dj = wl.length;
			double[] outputs = new double[dj];
			
			for (int j = 0; j < dj; j++) {
				double dp = dotProduct(wl[j], nextInputs);
				
				if (act == Iden)
					outputs[j] = dp;
				else if (act == ReLU)
					outputs[j] = ReLU(dp);
				else if (act == Sigm)
					outputs[j] = Sigm(dp);
				else if (act == Tanh)
					outputs[j] = Tanh(dp);
			}
			
			nextInputs = outputs;
		}
		
		if (false && DEBUG)
			System.out.printf("DEBUG: outputs: [%f, %f, %f]\n", nextInputs[0], nextInputs[1], nextInputs[2]);
		
		return nextInputs;
	}

	public int[] getStruct() { return structure; }
	public int[] getActs() { return acts; }
	public int numLayers() { return numLayers; }
	public double[][][] getWeights() { return weights; }

}
