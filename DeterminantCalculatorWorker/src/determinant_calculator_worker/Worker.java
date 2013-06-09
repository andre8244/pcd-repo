package determinant_calculator_worker;

import messages.Messages;
import log.l;
import akka.actor.Address;
import akka.actor.UntypedActor;
import akka.remote.RemoteActorRefProvider;
import java.util.ArrayList;

public class Worker extends UntypedActor {
	String me;

	// Alessi
	//private determinantcalculatorservice.DeterminantCalculatorService servicePort;

	// Leardini
	private leardini_ws_client.DeterminantCalculatorService servicePort;

	// Fortibuoni
	//private fortibuoni_WS_client.DeterminantCalculatorService servicePort;

	@Override
	public void preStart() {
		super.preStart();
		me = getSelf().path().name();

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

		Address systemRemoteAddress = ((RemoteActorRefProvider) context().provider()).transport().address();
		String remoteAddress = getSelf().path().toStringWithAddress(systemRemoteAddress);
		boolean result = servicePort.registerWorker(remoteAddress);

		if (result){
			l.l(me, "worker registered");
		}
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof Messages.Job) {
			Messages.Job job = (Messages.Job) msg;
			handleJob(job);
		} else {
			unhandled(msg);
		}
	}

	private void handleJob(Messages.Job job) {
		final ArrayList<Double> list = job.getList();
		final String reqId = job.getReqId();
		double result = 0;

		// per ora come job calcolo la media dei valori della lista
		for (int i = 0; i < list.size(); i++) {
			result = result + list.get(i);
		}
		result = result / list.size();
		final Messages.JobResult jr = new Messages.JobResult(result, reqId);
		getSender().tell(jr, getSelf());
	}

}
