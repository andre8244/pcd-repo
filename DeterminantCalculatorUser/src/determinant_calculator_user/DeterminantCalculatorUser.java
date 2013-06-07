/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package determinant_calculator_user;

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
	// Alessi
	//determinantcalculatorservice.DeterminantCalculatorService servicePort;

	// Leardini
	leardini_web_service_client.DeterminantCalculatorService servicePort;

	// Fortibuoni
	// ...
	private DeterminantCalculatorUser() {

		// Leardini
		leardini_web_service_client.DeterminantCalculatorService_Service service =
				new leardini_web_service_client.DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();

		// Alessi
		// determinantcalculatorservice.DeterminantCalculatorService_Service service =
		//		new determinantcalculatorservice.DeterminantCalculatorService_Service();
		// servicePort = service.getDeterminantCalculatorServicePort();

		// Fortibuoni
		// ..

		int nWorkers = 5;

		for (int i = 0; i < nWorkers; i++) {
			ActorSystem system = ActorSystem.create("worker" + i, ConfigFactory.load().getConfig("worker" + i));
			final ActorRef worker = system.actorOf(new Props(UntypedWorker.class), "worker" + i);
			// TODO usare il path del worker al posto di passargli nome, ip e porta
			servicePort.registerWorker("worker" + i, "127.0.0.1", (2553 + i));
		}

		servicePort.computeDeterminant(5000, null);
		System.out.println("RESULT: " + servicePort.getResult("req0"));
	}

	public static void main(String[] args) {
		new DeterminantCalculatorUser();
	}
}
