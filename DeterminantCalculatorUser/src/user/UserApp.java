package user;

import determinant_ws_client.GetResultResponse;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import log.l;

public class UserApp {

	private static final String me = "userApp";
	private determinant_ws_client.DeterminantCalculatorService servicePort;
	private static final int SYNCHRONOUS = 0;
	private static final int POLLING = 1;
	private static final int CALLBACK = 2;
	private String path = System.getProperty("user.home") + System.getProperty("file.separator");
	private String fileValues;
	private URL fileValuesURL;
	private int order = 2000;
	// select execution policy:
	private static final int policy = POLLING;

	public UserApp() {

		determinant_ws_client.DeterminantCalculatorService_Service service =
				new determinant_ws_client.DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();

		fileValues = path + "matrix.txt";
		//fileValues = path + "matrix" + order + ".txt";
		//fileValues = path + "matrix2000@1.15e-44.txt";
		//fileValues = "http://pcddeterminant.altervista.org/matrix300@6.03e60.txt";

		MatrixUtil.genAndWriteToFile(order, 0.1, 0.2, fileValues); // 4000

		l.l(me, "waiting for web service response...");

		switch (policy) {
			case SYNCHRONOUS:
				String reqId = servicePort.computeDeterminant(order, fileValues);
				l.l(me, "Result for " + reqId + ": " + servicePort.getResult(reqId));
				break;
			case POLLING:
				pollingRequest();
				break;
			case CALLBACK:
				callbackRequest();
				break;
		}
	}

	private void pollingRequest() {
		//String reqId = servicePort.computeDeterminant(order, fileValuesURL.toString());
		String reqId = servicePort.computeDeterminant(order, fileValues);
		Response<GetResultResponse> response = servicePort.getResultAsync(reqId);
		int lastPercentage = 0;
		long startTime = System.currentTimeMillis();

		while (!response.isDone()) {
			//l.l(me, "dummy print... i could do something more useful while waiting (polling)");
//			l.l(me, "getting percentage...");
			int percentage = servicePort.getPercentageDone(reqId);
			if (percentage!=lastPercentage){
				l.l(me, reqId + " percentage: " + percentage + " % (polling). Time elapsed: " + (double)((System.currentTimeMillis()-startTime)/1000) + "sec , duration stimated: "+ (double)((System.currentTimeMillis()-startTime)/(10*percentage))+" sec");
				lastPercentage = percentage;
			} else {
				l.l(me, reqId + " percentage: " + percentage + " % (polling)");
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}

		try {
			l.l(me, "Result for " + reqId + ": " + response.get().getReturn()+ ". Time elapsed: " + (double)((System.currentTimeMillis()-startTime)/1000)+" sec.");
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} catch (ExecutionException ex) {
			ex.printStackTrace();
		}

		// vecchia implementazione
//		String reqId1 = servicePort.computeDeterminant(order, fileValues);
//		int percentage1 = servicePort.getPercentageDone(reqId1);
//		l.l(me, reqId1 + " percentage: " + percentage1 + " %");
//		int lastPercentage1 = percentage1;
//
//		/*String reqId2 = servicePort.computeDeterminant(order, null);
//		 int percentage2 = servicePort.getPercentageDone(reqId2);
//		 l.l(me, reqId2 + " percentage: " + percentage2 + " %");
//		 int lastPercentage2 = percentage2;*/
//
//		while (percentage1 != 100 /*|| percentage2 != 100*/) {
//			if (percentage1 != lastPercentage1) {
//				l.l(me, reqId1 + " percentage: " + percentage1 + " %");
//				lastPercentage1 = percentage1;
//			}
//			/*if (percentage2 != lastPercentage2){
//			 l.l(me, reqId2 + " percentage: " + percentage2 + " %");
//			 lastPercentage2 = percentage2;
//			 }*/
//
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException ex) {
//				ex.printStackTrace();
//			}
//			percentage1 = servicePort.getPercentageDone(reqId1);
//			//percentage2 = servicePort.getPercentageDone(reqId2);
//		}
//		l.l(me, reqId1 + " percentage: " + percentage1 + " %");
//		//l.l(me, reqId2 + " percentage: " + percentage2 + " %");
//
//		l.l(me, "Result for " + reqId1 + ": " + servicePort.getResult(reqId1));
////		l.l(me, "Result for " + reqId2 + ": " + servicePort.getResult(reqId2));
	}

	private void callbackRequest() {
		final String reqId = servicePort.computeDeterminant(order, fileValues);
		// definizione dell'handler
		AsyncHandler<GetResultResponse> asyncHandler = new AsyncHandler<GetResultResponse>() {
			@Override
			public void handleResponse(Response<GetResultResponse> response) {
				try {
					// process of asynchronous response goes here
					l.l(me, "Result for " + reqId + ": " + response.get().getReturn());
				} catch (InterruptedException | ExecutionException ex) {
					ex.printStackTrace();
				}
			}
		};
		Future<?> response = servicePort.getResultAsync(reqId, asyncHandler);

		while (!response.isDone()) {
			//l.l(me, "dummy print... i could do something more useful while waiting (callback)");
//			l.l(me, "getting percentage...");
			int percentage = servicePort.getPercentageDone(reqId);
			l.l(me, reqId + " percentage: " + percentage + " % (callback)");

			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new UserApp();
	}
}
