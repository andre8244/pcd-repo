package determinant_calculator_worker;

import messages.Messages;
import akka.actor.Address;
import akka.actor.UntypedActor;
import akka.remote.RemoteActorRefProvider;

// IMPORT DEL WEB SERVICE CLIENT:
import localhost_client.*;
//import marco_client.*;
//import andreaf_client.*;
//import leardini_mac.*;

/**
 * An actor that processes blocks of rows sent from the masters.
 *
 */
public class Worker extends UntypedActor {

	private String me;
	private DeterminantCalculatorService servicePort;
	private String remoteAddress;

	@Override
	public void preStart() {
		super.preStart();
		me = getSelf().path().name();

		DeterminantCalculatorService_Service service = new DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();

		Address systemRemoteAddress = ((RemoteActorRefProvider) context().provider()).transport().address();
		remoteAddress = getSelf().path().toStringWithAddress(systemRemoteAddress);
		servicePort.addWorker(remoteAddress);
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof Messages.Rows) {
			Messages.Rows rows = (Messages.Rows) msg;
			handleRows(rows);
		} else if (msg instanceof Messages.Remove) {
			handleRemove();
		} else if (msg instanceof Messages.AddWorkerAck) {
			handleAddWorkerAck();
		} else {
			unhandled(msg);
		}
	}

	private void handleRows(Messages.Rows manyRows) {
		String reqId = manyRows.getReqId();
		double[] firstRow = manyRows.getFirstRow();
		double[][] rows = manyRows.getRows();
		int rowNumber = manyRows.getRowNumber();
		double factor;

		for (int i = 0; i < rows.length; i++) {
			factor = -rows[i][0] / firstRow[0];

			for (int j = 0; j < firstRow.length; j++) {
				rows[i][j] = rows[i][j] + factor * firstRow[j];
			}
		}
		getSender().tell(new Messages.RowsResult(reqId, rows, rowNumber), getSelf());
		// System.out.println("["+me+"] "+ reqId + ", sent rows from " + rowNumber + " to " + (rowNumber+rows.length-1)
		// + " to master");
	}

	private void handleRemove() {
		servicePort.removeWorker(remoteAddress);
		System.out.println("[" + me + "] worker removed");
		this.getContext().stop(this.getSelf());
	}

	private void handleAddWorkerAck() {
		System.out.println("[" + me + "] worker registered to master: " + this.getSender().path().toString());
	}

}
