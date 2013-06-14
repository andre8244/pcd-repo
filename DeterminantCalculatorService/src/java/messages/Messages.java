package messages;

import determinant_calculator_service.DeterminantCalculatorManager;
import java.io.Serializable;

public class Messages {

	public static class Compute implements Serializable {

		private final int order;
		private final String fileValues;
		private final String reqId;
		private DeterminantCalculatorManager manager;

		public Compute(int order, String fileValues, String reqId, DeterminantCalculatorManager manager) {
			this.order = order;
			this.fileValues = fileValues;
			this.reqId = reqId;
			this.manager = manager;
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

		public DeterminantCalculatorManager getManager() {
			return manager;
		}
	}

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

	public static class RegisterWorker implements Serializable {

		private final String remoteAddress;

		public RegisterWorker(String remoteAddress) {
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
	
	public static class RegAck implements Serializable {

		public RegAck() {
			
		}
	}
	
	public static class RemAck implements Serializable {

		public RemAck() {
			
		}
	}			
}
