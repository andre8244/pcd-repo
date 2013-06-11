/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package determinant_calculator_service;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 * @author Marco
 */
@WebService(serviceName = "DeterminantCalculatorService")
public class DeterminantCalculatorService {

	/**
	 * Operazione per calcolare il determinante
	 */
	@WebMethod(operationName = "computeDeterminant")
	public String computeDeterminant(@WebParam(name = "order") int order, @WebParam(name = "fileValues") java.net.URL fileValues) {
		return DeterminantCalculatorManager.getInstance().computeDeterminant(order, fileValues);
	}

	/**
	 * Operazione per ottenere la percentuale di lavoro effettuato
	 */
	@WebMethod(operationName = "getPercentageDone")
	public int getPercentageDone(@WebParam(name = "reqId") String reqId) {
		return DeterminantCalculatorManager.getInstance().getPercentageDone(reqId);
	}

	/**
	 * Operazione per ottenere il risultato
	 */
	@WebMethod(operationName = "getResult")
	public double getResult(@WebParam(name = "reqId") String reqId) {
		return DeterminantCalculatorManager.getInstance().getResult(reqId);
	}

	/**
	 * Registrazione di un worker al servizio
	 */
	@WebMethod(operationName = "registerWorker")
	public boolean registerWorker(@WebParam(name = "remoteAddress") String remoteAddress) {
		return DeterminantCalculatorManager.getInstance().registerWorker(remoteAddress);
	}

	/**
	 * Rimozione di un worker dal servizio
	 */
	@WebMethod(operationName = "removeWorker")
	public boolean removeWorker(@WebParam(name = "remoteAddress") String remoteAddress) {
		return DeterminantCalculatorManager.getInstance().removeWorker(remoteAddress);
	}
}
