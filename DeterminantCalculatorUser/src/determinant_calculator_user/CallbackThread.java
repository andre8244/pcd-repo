package determinant_calculator_user;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
// IMPORT DEL WEB SERVICE CLIENT:
import localhost_client.*;
//import marco_client.*;
//import marcoXP_client.*;
//import andreaf_client.*;
//import andreafWindows8dualCore_client.*;
//import leardini_linux.*;
//import leardini_mac.*;
//import leardini_linux_192_168_0_7.*;
//import linux_ethernet.*;

public class CallbackThread extends Thread {

	private String reqId;
	private DeterminantCalculatorService servicePort;
	private AsynchFrame view;
	private long startTime;
	private String me = "callbackThread";

	public CallbackThread(String reqId, DeterminantCalculatorService servicePort, AsynchFrame view) {
		this.reqId = reqId;
		this.servicePort = servicePort;
		this.view = view;
	}

	@Override
	public void run() {
		// definizione dell'handler
		AsyncHandler<GetResultResponse> asyncHandler = new AsyncHandler<GetResultResponse>() {
			@Override
			public void handleResponse(Response<GetResultResponse> response) {
				try {
					int percentage = servicePort.getPercentageDone(reqId);
					int elapsedTimeSecs = (int) ((System.currentTimeMillis() - startTime) / 1000);
					view.updateReqData(percentage, elapsedTimeSecs, -1);

					if (!("" + response.get().getReturn()).equals("-0.0")) {
						view.updateLabelResult("Result: " + response.get().getReturn());
					} else {
						view.updateLabelResult("Result: ERROR");
					}
				} catch (InterruptedException | ExecutionException ex) {
					ex.printStackTrace();
				}
			}
		};
		Future<?> response = servicePort.getResultAsync(reqId, asyncHandler);
		startTime = System.currentTimeMillis();
		int lastPercentage = 0;
		int eta = -1;

		while (!response.isDone()) {
			int percentage = servicePort.getPercentageDone(reqId);
			int elapsedTimeSecs = (int) ((System.currentTimeMillis() - startTime) / 1000);

			if (percentage!=lastPercentage){
				lastPercentage = percentage;
				eta = (int) ((System.currentTimeMillis() - startTime) / (double)(10 * percentage) - elapsedTimeSecs);
			}
			if (!response.isDone()){
				view.updateReqData(percentage, elapsedTimeSecs, eta);
			}

			try {
				Thread.sleep(10000); // TODO 1000
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}
}