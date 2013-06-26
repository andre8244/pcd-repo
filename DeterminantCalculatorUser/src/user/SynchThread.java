package user;

import localhost_client.DeterminantCalculatorService;

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
		view.updateLabel("Result: " + servicePort.getResult(reqId));
	}
	
}
