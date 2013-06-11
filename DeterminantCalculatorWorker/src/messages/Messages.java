package messages;

import java.util.ArrayList;
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
}
