package determinant_calculator_worker;

import messages.Messages;
import log.l;
import akka.actor.Address;
import akka.actor.UntypedActor;
import akka.remote.RemoteActorRefProvider;

public class Worker extends UntypedActor {

	private String me;
	private determinant_ws_client.DeterminantCalculatorService servicePort;

	@Override
	public void preStart() {
		super.preStart();
		me = getSelf().path().name();

		determinant_ws_client.DeterminantCalculatorService_Service service =
				new determinant_ws_client.DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();

		Address systemRemoteAddress = ((RemoteActorRefProvider) context().provider()).transport().address();
		String remoteAddress = getSelf().path().toStringWithAddress(systemRemoteAddress);
		boolean result = servicePort.registerWorker(remoteAddress);

		if (result) {
			l.l(me, "worker registered");
		}
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof Messages.OneRow) {
			Messages.OneRow oneRow = (Messages.OneRow) msg;
			handleOneRow(oneRow);
		} else {
			unhandled(msg);
		}
	}

	private void handleOneRow(Messages.OneRow oneRow) {
		final String reqId = oneRow.getReqId();
		final double[] firstRow = oneRow.getFirstRow();
		final double[] row = oneRow.getRow();
		final int rowNumber = oneRow.getRowNumber();

		double factor = -row[0] / firstRow[0];
		l.l(me, "factor: " + factor);

		for (int i = 0; i < firstRow.length; i++) {
			row[i] = row[i] + factor * firstRow[i];
		}

		final Messages.OneRowResult oneRowResult = new Messages.OneRowResult(reqId, row, rowNumber);
		getSender().tell(oneRowResult, getSelf());
	}
}
