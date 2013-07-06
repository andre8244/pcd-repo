package determinant_calculator_service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import log.l;

public class RequestInfo {

	private String me = "requestInfo";
	private double[][] matrix;
	private int matrixLength;
	private int nRowsDone; // TODO forse meglio rinominare
	private long totalWorkToDo;
	private int percentageDone;
	private double tempDeterminant;
	private double finalDeterminant;
	private boolean changeSign;
	private long startTime;
	private CountDownLatch computationEnded;
	private static Lock lock;

	public RequestInfo() {
		tempDeterminant = 1;
		finalDeterminant = -0.0; // -0.0 is not a valid result
		percentageDone = 0;
		nRowsDone = 0;
		changeSign = false;
		startTime = System.currentTimeMillis();
		computationEnded = new CountDownLatch(1);
		lock = new ReentrantLock(true);
	}

	public boolean setOriginalMatrix(double[][] matrix) {
		if (matrix == null){
			return false;
		}
		this.matrix = matrix;
		matrixLength = matrix.length;
		totalWorkToDo = 0;

		/*
		 * sum the elements of all the submatrices to be computed example: order = 1000 -> totalWorkToDo = (999 * 1000)
		 * + (998 * 999) + ... + (1 * 2)
		 */
		long startTime = System.currentTimeMillis();

		for (long i = matrixLength; i > 0; i--) {
			totalWorkToDo += i * (i + 1);
		}
		l.l(me,
				"setOriginalMatrix: computed totalWorkTodo (" + totalWorkToDo + "). Duration: "
						+ (System.currentTimeMillis() - startTime) + " ms");
		return true;
	}

	public boolean getChangeSign() {
		return changeSign;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getTotalWorkToDo() {
		return totalWorkToDo;
	}

	public void setChangeSign() {
		changeSign = !changeSign;
	}

	public void setFinalDeterminant(double finalDeterminant) {
		this.matrix = null;
		this.finalDeterminant = finalDeterminant;
		computationEnded.countDown();
	}

	public double getFinalDeterminant() {
		try {
			computationEnded.await();
			return finalDeterminant;
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			return -0.0;
		}
	}

	public void setPercentageDone(int percentage) {
		lock.lock();
		try {
			percentageDone = percentage;
		} finally {
			lock.unlock();
		}
	}

	public int getPercentageDone() {
		lock.lock();
		try {
			return percentageDone;
		} finally {
			lock.unlock();
		}
	}

	public void updateCurrentMatrix(double[] row, int rowNumber) {
		matrix[rowNumber] = row;
	}
	
	public void updateCurrentMatrix(double[][] rows, int rowNumber) {
		System.arraycopy(rows, 0, matrix, rowNumber, rows.length);
	}

	public int getCurrentOrder() {
		return matrix.length;
	}

	public void subMatrix() {
		double[][] subMatrix = new double[matrix.length - 1][matrix.length - 1];

		for (int i = 0; i < subMatrix.length; i++) {
			for (int j = 0; j < subMatrix.length; j++) {
				subMatrix[i][j] = matrix[i + 1][j + 1];
			}
		}
		matrix = subMatrix;
		nRowsDone = 0;
	}
	
	public void updateTempDeterminant() {
		tempDeterminant = tempDeterminant * matrix[0][0];
	}

	public double updateLastTempDeterminant() {
		tempDeterminant = tempDeterminant * matrix[1][1];
		return tempDeterminant;
	}
	
	public boolean swapFirtsRow() {
		for (int i = 1; i < matrix.length; i++) {
			if (matrix[i][0] != 0) {
				double[] tempRow = matrix[i];
				matrix[i] = matrix[0];
				matrix[0] = tempRow;
				return true;
			}
		}
		return false;
	}
		
	public int updateRowsDone(int n) {
		nRowsDone = nRowsDone + n;
		return nRowsDone;
	}
	
	public double[] getFirstRow() {
		return matrix[0];
	}
	
	public double getFirstElement() {
		return matrix[0][0];
	}
		
	
	public double[] getRow(int rowNumber) {
		return matrix[rowNumber];
	}
	
	public double[][] getRows(int size, int rowNumber) {
		double[][] rows = new double[size][matrix.length];
		for (int i = 0; i < rows.length; i++) {
			rows[i] = matrix[rowNumber + i];
		}
		return rows;
	}

}