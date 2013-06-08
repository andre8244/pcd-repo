package deploy_worker;

import Log.L;
import akka.actor.Address;
import akka.remote.RemoteActorRefProvider;
import untyped.UntypedWorker;

public class Worker extends UntypedWorker {
	String me;

	// Alessi
	//determinantcalculatorservice.DeterminantCalculatorService servicePort;

	// Leardini
	leardini_web_service_client.DeterminantCalculatorService servicePort;

	// Fortibuoni
	//fortibuoni_WS_client.DeterminantCalculatorService servicePort;

	@Override
	public void preStart() {
		super.preStart();
		me = getSelf().path().name();

		// Leardini
		leardini_web_service_client.DeterminantCalculatorService_Service service =
				new leardini_web_service_client.DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();

		// Alessi
		/*determinantcalculatorservice.DeterminantCalculatorService_Service service =
				new determinantcalculatorservice.DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();*/

		// Fortibuoni
		/*fortibuoni_WS_client.DeterminantCalculatorService_Service service =
				new fortibuoni_WS_client.DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();*/

		Address systemRemoteAddress = ((RemoteActorRefProvider) context().provider()).transport().address();
		String actorRemoteAddress = getSelf().path().toStringWithAddress(systemRemoteAddress);
	}



	@Override
	public void onReceive(Object msg) throws Exception {

	}

}
