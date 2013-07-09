package messages;

import java.io.Serializable;

/**
 * The messages used by every master actor.
 *
 */
public class Messages {

	/**
	 * A message sent to a master in order to request a new computation.
	 *
	 */
	public static class Compute implements Serializable {

		private final String reqId;
		private final double[][] matrix;

		public Compute(String reqId, double[][] matrix) {
			this.reqId = reqId;
			this.matrix = matrix;
		}

		public String getReqId() {
			return reqId;
		}

		public double[][] getMatrix() {
			return matrix;
		}
	}

	/**
	 * A message sent to a worker in order to process a portion of a matrix.
	 *
	 */
	public static class Rows implements Serializable {
		private final double[] firstRow;
		private final double[][] rows;
		private final String reqId;
		private final int rowNumber;

		public Rows(String reqId, double[] firstRow, double[][] rows, int rowNumber) {
			this.firstRow = firstRow;
			this.rows = rows;
			this.reqId = reqId;
			this.rowNumber = rowNumber;
		}

		public int getRowNumber() {
			return rowNumber;
		}

		public String getReqId() {
			return reqId;
		}

		public double[] getFirstRow() {
			return firstRow;
		}

		public double[][] getRows() {
			return rows;
		}
	}

	/**
	 * A message sent from a worker to a master in order to aggregate the results of the Gaussian elimination algorithm.
	 *
	 */
	public static class RowsResult implements Serializable {
		private final double[][] rows;
		private final String reqId;
		private final int rowNumber;

		public RowsResult(String reqId, double[][] rows, int rowNumber) {
			this.rows = rows;
			this.reqId = reqId;
			this.rowNumber = rowNumber;
		}

		public int getRowNumber() {
			return rowNumber;
		}

		public String getReqId() {
			return reqId;
		}

		public double[][] getRows() {
			return rows;
		}
	}

	public static class AddWorker implements Serializable {
		private final String remoteAddress;

		public AddWorker(String remoteAddress) {
			this.remoteAddress = remoteAddress;
		}

		public String getRemoteAddress() {
			return remoteAddress;
		}
	}

	public static class RemoveWorker implements Serializable {
		private final String remoteAddress;

		public RemoveWorker(String remoteAddress) {
			this.remoteAddress = remoteAddress;
		}

		public String getRemoteAddress() {
			return remoteAddress;
		}
	}

	public static class AddWorkerAck implements Serializable {
		public AddWorkerAck() {
		}
	}

}
