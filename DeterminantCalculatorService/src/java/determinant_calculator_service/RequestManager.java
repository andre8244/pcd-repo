package determinant_calculator_service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A data structure to store the informations about a request.
 * 
 */
public class RequestManager {
	
	private double[][] matrix;
	private int matrixLength;
	private int nRowsDone;
	private long totalWorkToDo;
	private int percentageDone;
	private double tempDeterminant;
	private double finalDeterminant;
	private boolean changeSign;
	private long startTime;
	private CountDownLatch computationEnded;
	private Lock percentageLock;
	
	/**
	 * Constructs the {@code RequestManager}.
	 */
	public RequestManager() {
		tempDeterminant = 1;
		finalDeterminant = -0.0; // -0.0 is not a valid result
		percentageDone = 0;
		nRowsDone = 0;
		changeSign = false;
		startTime = System.currentTimeMillis();
		computationEnded = new CountDownLatch(1);
		percentageLock = new ReentrantLock(true);
	}
	
	/**
	 * Sets the original matrix of the request and compute the total number of elements to process in all the
	 * submatrices.
	 * 
	 * @param matrix a matrix
	 */
	public void setOriginalMatrix(double[][] matrix) {
		this.matrix = matrix;
		matrixLength = matrix.length;
		totalWorkToDo = 0;
		
		/*
		 * sum the elements of all the submatrices to be computed example: order = 1000 -> totalWorkToDo = (999 * 1000)
		 * + + (998 * 999) + ... + (1 * 2)
		 */
		for (long i = matrixLength - 1; i > 0; i--) {
			totalWorkToDo += i * (i + 1);
		}
	}
	
	/**
	 * Returns {@code true} if the sign of the determinant compute must be inverted, i.e. if the number of times the
	 * Gauss algorithm has swapped two rows is odd.
	 * 
	 * @return
	 */
	public boolean getChangeSign() {
		return changeSign;
	}
	
	/**
	 * Returns a {@code long} that represents the moment the request has been created.
	 * 
	 * @return a {@code long} that represents the moment the request has been created
	 */
	public long getStartTime() {
		return startTime;
	}
	
	/**
	 * Returns the total number of elements to process in all the submatrices.
	 * 
	 * @return the total number of elements to process in all the submatrices
	 */
	public long getTotalWorkToDo() {
		return totalWorkToDo;
	}
	
	/**
	 * Changes the sign the final determinant will have.
	 */
	public void setChangeSign() {
		changeSign = !changeSign;
	}
	
	/**
	 * Sets the determinant computed and awake the threads that were waiting for it.
	 * 
	 * @param finalDeterminant the determinant computed
	 */
	public void setFinalDeterminant(double finalDeterminant) {
		this.matrix = null;
		this.finalDeterminant = finalDeterminant;
		computationEnded.countDown();
	}
	
	/**
	 * Returns the determinant of the request, as soon it is available.
	 * 
	 * @return the determinant computed
	 */
	public double getFinalDeterminant() {
		try {
			computationEnded.await();
			return finalDeterminant;
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			return -0.0;
		}
	}
	
	/**
	 * Sets the percentage of the request.
	 * 
	 * @param percentage an {@code int} between 1 and 100
	 */
	public void setPercentageDone(int percentage) {
		percentageLock.lock();
		try {
			percentageDone = percentage;
		} finally {
			percentageLock.unlock();
		}
	}
	
	/**
	 * Returns the percentage of the request.
	 * 
	 * @return the percentage of the request
	 */
	public int getPercentageDone() {
		percentageLock.lock();
		try {
			return percentageDone;
		} finally {
			percentageLock.unlock();
		}
	}
	
	/**
	 * Replaces a block of rows of the matrix with the block of rows computed by a worker.
	 * 
	 * @param rows the block of rows computed by a worker
	 * @param rowNumber the index of the first row of the block
	 */
	public void updateCurrentMatrix(double[][] rows, int rowNumber) {
		System.arraycopy(rows, 0, matrix, rowNumber, rows.length);
	}
	
	/**
	 * Returns the order of the current matrix.
	 * 
	 * @return the order of the current matrix
	 */
	public int getCurrentOrder() {
		return matrix.length;
	}
	
	/**
	 * Replace the current matrix with the submatrix obtained removing the first row and the first column.
	 */
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
	
	/**
	 * Multiplies the current temporary determinant by the first element of the current matrix.
	 */
	public void updateTempDeterminant() {
		tempDeterminant = tempDeterminant * matrix[0][0];
	}
	
	/**
	 * Multiplies the current temporary determinant by the 2nd element of the 2nd element of the current 2x2 matrix,
	 * obtaining the absolute value of the final determinant.
	 * 
	 * @return the absolute value of the final determinant
	 */
	public double updateLastTempDeterminant() {
		tempDeterminant = tempDeterminant * matrix[1][1];
		return tempDeterminant;
	}
	
	/**
	 * Tries to swap the first row of the current matrix with the first row that has the first element not null.
	 * 
	 * @return {@code true} if a row that has the first element not null is found, {@code false} otherwise
	 */
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
	
	/**
	 * Updates the number of rows processed by the workers.
	 * 
	 * @param n the number of rows just processed
	 * @return the updated number of rows processed
	 */
	public int updateRowsDone(int n) {
		nRowsDone = nRowsDone + n;
		return nRowsDone;
	}
	
	/**
	 * Returns the first row of the current matrix.
	 * 
	 * @return the first row of the current matrix
	 */
	public double[] getFirstRow() {
		return matrix[0];
	}
	
	/**
	 * Returns the first element of the current matrix.
	 * 
	 * @return the first element of the current matrix
	 */
	public double getFirstElement() {
		return matrix[0][0];
	}
	
	/**
	 * Returns a block of rows of the current matrix.
	 * 
	 * @param size the number of rows of the block
	 * @param rowNumber the index of the first row of the block
	 * @return a block of rows of the current matrix
	 */
	public double[][] getRows(int size, int rowNumber) {
		double[][] rows = new double[size][matrix.length];
		
		for (int i = 0; i < rows.length; i++) {
			rows[i] = matrix[rowNumber + i];
		}
		return rows;
	}
	
}