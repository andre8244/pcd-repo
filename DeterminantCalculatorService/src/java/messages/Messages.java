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
		
		private final int order;
		private final String fileValues;
		private final String reqId;
		
		public Compute(int order, String fileValues, String reqId) {
			this.order = order;
			this.fileValues = fileValues;
			this.reqId = reqId;
		}
		
		public int getOrder() {
			return order;
		}
		
		public String getFileValues() {
			return fileValues;
		}
		
		public String getReqId() {
			return reqId;
		}
	}
	
	// TODO eliminare
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
		
		public int getRowNumber() {
			return rowNumber;
		}
		
		public String getReqId() {
			return reqId;
		}
		
		public double[] getFirstRow() {
			return firstRow;
		}
		
		public double[] getRow() {
			return row;
		}
	}
	
	// TODO eliminare
	public static class OneRowResult implements Serializable {
		private final double[] row;
		private final String reqId;
		private final int rowNumber;
		
		public OneRowResult(String reqId, double[] row, int rowNumber) {
			this.row = row;
			this.reqId = reqId;
			this.rowNumber = rowNumber;
		}
		
		public int getRowNumber() {
			return rowNumber;
		}
		
		public String getReqId() {
			return reqId;
		}
		
		public double[] getRow() {
			return row;
		}
	}
	
	/**
	 * A message sent to a worker in order to process a portion of a matrix.
	 * 
	 */
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
	public static class ManyRowsResult implements Serializable {
		private final double[][] rows;
		private final String reqId;
		private final int rowNumber;
		
		public ManyRowsResult(String reqId, double[][] rows, int rowNumber) {
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
	
	public static class RemoveWorkerAck implements Serializable {
		public RemoveWorkerAck() {
		}
	}
}
