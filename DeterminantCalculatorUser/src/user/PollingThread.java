package user;

import java.util.concurrent.ExecutionException;
import javax.xml.ws.Response;
import localhost_client.DeterminantCalculatorService;
import localhost_client.GetResultResponse;

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
				view.updatingData(percentage,"Time elapsed: " + (double)((System.currentTimeMillis()-startTime)/1000) + "sec","Duration estimated: "+ (double)((System.currentTimeMillis()-startTime)/(10*percentage))+" sec");
				lastPercentage = percentage;
			} else {
				view.updatingData(percentage,"Time elapsed: " + (double)((System.currentTimeMillis()-startTime)/1000) + "sec","");
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		int percentage = servicePort.getPercentageDone(reqId);
		view.updatingData(percentage,"Time elapsed: " + (double)((System.currentTimeMillis()-startTime)/1000) + "sec","Duration estimated: "+ (double)((System.currentTimeMillis()-startTime)/(10*percentage))+" sec");
		try {
			view.updateLabelResult("Result : " + response.get().getReturn());
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} catch (ExecutionException ex) {
			ex.printStackTrace();
		}		
	}
	
}