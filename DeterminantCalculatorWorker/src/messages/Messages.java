package messages;

import java.io.Serializable;

/**
 * The messages used by every worker actor.
 * 
 */
public class Messages {
	
	/**
	 * A message sent from a master to a worker in order to process a block of rows of a matrix.
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
	 * A message sent from a worker to a master. It contains a block of rows processed by the worker.
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
	
	/**
	 * A message sent from the {@code WorkerNodeApp} to a worker when a user decides to remove it.
	 * 
	 */
	public static class Remove implements Serializable {
		
		public Remove() {
		}
	}
	
	/**
	 * A message sent from a master to a worker. The message is used to establish a connection between the two actors,
	 * so that the master can be notified when a life cycle event about the worker occurs.
	 * 
	 */
	public static class AddWorkerAck implements Serializable {
		
		public AddWorkerAck() {
		}
	}
	
}