package determinant_calculator_user;

// IMPORT DEL WEB SERVICE CLIENT:
import localhost_client.*;
//import marco_client.*;
//import andreaf_client.*;
//import leardini_mac.*;

/**
 * A thread that requests a determinant calculation adopting a synchronous strategy.
 *
 */
public class SynchThread extends Thread {

	private String reqId;
	private DeterminantCalculatorService servicePort;
	private SynchFrame view;

	/**
	 * Creates the thread.
	 *
	 * @param reqId the request of interest
	 * @param servicePort the servicePort to access the web service
	 * @param view the frame to show the result
	 */
	public SynchThread(String reqId, DeterminantCalculatorService servicePort, SynchFrame view) {
		this.reqId = reqId;
		this.servicePort = servicePort;
		this.view = view;
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		double res = servicePort.getResult(reqId);
		int elapsedTimeSecs = (int) ((System.currentTimeMillis() - startTime) / (double) 1000);
		view.updateReqData(res, elapsedTimeSecs);
	}
}
