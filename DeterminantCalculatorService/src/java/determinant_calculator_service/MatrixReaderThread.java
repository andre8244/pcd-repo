package determinant_calculator_service;

import akka.actor.ActorRef;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import messages.Messages;

import log.l;

/**
 * An utility class to read the values of a matrix from a HTTP or local URL.
 *
 */
public class MatrixReaderThread extends Thread {

	private int order;
	private String fileValues;
	private String reqId;
	private RequestManager requestManager;
	private ActorRef master;
	private static String me = "matrixReader";

	public MatrixReaderThread(int order, String fileValues, String reqId, RequestManager requestManager, ActorRef master) {
		this.order = order;
		this.fileValues = fileValues;
		this.reqId = reqId;
		this.requestManager = requestManager;
		this.master = master;
	}

	/**
	 * Reads the values of a matrix from a HTTP or local URL
	 *
	 * @param order the order of the matrix
	 * @param filePath the HTTP or local URL of the matrix file
	 * @return an array of array of <code>double </code> containing the values of the matrix
	 */
	@Override
	public void run() {
		l.l(me, reqId + ", writing list");
		long startTime = System.currentTimeMillis();
		double[][] matrix = new double[order][order]; //ArrayList<ArrayList<Double>> e HashMap<Integer,HashMap<Integer,Double>> sono meno performanti
		BufferedReader reader;

		try {
			if (fileValues.contains("http://")) {
				URL url = new URL(fileValues);
				reader = new BufferedReader(new InputStreamReader(url.openStream()));
			} else {
				reader = new BufferedReader(new FileReader(fileValues));
			}
			String line = reader.readLine();
			String[] tokens;
			int nLines = 0;

			while (line != null) {
				// the number of lines can't be greater than the order
				if (nLines == order) {
					l.l(me, reqId + ", Order error!");
					requestManager.setPercentageDone(100);
					requestManager.setFinalDeterminant(-0.0);
					return;
				}

			//	if (nLines % 500 == 0) {
					//l.l(me, reqId + ", wrote " + nLines + " lines in list");
			//	}
				tokens = line.split(" ");

				// every line must have <order> elements
				if (tokens.length != order) {
					l.l(me, reqId + ", Order error!");
					requestManager.setPercentageDone(100);
					requestManager.setFinalDeterminant(-0.0);
					return;
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
				l.l(me, reqId + ", Order error!");
				requestManager.setPercentageDone(100);
				requestManager.setFinalDeterminant(-0.0);
				return;
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			requestManager.setPercentageDone(100);
			requestManager.setFinalDeterminant(-0.0);
			return;
		} catch (IOException ex) {
			ex.printStackTrace();
			requestManager.setPercentageDone(100);
			requestManager.setFinalDeterminant(-0.0);
			return;
		}
		//l.l(me, reqId + ", finished reading file and writing list: " + ((System.currentTimeMillis() - startTime) / (double) 1000)
		//		+ " sec");
		master.tell(new Messages.Compute(reqId, matrix));
	}
}