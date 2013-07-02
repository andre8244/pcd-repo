package determinant_calculator_worker;

import messages.Messages;
import log.l;
import akka.actor.Address;
import akka.actor.UntypedActor;
import akka.remote.RemoteActorRefProvider;
// IMPORT DEL WEB SERVICE CLIENT:
//import localhost_client.*;
import marco_client.*;
//import marcoXP_client.*;
//import andreaf_client.*;
//import andreafWindows8dualCore_client.*;
//import leardini_linux.*;

public class Worker extends UntypedActor {

	private String me;
	private DeterminantCalculatorService servicePort;
	private String remoteAddress;

	@Override
	public void preStart() {
		super.preStart();
		me = getSelf().path().name();

		DeterminantCalculatorService_Service service =
				new DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();

		Address systemRemoteAddress = ((RemoteActorRefProvider) context().provider()).transport().address();
		remoteAddress = getSelf().path().toStringWithAddress(systemRemoteAddress);
		servicePort.addWorker(remoteAddress);
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof Messages.OneRow) { // TODO eliminare OneRow
			Messages.OneRow oneRow = (Messages.OneRow) msg;
			handleOneRow(oneRow);
		} else if (msg instanceof Messages.ManyRows) {
			Messages.ManyRows manyRows = (Messages.ManyRows) msg;
			handleManyRows(manyRows);
        } else if (msg instanceof Messages.Remove) { // TODO da eliminare?
			handleRemove();
		} else if (msg instanceof Messages.AddWorkerAck) {
			handleAddWorkerNodeAck();
		} else if (msg instanceof Messages.RemoveWorkerAck) {
			handleRemoveWorkerNodeAck();
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

		for (int i = 0; i < firstRow.length; i++) {
			row[i] = row[i] + factor * firstRow[i];
		}
		final Messages.OneRowResult oneRowResult = new Messages.OneRowResult(reqId, row, rowNumber);
		getSender().tell(oneRowResult, getSelf());
		//l.l(me, reqId + ", sent row " + rowNumber + " to master");
	}

	private void handleManyRows(Messages.ManyRows manyRows) {
		final String reqId = manyRows.getReqId();
		final double[] firstRow = manyRows.getFirstRow();
		final double[][] rows = manyRows.getRows();
		final int rowNumber = manyRows.getRowNumber();
        double factor;

        for (int i= 0; i < rows.length; i++){
            factor = -rows[i][0] / firstRow[0];

            for (int j = 0; j < firstRow.length; j++) {
                rows[i][j] = rows[i][j] + factor * firstRow[j];
            }
        }
		final Messages.ManyRowsResult manyRowsResult = new Messages.ManyRowsResult(reqId, rows, rowNumber);
		getSender().tell(manyRowsResult, getSelf());
        //l.l(me, reqId + ", sent rows from " + rowNumber + " to " + (rowNumber+rows.length-1) + " to master");
	}

	private void handleRemove() {
		servicePort.removeWorker(remoteAddress);
	}

	private void handleAddWorkerNodeAck() {
		l.l(me, "worker registered to master: " + this.getSender().toString());
	}

	private void handleRemoveWorkerNodeAck() {
		l.l(me, "worker removed");
		this.getContext().stop(this.getSelf());
	}
}
