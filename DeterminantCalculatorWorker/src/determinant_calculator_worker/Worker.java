package determinant_calculator_worker;

import messages.Messages;
import log.l;
import akka.actor.Address;
import akka.actor.UntypedActor;
import akka.remote.RemoteActorRefProvider;
// IMPORT DEL WEB SERVICE CLIENT:
import localhost_client.*;
//import marco_client.*;
//import marcoXP_client.*;
//import andreaf_client.*;
//import andreafWindows8dualCore_client.*;
//import leardini_linux.*;
//import leardini_mac.*;

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
		//l.l(me, "computing rows...");
		String reqId = manyRows.getReqId();
		double[] firstRow = manyRows.getFirstRow();
		double[][] rows = manyRows.getRows();
		int rowNumber = manyRows.getRowNumber();
        double factor;

        for (int i= 0; i < rows.length; i++){
            factor = -rows[i][0] / firstRow[0];

            for (int j = 0; j < firstRow.length; j++) {
                rows[i][j] = rows[i][j] + factor * firstRow[j];
            }
        }
		getSender().tell(new Messages.RowsResult(reqId, rows, rowNumber), getSelf());
        //l.l(me, reqId + ", sent rows from " + rowNumber + " to " + (rowNumber+rows.length-1) + " to master");
	}

	private void handleRemove() {
		servicePort.removeWorker(remoteAddress);
		//l.l(me, "worker removed");
		this.getContext().stop(this.getSelf());
	}

	private void handleAddWorkerAck() {
		l.l(me, "worker registered to master: " + this.getSender().path().toString());
	}

}
