package user;

import log.l;

public class UserApp {

	private String me = "userApp";

	// Alessi
	//private determinantcalculatorservice.DeterminantCalculatorService servicePort;

	// Leardini
	private determinant_ws_client.DeterminantCalculatorService servicePort;

	// Fortibuoni
	//private fortibuoni_WS_client.DeterminantCalculatorService servicePort;

	public UserApp(){
		// Leardini
		determinant_ws_client.DeterminantCalculatorService_Service service =
				new determinant_ws_client.DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();

		// Alessi
		/*determinantcalculatorservice.DeterminantCalculatorService_Service service =
				new determinantcalculatorservice.DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();*/

		// Fortibuoni
		//fortibuoni_WS_client.DeterminantCalculatorService_Service service =
		//		new fortibuoni_WS_client.DeterminantCalculatorService_Service();
		//servicePort = service.getDeterminantCalculatorServicePort();

		String reqId = servicePort.computeDeterminant(1000, null);
		int percentage = servicePort.getPercentageDone(reqId);
		l.l(me, reqId + " percentage: " + percentage + " %");
		int lastPercentage = percentage;

		while (percentage != 100) {
			if (percentage != lastPercentage){
				l.l(me, reqId + " percentage: " + percentage + " %");
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


        //String path = System.getProperty("user.home") + System.getProperty("file.separator");
	//	String fileName = path + "matrix.txt";
		//MatrixUtil.genAndWriteToFile(10000, 20, fileName);
      //  MatrixUtil.fromFileToList(10000, path + "matrix10000.txt");
		//MatrixUtil.fromFileToArrayList(10000, path + "matrix10000.txt");
		//MatrixUtil.fromFileToHashMap(fileName);

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
