package determinant_calculator_service;

/**
 * A data structure that stores the informations about a job to be performed by a worker actor. A job represents a
 * portion of a matrix. The Gaussian elimination algorithm is executed on every portion of a matrix by a worker actor.
 * 
 */
public class GaussJob {
	
	private String reqId;
	private int nRows;
	private int rowNumber;
	
	/**
	 * Constructs the data structure.
	 * 
	 * @param reqId the request the job belongs to
	 * @param rows the set of rows of the job
	 * @param rowNumber the index of the first row of the job
	 */
	public GaussJob(String reqId, int nRows, int rowNumber) {
		this.reqId = reqId;
		this.nRows = nRows;
		this.rowNumber = rowNumber;
	}
	
	/**
	 * Returns the request the job belongs to.
	 * 
	 * @return the request the job belongs to
	 */
	public String getReqId() {
		return reqId;
	}
	
	/**
	 * Returns the set of rows of the job.
	 * 
	 * @return the set of rows of the job
	 */
	public int getNRows() {
		return nRows;
	}
	
	/**
	 * Returns the index of the first row of the job.
	 * 
	 * @return the index of the first row of the job
	 */
	public int getRowNumber() {
		return rowNumber;
	}
	
}