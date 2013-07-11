package determinant_calculator_user;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * A generator of matrix files.
 * 
 */
public class MatrixFileGenerator {
	
	private BufferedWriter writer;
	
	/**
	 * Generates a new text file containing the values of a matrix.
	 * 
	 * @param order the order of the matrix
	 * @param minAbs the minimum absolute value an element can have
	 * @param maxAbs the maximum absolute value an element can have
	 * @param fileName the full path where the file will be created
	 */
	public void generate(int order, double minAbs, double maxAbs, String fileName) {
		Random rand = new Random();
		
		try {
			writer = new BufferedWriter(new FileWriter(fileName));
			double val;
			
			for (int row = 0; row < order; row++) {
				for (int col = 0; col < order; col++) {
					val = minAbs + rand.nextDouble() * (maxAbs - minAbs);
					if (rand.nextBoolean()) {
						val = -val;
					}
					if (col == order - 1) {
						writer.write(val + "\n");
					} else {
						writer.write(val + " ");
					}
				}
			}
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
}
