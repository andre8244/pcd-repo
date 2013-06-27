package user;

import java.util.concurrent.ExecutionException;
import javax.xml.ws.Response;
// IMPORT DEL WEB SERVICE CLIENT:
import localhost_client.*;
import log.l;

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
		int lastPercentage = 0;
		long startTime = System.currentTimeMillis();

		while (!response.isDone()) {
			int percentage = servicePort.getPercentageDone(reqId);
			if (percentage!=lastPercentage){
				view.updatingData(percentage,"Elapsed: " + (int)((System.currentTimeMillis()-startTime)/(double)1000) + " sec","ETA: "+ (int)((System.currentTimeMillis()-startTime)/(double)(10*percentage))+" sec");
				lastPercentage = percentage;
			} else {
				view.updatingData(percentage,"Elapsed: " + (int)((System.currentTimeMillis()-startTime)/(double) 1000) + " sec","");
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		int percentage = servicePort.getPercentageDone(reqId);
		view.updatingData(percentage,"Elapsed: " + (int)((System.currentTimeMillis()-startTime)/ (double) 1000) + " sec","ETA: --");
		try {
			l.l("", ""+response.get().getReturn());
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