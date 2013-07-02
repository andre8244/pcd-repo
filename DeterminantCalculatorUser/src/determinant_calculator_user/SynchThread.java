package determinant_calculator_user;

// IMPORT DEL WEB SERVICE CLIENT:
import localhost_client.*;
//import marco_client.*;
//import marcoXP_client.*;
//import andreaf_client.*;
//import andreafWindows8dualCore_client.*;
//import leardini_linux.*;
//import leardini_mac.*;

public class SynchThread extends Thread {

	private String reqId;
	private DeterminantCalculatorService servicePort;
	private SynchFrame view;

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
