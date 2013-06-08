package determinantcalculatorservice;

import Log.L;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MatrixUtil {

	private static BufferedWriter writer;
	private static Random rand;

	private static void log(String msg) {
		System.out.println("" + msg);
	}

	public static void genAndWriteToFile(int order, int maxAbs, String fileName) {
		rand = new Random();

		File file = new File(fileName);
		L.log("MATRIXUTIL", file.getAbsolutePath());

		try {
			writer = new BufferedWriter(new FileWriter(fileName));
			double val;
			log("Writing matrix...");

			for (int row = 0; row < order; row++) {
				for (int col = 0; col < order; col++) {
					val = rand.nextDouble() * maxAbs;

					if (rand.nextBoolean()) {
						val = -val;
					}
					writer.write(val + " ");
				}
				writer.newLine();

				if (row % 500 == 0) {
					log((int) ((double) row / order * 100) + " %");
				}
			}
			log("Matrix wrote to file");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static ArrayList<ArrayList<Double>> fromFileToArrayList(String fileName) {
		ArrayList<ArrayList<Double>> matrix = new ArrayList<ArrayList<Double>>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = reader.readLine();
			StringTokenizer tokenizer;

			while (line != null) {
				tokenizer = new StringTokenizer(line, " ");
				ArrayList<Double> row = new ArrayList<Double>();
				matrix.add(row);

				while (tokenizer.hasMoreTokens()) {
					row.add(Double.parseDouble(tokenizer.nextToken()));
				}
				line = reader.readLine();
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		testMatrix(matrix);
		return matrix;
	}

	public static HashMap<Integer, HashMap<Integer, Double>> fromFileToHashMap(String fileName) {
		HashMap<Integer, HashMap<Integer, Double>> matrix = new HashMap<Integer, HashMap<Integer, Double>>();
		int i;
		int j;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = reader.readLine();
			StringTokenizer tokenizer;
			i = 0;

			while (line != null) {
				tokenizer = new StringTokenizer(line, " ");
				HashMap<Integer, Double> row = new HashMap<Integer, Double>();
				matrix.put(i, row);
				j = 0;

				while (tokenizer.hasMoreTokens()) {
					row.put(j, Double.parseDouble(tokenizer.nextToken()));
					j++;
				}
				line = reader.readLine();
				i++;
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		testMatrix(matrix);
		return matrix;
	}

	public static void printMatrix(ArrayList<ArrayList<Double>> matrix) {
		for (ArrayList<Double> row : matrix) {
			for (Double elem : row) {
				System.out.print(elem + " ");
			}
			System.out.print("\n");
		}
	}

	private static void testMatrix(ArrayList<ArrayList<Double>> matrix) {
		L.log("matrix util", "matrix size: "+matrix.size());
	}

	private static void testMatrix(HashMap<Integer, HashMap<Integer, Double>> matrix) {
		
	}
}
