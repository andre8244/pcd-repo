/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package determinantcalculatorservice;

import java.net.URL;
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
    public String computeDeterminant(@WebParam(name = "order") int order, @WebParam(name = "fileValues") URL fileValues) {
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
    public boolean registerWorker(@WebParam(name = "name") String name, @WebParam(name = "ip") String ip, @WebParam(name = "port") int port) {
        return DeterminantCalculatorManager.getInstance().registerWorker(name,ip,port);
    }

    /**
     * Rimozione di un worker dal servizio
     */
    @WebMethod(operationName = "removeWorker")
    public boolean removeWorker(@WebParam(name = "name") String name, @WebParam(name = "ip") String ip, @WebParam(name = "port") int port) {
        return DeterminantCalculatorManager.getInstance().removeWorker(name,ip,port);
    }
}
