package determinant_calculator_service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import log.l;

/**
 * An utility class to read the values of a matrix from a HTTP or local URL.
 *
 */
public class MatrixReader {

	private static String me = "matrixUtil";

	/**
	 * Reads the values of a matrix from a HTTP or local URL
	 *
	 * @param order the order of the matrix
	 * @param filePath the HTTP or local URL of the matrix file
	 * @return an array of array of <code>double </code> containing the values of the matrix
	 */
	public double[][] read(int order, String filePath) {
		l.l(me, "writing list");
		long startTime = System.currentTimeMillis();
		// TODO provati anche: ArrayList<ArrayList<Double>> e HashMap<Integer,HashMap<Integer,Double>> ma meno
		// performanti
		double[][] matrix = new double[order][order];
		BufferedReader reader;

		try {
			if (filePath.contains("http://")) {
				URL url = new URL(filePath);
				reader = new BufferedReader(new InputStreamReader(url.openStream()));
			} else {
				reader = new BufferedReader(new FileReader(filePath));
			}
			String line = reader.readLine();
			String[] tokens;
			int nLines = 0;

			while (line != null) {
				// the number of lines can't be greater than the order
				if (nLines == order) {
					l.l(me, "Order error!");
					return null;
				}

				if (nLines % 500 == 0) {
					l.l(me, "wrote " + nLines + " lines in list");
				}
				tokens = line.split(" ");

				// every line must have <order> elements
				if (tokens.length != order) {
					l.l(me, "Order error!");
					return null;
				}

				for (int j = 0; j < tokens.length; j++) {
					matrix[nLines][j] = Double.parseDouble(tokens[j]);
				}
				line = reader.readLine();
				nLines++;
			}
			reader.close();

			// the number of lines can't be less than the order
			if (order != nLines) {
				l.l(me, "Order error!");
				return null;
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			return null;
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		l.l(me, "finished reading file and writing list: " + ((System.currentTimeMillis() - startTime) / (double) 1000)
				+ " sec");
		return matrix;
	}
}