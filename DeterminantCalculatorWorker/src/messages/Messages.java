package messages;

import java.io.Serializable;

/**
 * The messages used by every worker actor.
 *
 */
public class Messages {

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

	public static class Remove implements Serializable {

		public Remove() {
		}
	}

	public static class AddWorkerAck implements Serializable {

		public AddWorkerAck() {
		}
	}

}