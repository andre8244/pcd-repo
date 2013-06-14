package determinant_calculator_service;

import log.l;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class MatrixUtil {

	private static BufferedWriter writer;
	private static Random rand;
	private static String me = "matrixUtil";

	public static double[][] fromFileToList(int order, String fileName) {
		l.l(me, "writing list");
		long startTime = System.currentTimeMillis();

		double[][] matrix = new double[order][order];

		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = reader.readLine();
			String[] tokens;
			int i = 0;

			while (line != null) {
				if (i % 500 == 0) {
					l.l(me, "wrote " + i + " lines in list");
				}
				tokens = line.split(" ");

				for (int j = 0; j < tokens.length; j++) {
					matrix[i][j] = Double.parseDouble(tokens[j]);
				}
				line = reader.readLine();
				i++;
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		l.l(me, "finished writing list " + ((System.currentTimeMillis() - startTime) / (double) 1000) + " sec");
		startTime = System.currentTimeMillis();
		testReadMatrix(matrix);
		l.l(me, "finished reading list " + ((System.currentTimeMillis() - startTime) / (double) 1000) + " sec");
		//printMatrix(matrix);
		return matrix;
	}

	// TODO eliminare metodo
	private static void testReadMatrix(double[][] matrix) {
		long dummy = 0;

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				dummy++;
			}
		}
	}

	// TODO rimuovere metodo
	public static void printMatrix(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.print(matrix[i][j] + " ");
			}
			System.out.print("\n");
		}
	}
	
	/*public static void genAndWriteToFile(int order, int maxAbs, String fileName) {
	 long startTime = System.currentTimeMillis();
	 rand = new Random();

	 File file = new File(fileName);
	 l.l("MATRIXUTIL", file.getAbsolutePath());

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
	 if (col==order-1){
	 writer.write(val + "\n");
	 } else {
	 writer.write(val + " ");
	 }
	 }

	 if (row % 500 == 0) {
	 log((int) ((double) row / order * 100) + " %");
	 }
	 }
	 log("Matrix wrote to file, duration: " + ((System.currentTimeMillis() - startTime) / (double) 1000) + " sec");
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
	 }*/

	/*public static ArrayList<ArrayList<Double>> fromFileToArrayList(String fileName) {
	 l.l(me, "writing arraylist");
	 long startTime = System.currentTimeMillis();

	 ArrayList<ArrayList<Double>> matrix = new ArrayList<ArrayList<Double>>();

	 try {
	 BufferedReader reader = new BufferedReader(new FileReader(fileName));
	 String line = reader.readLine();
	 StringTokenizer tokenizer;
	 ArrayList<Double> row;
	 int lineNumber = 0;

	 while (line != null) {
	 tokenizer = new StringTokenizer(line, " ");
	 row = new ArrayList<Double>();
	 lineNumber++;

	 if (lineNumber % 500 == 0){
	 l.l(me, "wrote " + lineNumber + " lines in arraylist");
	 }

	 while (tokenizer.hasMoreTokens()) {
	 row.add(Double.parseDouble(tokenizer.nextToken()));
	 }
	 matrix.add(row);
	 line = reader.readLine();
	 }
	 } catch (FileNotFoundException ex) {
	 ex.printStackTrace();
	 } catch (IOException ex) {
	 ex.printStackTrace();
	 }
	 l.l(me, "finished writing arraylist " + ((System.currentTimeMillis() - startTime) / (double) 1000) + " sec");
	 startTime = System.currentTimeMillis();
	 testReadMatrix(matrix);
	 l.l(me, "finished reading arraylist " + ((System.currentTimeMillis() - startTime) / (double) 1000) + " sec");
	 return matrix;
	 }*/

	/*public static HashMap<Integer, HashMap<Integer, Double>> fromFileToHashMap(String fileName) {
	 l.l(me, "writing hashmap");
	 long startTime = System.currentTimeMillis();

	 HashMap<Integer, HashMap<Integer, Double>> matrix = new HashMap<Integer, HashMap<Integer, Double>>();
	 int i;
	 int j;

	 try {
	 BufferedReader reader = new BufferedReader(new FileReader(fileName));
	 String line = reader.readLine();
	 StringTokenizer tokenizer;
	 i = 0;

	 while (line != null) {
	 if (i % 500 == 0){
	 l.l(me, "wrote " + i + " lines in hashmap");
	 }
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
	 l.l(me, "finished writing hashmap " + ((System.currentTimeMillis() - startTime) / (double) 1000) + " sec");
	 startTime = System.currentTimeMillis();
	 testReadMatrix(matrix);
	 l.l(me, "finished reading hashmap " + ((System.currentTimeMillis() - startTime) / (double) 1000) + " sec");
	 return matrix;
	 }*/

	/*public static void printMatrix(ArrayList<ArrayList<Double>> matrix) {
	 for (ArrayList<Double> row : matrix) {
	 for (Double elem : row) {
	 System.out.print(elem + " ");
	 }
	 System.out.print("\n");
	 }
	 }*/

	/*private static void testReadMatrix(ArrayList<ArrayList<Double>> matrix) {
	 long dummy = 0;

	 for (int i = 0; i < matrix.size(); i++) {
	 for (int j = 0; j < matrix.get(i).size(); j++) {
	 dummy++;
	 }
	 }
	 }

	 /*private static void testReadMatrix(HashMap<Integer, HashMap<Integer, Double>> matrix) {
	 long dummy = 0;

	 for (int i = 0; i < matrix.size(); i++) {
	 for (int j = 0; j < matrix.get(i).size(); j++) {
	 dummy++;
	 }
	 }
	 }*/
}
