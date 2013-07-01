package determinant_calculator_service;

import log.l;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class MatrixReader {

	private static String me = "matrixUtil";

	// provati anche: ArrayList<ArrayList<Double>> e HashMap<Integer,HashMap<Integer,Double>> ma meno performanti
	public static double[][] read(int order, String filePath) {
		l.l(me, "writing list");
		long startTime = System.currentTimeMillis();
		double[][] matrix = new double[order][order];
		BufferedReader reader;

		try {
			if (filePath.contains("http://")){
				URL url = new URL(filePath);
				reader = new BufferedReader(new InputStreamReader(url.openStream()));
			} else {
				reader = new BufferedReader(new FileReader(filePath));
			}
			String line = reader.readLine();
			String[] tokens;
			int i = 0;
			while (line != null) {
				//gestione errore numero di righe maggiore di order
				if (i==order){
					l.l(me, "Order error!");
					return null;
				}
				if (i % 500 == 0) {
					l.l(me, "wrote " + i + " lines in list");
				}
				tokens = line.split(" ");
				//gestione errore sul numero di colonne
				if (tokens.length!=order){
					l.l(me, "Error order!!!");
					return null;
				}
				for (int j = 0; j < tokens.length; j++) {
					matrix[i][j] = Double.parseDouble(tokens[j]);
				}
				line = reader.readLine();
				i++;
			}
			//gestione errore numero di righe minore di order
			if (order!=i){
				l.l(me, "Error order!!!");
				return null;
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			return null;
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		l.l(me, "finished reading file and writing list: " + ((System.currentTimeMillis() - startTime) / (double) 1000) + " sec");
		return matrix;
	}
}