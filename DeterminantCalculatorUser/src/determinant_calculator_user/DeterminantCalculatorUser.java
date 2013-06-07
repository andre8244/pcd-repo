/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package determinant_calculator_user;

import Log.L;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import untyped.UntypedWorker;

/**
 *
 * @author Marco
 */
public class DeterminantCalculatorUser {

	private String name = "user";

	// Alessi
	//determinantcalculatorservice.DeterminantCalculatorService servicePort;

	// Leardini
	leardini_web_service_client.DeterminantCalculatorService servicePort;

	// Fortibuoni
	//fortibuoni_WS_client.DeterminantCalculatorService servicePort;

	private DeterminantCalculatorUser() {

		// Leardini
		leardini_web_service_client.DeterminantCalculatorService_Service service =
				new leardini_web_service_client.DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();

		// Alessi
		/*determinantcalculatorservice.DeterminantCalculatorService_Service service =
				new determinantcalculatorservice.DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();

		// Fortibuoni
		/*fortibuoni_WS_client.DeterminantCalculatorService_Service service =
				new fortibuoni_WS_client.DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();*/

		int nWorkers = 5;

		for (int i = 0; i < nWorkers; i++) {
			ActorSystem system = ActorSystem
					.create("worker" + i, ConfigFactory.load().getConfig("worker" + i));
			final ActorRef worker = system.actorOf(new Props(UntypedWorker.class), "worker" + i);
			// TODO usare il path del worker al posto di passargli nome, ip e porta
			servicePort.registerWorker("worker" + i, "127.0.0.1", (2553 + i));
		}

		String reqId = servicePort.computeDeterminant(300000, null);
		int percentage = servicePort.getPercentageDone(reqId);

		while (percentage != 100) {
			L.log(name, reqId + " percentage: " + percentage + " %");

			try {
				Thread.sleep(20);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			percentage = servicePort.getPercentageDone(reqId);
		}

		System.out.println("RESULT for " + reqId + ": " + servicePort.getResult(reqId));

		try {
			Thread.sleep(3000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		System.exit(0);
	}

	public static void main(String[] args) {
		new DeterminantCalculatorUser();
	}
}
