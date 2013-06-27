package user;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
// IMPORT DEL WEB SERVICE CLIENT:
import localhost_client.*;
//import windowsXP_client.*;

public class CallbackThread extends Thread{

	private String reqId;
	private DeterminantCalculatorService servicePort;
	private AsynchFrame view;
	private long startTime;

	public CallbackThread(String reqId, DeterminantCalculatorService servicePort, AsynchFrame view) {
		this.reqId = reqId;
		this.servicePort = servicePort;
		this.view = view;
	}

	@Override
	public void run(){
		// definizione dell'handler
		AsyncHandler<GetResultResponse> asyncHandler = new AsyncHandler<GetResultResponse>() {
			@Override
			public void handleResponse(Response<GetResultResponse> response) {
				try {
					// process of asynchronous response goes here
					int percentage = servicePort.getPercentageDone(reqId);
					view.updatingData(percentage,"Elapsed: " + (int)((System.currentTimeMillis()-startTime)/(double)1000) + " sec","ETA: --");
					if (!(""+response.get().getReturn()).equals("-0.0")){
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
		int lastPercentage = 0;
		startTime = System.currentTimeMillis();

		while (!response.isDone()) {
			//l.l(me, "dummy print... i could do something more useful while waiting (callback)");
//			l.l(me, "getting percentage...");
			int percentage = servicePort.getPercentageDone(reqId);
			if (percentage != lastPercentage){
				view.updatingData(percentage,"Elapsed: " + (int)((System.currentTimeMillis()-startTime)/(double)1000) + " sec","ETA: "+ (int)((System.currentTimeMillis()-startTime)/(double)(10*percentage))+" sec");
				lastPercentage = percentage;
			} else {
				view.updatingData(percentage,"Elapsed: " + (int)((System.currentTimeMillis()-startTime)/(double)1000) + " sec","");
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

}