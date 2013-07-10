package determinant_calculator_service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * A web service that computes the determinant of a square matrix.
 * 
 */
@WebService(serviceName = "DeterminantCalculatorService")
public class DeterminantCalculatorService {
	
	/**
	 * Operation to compute the determinant of a matrix.
	 * 
	 * @param order the order of the square matrix
	 * @param fileValues the URL to the file that stores the values of the matrix
	 * @return a <code>String</code> that identifies the request
	 */
	@WebMethod(operationName = "computeDeterminant")
	public String computeDeterminant(@WebParam(name = "order") int order,
			@WebParam(name = "fileValues") String fileValues) {
		return DeterminantCalculatorManager.getInstance().computeDeterminant(order, fileValues);
	}
	
	/**
	 * Operation to get an estimation of percentage of a previously requested computation.
	 * 
	 * @param reqId the request of interest
	 * @return an estimation of percentage of the computation
	 */
	@WebMethod(operationName = "getPercentageDone")
	public int getPercentageDone(@WebParam(name = "reqId") String reqId) {
		return DeterminantCalculatorManager.getInstance().getPercentageDone(reqId);
	}
	
	/**
	 * Operation to get the determinant computed.
	 * 
	 * @param reqId the request of interest
	 * @return the determinant computed
	 */
	@WebMethod(operationName = "getResult")
	public double getResult(@WebParam(name = "reqId") String reqId) {
		return DeterminantCalculatorManager.getInstance().getResult(reqId);
	}
	
	/**
	 * Operation to add a worker actor.
	 * 
	 * @param remoteAddress the remote path of the worker actor
	 */
	@WebMethod(operationName = "addWorker")
	public void addWorker(@WebParam(name = "remoteAddress") String remoteAddress) {
		DeterminantCalculatorManager.getInstance().addWorker(remoteAddress);
	}
	
	/**
	 * Operation to remove a worker actor.
	 * 
	 * @param remoteAddress the remote path of the worker actor
	 */
	@WebMethod(operationName = "removeWorker")
	public void removeWorker(@WebParam(name = "remoteAddress") String remoteAddress) {
		DeterminantCalculatorManager.getInstance().removeWorker(remoteAddress);
	}
}
