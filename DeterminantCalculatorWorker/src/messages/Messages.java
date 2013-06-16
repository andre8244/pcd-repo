package messages;

import java.io.Serializable;

public class Messages {

	public static class OneRow implements Serializable {

		private final double[] firstRow;
		private final double[] row;
		private final String reqId;
		private final int rowNumber;

		public OneRow(String reqId, double[] firstRow, double[] row, int rowNumber) {
			this.firstRow = firstRow;
			this.row = row;
			this.reqId = reqId;
			this.rowNumber = rowNumber;
		}

		public int getRowNumber(){
			return rowNumber;
		}

		public String getReqId(){
			return reqId;
		}

		public double[] getFirstRow() {
			return firstRow;
		}

		public double[] getRow() {
			return row;
		}
	}

	public static class OneRowResult implements Serializable {

		private final double[] row;
		private final String reqId;
		private final int rowNumber;

		public OneRowResult(String reqId, double[]row, int rowNumber) {
			this.row = row;
			this.reqId = reqId;
			this.rowNumber = rowNumber;
		}

		public int getRowNumber(){
			return rowNumber;
		}

		public String getReqId(){
			return reqId;
		}

		public double[] getRow() {
			return row;
		}
	}

    public static class ManyRows implements Serializable {

		private final double[] firstRow;
		private final double[][] rows;
		private final String reqId;
		private final int rowNumber;

		public ManyRows(String reqId, double[] firstRow, double[][] rows, int rowNumber) {
			this.firstRow = firstRow;
			this.rows = rows;
			this.reqId = reqId;
			this.rowNumber = rowNumber;
		}

		public int getRowNumber(){
			return rowNumber;
		}

		public String getReqId(){
			return reqId;
		}

		public double[] getFirstRow() {
			return firstRow;
		}

		public double[][] getRows() {
			return rows;
		}
	}

    public static class ManyRowsResult implements Serializable {

		private final double[][] rows;
		private final String reqId;
		private final int rowNumber;

		public ManyRowsResult(String reqId, double[][] rows, int rowNumber) {
			this.rows = rows;
			this.reqId = reqId;
			this.rowNumber = rowNumber;
		}

		public int getRowNumber(){
			return rowNumber;
		}

		public String getReqId(){
			return reqId;
		}

		public double[][] getRows() {
			return rows;
		}
	}

	public static class Remove implements Serializable { // TODO da eliminare?

		public Remove() {
		}
	}

	public static class AddWorkerNodeAck implements Serializable {

		public AddWorkerNodeAck() {
		}
	}

	public static class RemoveWorkerNodeAck implements Serializable {

		public RemoveWorkerNodeAck() {
		}
	}
}