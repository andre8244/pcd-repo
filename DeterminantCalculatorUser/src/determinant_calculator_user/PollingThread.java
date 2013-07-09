package determinant_calculator_user;

import java.util.concurrent.ExecutionException;
import javax.xml.ws.Response;
// IMPORT DEL WEB SERVICE CLIENT:
//import localhost_client.*;
//import marco_client.*;
//import marcoXP_client.*;
//import andreaf_client.*;
//import andreafWindows8dualCore_client.*;
//import leardini_linux.*;
//import leardini_mac.*;
//import leardini_linux_192_168_0_7.*;
import linux_ethernet.*;

public class PollingThread extends Thread{
	private String reqId;
	private DeterminantCalculatorService servicePort;
	private AsynchFrame view;

	public PollingThread(String reqId, DeterminantCalculatorService servicePort, AsynchFrame view) {
		this.reqId = reqId;
		this.servicePort = servicePort;
		this.view = view;
	}

	@Override
	public void run(){
		Response<GetResultResponse> response = servicePort.getResultAsync(reqId);
		long startTime = System.currentTimeMillis();
		int lastPercentage = 0;
		int eta = -1;

		while (!response.isDone()) {
			int percentage = servicePort.getPercentageDone(reqId);
			int elapsedTimeSecs = (int) ((System.currentTimeMillis() - startTime) / 1000);

			if (percentage!=lastPercentage){
				lastPercentage = percentage;
				eta = (int) ((System.currentTimeMillis() - startTime) / (double)(10 * percentage) - elapsedTimeSecs);
			}
			view.updateReqData(percentage, elapsedTimeSecs, eta);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}

		// Response is done
		int percentage = servicePort.getPercentageDone(reqId);
		int elapsedTime = (int) ((System.currentTimeMillis() - startTime) / 1000);
		view.updateReqData(percentage, elapsedTime, -1);

		try {
			if (!(""+response.get().getReturn()).equals("-0.0")){
				view.updateLabelResult("Result : " + response.get().getReturn());
			} else {
				view.updateLabelResult("Result: ERROR");
			}
		} catch (InterruptedException | ExecutionException ex) {
			ex.printStackTrace();
		}
	}

}