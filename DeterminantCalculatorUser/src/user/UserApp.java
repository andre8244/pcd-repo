package user;

import Log.L;

public class UserApp {

	private String me = "userApp";

	// Alessi
	//private determinantcalculatorservice.DeterminantCalculatorService servicePort;

	// Leardini
	private leardini_ws_client.DeterminantCalculatorService servicePort;

	// Fortibuoni
	//private fortibuoni_WS_client.DeterminantCalculatorService servicePort;

	public UserApp(){
		// Leardini
		leardini_ws_client.DeterminantCalculatorService_Service service =
				new leardini_ws_client.DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();

		// Alessi
		/*determinantcalculatorservice.DeterminantCalculatorService_Service service =
				new determinantcalculatorservice.DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();*/

		// Fortibuoni
		/*fortibuoni_WS_client.DeterminantCalculatorService_Service service =
				new fortibuoni_WS_client.DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();*/

		String reqId = servicePort.computeDeterminant(20000, null);
		int percentage = servicePort.getPercentageDone(reqId);
		L.log(me, reqId + " percentage: " + percentage + " %");
		int lastPercentage = percentage;

		while (percentage != 100) {
			if (percentage != lastPercentage){
				L.log(me, reqId + " percentage: " + percentage + " %");
				lastPercentage = percentage;
			}

			try {
				Thread.sleep(50);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			percentage = servicePort.getPercentageDone(reqId);
		}

		System.out.println("Result for " + reqId + ": " + servicePort.getResult(reqId));

		try {
			Thread.sleep(3000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		System.exit(0);
	}

	public static void main(String[] args) {
		new UserApp();
	}
}
