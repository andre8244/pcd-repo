package user;

import log.l;

public class UserApp {

	private String me = "userApp";
        private determinant_ws_client.DeterminantCalculatorService servicePort;

	public UserApp(){

        determinant_ws_client.DeterminantCalculatorService_Service service =
				new determinant_ws_client.DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();

        String path = System.getProperty("user.home") + System.getProperty("file.separator");
        String fileName = path + "matrix.txt";
        int order = 100;
		MatrixUtil.genAndWriteToFile(order, 20, fileName);
                        
        String reqId1 = servicePort.computeDeterminant(order, null);
		int percentage1 = servicePort.getPercentageDone(reqId1);
		l.l(me, reqId1 + " percentage: " + percentage1 + " %");
		int lastPercentage1 = percentage1;
        
        /*String reqId2 = servicePort.computeDeterminant(order, null);
		int percentage2 = servicePort.getPercentageDone(reqId2);
		l.l(me, reqId2 + " percentage: " + percentage2 + " %");
		int lastPercentage2 = percentage2;*/

		while (percentage1 != 100 /*|| percentage2 != 100*/) {
			if (percentage1 != lastPercentage1){
				l.l(me, reqId1 + " percentage: " + percentage1 + " %");
				lastPercentage1 = percentage1;
			}
            /*if (percentage2 != lastPercentage2){
				l.l(me, reqId2 + " percentage: " + percentage2 + " %");
				lastPercentage2 = percentage2;
			}*/

			try {
				Thread.sleep(50);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			percentage1 = servicePort.getPercentageDone(reqId1);
            //percentage2 = servicePort.getPercentageDone(reqId2);
		}

		System.out.println("Result for " + reqId1 + ": " + servicePort.getResult(reqId1));
        //System.out.println("Result for " + reqId2 + ": " + servicePort.getResult(reqId2));
            
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
