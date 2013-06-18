package determinant_calculator_service;

import java.util.concurrent.CountDownLatch;
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

	public RequestInfo() {
		tempDeterminant = 1;
		finalDeterminant = -0.0; // -0.0 is not a valid result
		percentageDone = 0;
		nRowsDone = 0;
		changeSign = false;
		startTime = System.currentTimeMillis(); // TODO a cosa serve?
		computationEnded = new CountDownLatch(1);
	}

	public void setOriginalMatrix(double[][] matrix) {
		this.matrix = matrix;
		matrixLength = matrix.length;
		totalWorkToDo = 0;

		/* sums the elements of all the submatrices to be computed
		 * example: order = 1000 -> totalWorkToDo = (999 * 1000) + (998 * 999) + ... + (1 * 2)
		 */
		long startTime = System.currentTimeMillis();
		for (long i = matrixLength; i > 0; i--) {
			//l.l(me, "setOriginalMatrix: i("+i+")");
			totalWorkToDo += i * (i + 1);
			//l.l(me, "totalWorkToDo += i * (i + 1) = " + totalWorkToDo);
		}
		l.l(me, "setOriginalMatrix: computed totalWorkTodo (" + totalWorkToDo + "). Duration: " + (System.currentTimeMillis() - startTime) + " ms");
	}

	public void setMatrix(double[][] matrix) {
		this.matrix = matrix;

	}

	public double[][] getMatrix() {
		return matrix;
	}

	public int getRowsDone() {
		return nRowsDone;
	}

	public double getTempDeterminant() {
		return tempDeterminant;
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

	public void setTempDeterminant(double tempDeterminant) {
		this.tempDeterminant = tempDeterminant;
	}

	public void setRowsDone(int nRowsDone) {
		this.nRowsDone = nRowsDone;
	}

	public void setChangeSign() {
		changeSign = !changeSign;
	}

	public void setFinalDeterminant(double finalDeterminant) {
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

	public void setPercentageDone(int percentage){
		percentageDone = percentage;
	}

	public int getPercentageDone(){
		return percentageDone;
	}
}
