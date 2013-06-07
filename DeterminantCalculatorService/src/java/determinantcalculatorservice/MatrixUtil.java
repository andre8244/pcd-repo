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
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException ex) {
			Logger.getLogger(MatrixUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		// TODO
		return null;
	}

	public static HashMap<Integer,HashMap<Integer, Double>> fromFileToHashMap(String fileName){
		// TODO
		return null;
	}
}
