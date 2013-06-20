package determinant_calculator_service;

import java.net.URL;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 *
 */
@WebService(serviceName = "DeterminantCalculatorService")
public class DeterminantCalculatorService {

	/**
	 * Operazione per calcolare il determinante
	 */
	@WebMethod(operationName = "computeDeterminant") // TODO sistemare il tipo di fileValues
	public String computeDeterminant(@WebParam(name = "order") int order, @WebParam(name = "fileValues") String fileValues) {
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
	@WebMethod(operationName = "addWorkerNode")
	public boolean addWorkerNode(@WebParam(name = "remoteAddress") String remoteAddress) {
		return DeterminantCalculatorManager.getInstance().addWorkerNode(remoteAddress);
	}

	/**
	 * Rimozione di un worker dal servizio
	 */
	@WebMethod(operationName = "removeWorkerNode")
	public boolean removeWorkerNode(@WebParam(name = "remoteAddress") String remoteAddress) {
		return DeterminantCalculatorManager.getInstance().removeWorkerNode(remoteAddress);
	}
}
