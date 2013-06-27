package user;

// IMPORT DEL WEB SERVICE CLIENT:
import localhost_client.*;

public class SynchThread extends Thread{

	private String reqId;
	private DeterminantCalculatorService servicePort;
	private SynchFrame view;
	
	public SynchThread(String reqId, DeterminantCalculatorService servicePort, SynchFrame view) {
		this.reqId = reqId;
		this.servicePort = servicePort;
		this.view = view;
	}
	
	@Override
	public void run(){
		long startTime = System.currentTimeMillis();
		double res = servicePort.getResult(reqId);
		if (!(""+res).equals("-0.0")){
			view.updateData("Result: " + res,"Elapsed: " + (int)((System.currentTimeMillis()-startTime)/(double)1000) + " sec");
		} else {
			view.updateData("Result: ERROR","Elapsed: " + (int)((System.currentTimeMillis()-startTime)/(double)1000) + " sec");
		}
	}		
	
}
