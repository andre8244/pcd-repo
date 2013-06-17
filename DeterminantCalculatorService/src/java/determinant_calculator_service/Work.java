package determinant_calculator_service;

public class Work {

	private String reqId;
	private double[][] rows;
	private int rowNumber;

	public Work(String reqId, double[][] rows, int rowNumber) {
		this.reqId = reqId;
		this.rows = rows;
		this.rowNumber = rowNumber;
	}
	
	public String getReqId(){
		return reqId;
	}
	
	public double[][] getRows(){
		return rows;
	}
		
	public int getRowNumber(){
		return rowNumber;
	}
	
}