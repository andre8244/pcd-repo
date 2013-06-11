package user;

import log.l;

public class UserApp {

	private String me = "userApp";
        private determinant_ws_client.DeterminantCalculatorService servicePort;

	public UserApp(){
		
                determinant_ws_client.DeterminantCalculatorService_Service service =
				new determinant_ws_client.DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();

                String reqId = servicePort.computeDeterminant(10000, null);
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
                //String fileName = path + "matrix.txt";
		//MatrixUtil.genAndWriteToFile(10000, 20, fileName);
      
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
