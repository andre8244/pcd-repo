package messages;

import java.io.Serializable;

/**
 * The messages used by every master actor.
 * 
 */
public class Messages {
	
	/**
	 * A message sent from a {@code MatrixReaderThread} to a master in order to request a new computation.
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
	 * A message sent from the {@code DeterminantCalculatorManager} to a master to inform him a new worker has joined.
	 * 
	 */
	public static class AddWorker implements Serializable {
		private final String remoteAddress;
		
		public AddWorker(String remoteAddress) {
			this.remoteAddress = remoteAddress;
		}
		
		public String getRemoteAddress() {
			return remoteAddress;
		}
	}
	
	/**
	 * A message sent from the {@code DeterminantCalculatorManager} to a master to inform him a worker has left the
	 * system.
	 * 
	 */
	public static class RemoveWorker implements Serializable {
		private final String remoteAddress;
		
		public RemoveWorker(String remoteAddress) {
			this.remoteAddress = remoteAddress;
		}
		
		public String getRemoteAddress() {
			return remoteAddress;
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
