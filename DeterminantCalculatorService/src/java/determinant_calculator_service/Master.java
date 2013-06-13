package determinant_calculator_service;

import log.l;
import java.util.ArrayList;

import akka.actor.UntypedActor;
import akka.remote.RemoteClientShutdown;
import akka.remote.RemoteLifeCycleEvent;
import java.util.HashMap;
import messages.Messages;

public class Master extends UntypedActor {

	private HashMap<String, MatrixInfo> matricesInfo;
	private ArrayList<RemoteWorker> workers;
	private DeterminantCalculatorManager manager;
	private String me;

	public Master() {
		// TODO è meglio che i workers stiano dentro al manager?
		workers = new ArrayList<RemoteWorker>();
		me = getSelf().path().name();
		matricesInfo = new HashMap<String, MatrixInfo>();
	}

	@Override
	public void preStart() {
		super.preStart();
		context().system().eventStream().subscribe(getSelf(), RemoteLifeCycleEvent.class);
	}
	
	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof Messages.Compute) {
			Messages.Compute c = (Messages.Compute) msg;
			handleCompute(c);
		} else if (msg instanceof Messages.OneRowResult) {
			Messages.OneRowResult orr = (Messages.OneRowResult) msg;
			handleOneRowResult(orr);
		} else if (msg instanceof Messages.ManyRowsResult) {
			Messages.ManyRowsResult mrr = (Messages.ManyRowsResult) msg;
			handleManyRowsResult(mrr);
		} else if (msg instanceof Messages.RegisterWorker) {
			Messages.RegisterWorker rw = (Messages.RegisterWorker) msg;
			handleRegisterWorker(rw);
		} else if (msg instanceof Messages.RemoveWorker) {
			Messages.RemoveWorker rw = (Messages.RemoveWorker) msg;
			handleRemoveWorker(rw);
		} else if (msg instanceof RemoteClientShutdown) {
			l.l(me, "Remote Client Shutdown!");
			RemoteClientShutdown rcs = (RemoteClientShutdown) msg;
			l.l(me, "address: " + rcs.getRemoteAddress().toString());
			handleRemoveWorkerSystem(rcs.getRemoteAddress().toString());
		} else {
			unhandled(msg);
		}
	}

	private void handleCompute(Messages.Compute compute) {
		if (manager == null) {
			manager = compute.getManager();
		}

		int order = compute.getOrder();
		String fileValues = compute.getFileValues();
		String reqId = compute.getReqId();

		l.l(me, "handleCompute, reqId: " + reqId + ", order: " + order + ", workers.size():" + workers.size());

		matricesInfo.put(reqId, new MatrixInfo(MatrixUtil.fromFileToList(order, fileValues)));

		if (workers.isEmpty()) {
			l.l(me, "\nWORKERS.SIZE() = 0 !!!!\n");
			manager.setPercentageDone(reqId, 100); // TODO comunico al client di aver finito anche se non ho calcolato niente
			return;
		}

		gauss(reqId);
	}

	private void handleOneRowResult(Messages.OneRowResult orr) {
		String reqId = orr.getReqId();
		double[] row = orr.getRow();
		int rowNumber = orr.getRowNumber();

		MatrixInfo matrixInfo = matricesInfo.get(reqId);

		int nRowsDone = matrixInfo.getRowsDone();
		nRowsDone++;
		matrixInfo.setRowsDone(nRowsDone);

		/*if (nRowsDone % 500 == 0) {
            l.l(me, "nRowsDone: " + nRowsDone);
		 }*/

		double[][] matrix = matrixInfo.getMatrix();
		matrix[rowNumber] = row;
		matrixInfo.setMatrix(matrix);

		if (nRowsDone == matrix.length - 1) {
			if (matrix.length % 500 == 0){
                l.l(me, "Received all rows for " + reqId + ", submatrix " + matrix.length + ". Duration: " + ((System.currentTimeMillis() - matrixInfo.getStartTime()) / (double) 1000) + " sec");
			}
			boolean zeroColumn = false;

			if (matrix.length > 2) {
				matrixInfo.setRowsDone(0);
				matrixInfo.setMatrix(subMatrix(reqId, matrix));
				
				if (workers.isEmpty()) {
					l.l(me, "\nWORKERS.SIZE() = 0 !!!!\n");
					manager.setPercentageDone(reqId, 100);
					return;
				}
				
				zeroColumn = gauss(reqId);
			} else {
				l.l(me, "Received ALL rows for " + reqId + ", submatrix " + matrix.length + ". Duration: " + ((System.currentTimeMillis() - matrixInfo.getStartTime()) / (double) 1000) + " sec");
				double oldDeterminant = matrixInfo.getDeterminant();
				double determinant = oldDeterminant * matrix[1][1];

				if (!matrixInfo.getChangeSign()) {
					matrixInfo.setDeterminant(determinant);
					manager.setResult(reqId, determinant); // TODO
				} else {
					matrixInfo.setDeterminant(-determinant);
					manager.setResult(reqId, -determinant); // TODO
				}
			}
			if (!zeroColumn) {
				manager.setPercentageDone(reqId, (matrixInfo.getMatrixLength() - matrixInfo.getMatrix().length) * 100 / (matrixInfo.getMatrixLength() - 2));
			}
		}
	}

	private void handleManyRowsResult(Messages.ManyRowsResult mrr) {
		String reqId = mrr.getReqId();
		double[][] rows = mrr.getRows();
		int rowNumber = mrr.getRowNumber();

		MatrixInfo matrixInfo = matricesInfo.get(reqId);

		int nRowsDone = matrixInfo.getRowsDone();
		nRowsDone = nRowsDone + rows.length;
		matrixInfo.setRowsDone(nRowsDone);

		/*if (nRowsDone % 500 == 0) {
            l.l(me, "nRowsDone: " + nRowsDone);
		 }*/

		double[][] matrix = matrixInfo.getMatrix();
		System.arraycopy(rows, 0, matrix, rowNumber, rows.length);
		/*for (int i=0; i < rows.length; i++){
		 matrix[i+rowNumber] = rows[i];
		 }*/

		matrixInfo.setMatrix(matrix);

		if (nRowsDone == matrix.length - 1) {
			if (matrix.length % 500 == 0){
				l.l(me, "Received all rows for " + reqId + ", submatrix " + matrix.length + ". Duration: " + ((System.currentTimeMillis() - matrixInfo.getStartTime()) / (double) 1000) + " sec");
			}
			boolean zeroColumn = false;

			if (matrix.length > 2) {
				matrixInfo.setRowsDone(0);
				matrixInfo.setMatrix(subMatrix(reqId, matrix));
				
				if (workers.isEmpty()) {
					l.l(me, "\nWORKERS.SIZE() = 0 !!!!\n");
					manager.setPercentageDone(reqId, 100);
					return;
				}
								
				zeroColumn = gauss(reqId);
			} else {
				l.l(me, "Received ALL rows for " + reqId + ", submatrix " + matrix.length + ". Duration: " + ((System.currentTimeMillis() - matrixInfo.getStartTime()) / (double) 1000) + " sec");
				double oldDeterminant = matrixInfo.getDeterminant();
				double determinant = oldDeterminant * matrix[1][1];

				if (!matrixInfo.getChangeSign()) {
					matrixInfo.setDeterminant(determinant);
					manager.setResult(reqId, determinant); // TODO
				} else {
					matrixInfo.setDeterminant(-determinant);
					manager.setResult(reqId, -determinant); // TODO
				}
			}
			//l.l(me, "matrix " + matrix.length + ", percentage done " + reqId + ": " + (matrixInfo.getMatrixLength()-matrix.length)*100/(matrixInfo.getMatrixLength()-2));
			if (!zeroColumn) {
				manager.setPercentageDone(reqId, (matrixInfo.getMatrixLength() - matrixInfo.getMatrix().length) * 100 / (matrixInfo.getMatrixLength() - 2));
			}
		}
	}

	private void handleRegisterWorker(Messages.RegisterWorker rw) {
		String remoteAddress = rw.getRemoteAddress();
		RemoteWorker worker = new RemoteWorker(remoteAddress, getContext().actorFor(remoteAddress));
		workers.add(worker);
		l.l(me, worker.getName() + " added, workers size: " + workers.size());
	}

	private void handleRemoveWorker(Messages.RemoveWorker rw) {
		String remoteAddress = rw.getRemoteAddress();

		// TODO si potrebbe usare una hashmap per rendere la ricerca più performante
		for (int i = 0; i < workers.size(); i++) {
			if (workers.get(i).getRemoteAddress().equals(remoteAddress)) {
				l.l(me, workers.get(i).getName() + " removed, workers size: " + (workers.size()-1));
				workers.remove(i);
				i--;
			}
		}
	}

	private boolean swapFirtsRow(double[][] matrix) {
		for (int i = 1; i < matrix.length; i++) {
			if (matrix[i][0] != 0) {
				double[] tempRow = matrix[i];
				matrix[i] = matrix[0];
				matrix[0] = tempRow;
				return true;
			}
		}
		return false;
	}

	private void sendOneRowPerMsg(String reqId, double[][] matrix) {
		double[] firstRow = matrix[0];

		for (int i = 1; i < matrix.length; i++) {
			double[] row = matrix[i];
			workers.get(((i - 1) % workers.size())).getActorRef().tell(new Messages.OneRow(reqId, firstRow, row, i), getSelf());
			//if (i % 500 == 0) {
			//l.l(me, "sent row " + i + " to " + workers.get(((i - 1) % workers.size())).getName());
			//}
		}
	}

	private void sendManyRowsPerMsg(String reqId, double[][] matrix) {
		double[] firstRow = matrix[0];

		// TODO provare a migliorare il bilanciamento del lavoro
		int nRowsPerMsg = (matrix.length - 1) / workers.size();

		if (nRowsPerMsg > 0) {
			for (int i = 0; i < (workers.size() - 1); i++) {
				double[][] rows = new double[nRowsPerMsg][matrix.length];
				for (int j = 0; j < rows.length; j++) {
					rows[j] = matrix[i * nRowsPerMsg + j + 1];
				}
				workers.get(i).getActorRef().tell(new Messages.ManyRows(reqId, firstRow, rows, i * nRowsPerMsg + 1), getSelf());
                //l.l(me, "sent rows from " + (i*nRowsPerMsg+1) + " to " + (i*nRowsPerMsg+nRowsPerMsg) + " to " + workers.get(i).getName());
			}
		}
		double[][] rows = new double[matrix.length - 1 - nRowsPerMsg * (workers.size() - 1)][matrix.length];
		for (int j = 0; j < rows.length; j++) {
			rows[j] = matrix[(workers.size() - 1) * nRowsPerMsg + j + 1];
		}
		workers.get(workers.size() - 1).getActorRef().tell(new Messages.ManyRows(reqId, firstRow, rows, (workers.size() - 1) * nRowsPerMsg + 1), getSelf());
		//l.l(me, "sent rows from " + ((workers.size()-1)*nRowsPerMsg+1) + " to " + (matrix.length-1) + " to " + workers.get(workers.size()-1).getName());
	}

	private boolean gauss(String reqId) {
		MatrixInfo matrixInfo = matricesInfo.get(reqId);
		double[][] matrix = matrixInfo.getMatrix();
		boolean swapped = false;

		if (matrix[0][0] == 0) {
			swapped = swapFirtsRow(matrix);

			if (!swapped) {
                l.l(me, "zero column! determinant = 0. Duration: " + ((System.currentTimeMillis() - matrixInfo.getStartTime()) / (double) 1000) + " sec");
				manager.setResult(reqId, 0.0);
				manager.setPercentageDone(reqId, 100);
				return true;
			}
		}

		if (swapped) {
			matrixInfo.setChangeSign();
		}
		double oldDeterminant = matrixInfo.getDeterminant();
		matrixInfo.setDeterminant(oldDeterminant * matrix[0][0]);
		//sendOneRowPerMsg(reqId, matrix); // TODO provare a passare solo reqId
		sendManyRowsPerMsg(reqId,matrix);
		return false;
	}

	private double[][] subMatrix(String reqId, double[][] matrix) {
		double[][] subMatrix = new double[matrix.length - 1][matrix.length - 1];

		for (int i = 0; i < subMatrix.length; i++) {
			for (int j = 0; j < subMatrix.length; j++) {
				subMatrix[i][j] = matrix[i + 1][j + 1];
			}
		}
		return subMatrix;
	}

	private void handleRemoveWorkerSystem(String workerSystem) {
		String[] tokens;
		
		for (int i = 0; i < workers.size(); i++) {
			tokens = workers.get(i).getRemoteAddress().split("/user");
			//l.l(me, "worker system "+tokens[0]);
			if (tokens[0].equals(workerSystem)) {
				l.l(me, workers.get(i).getName() + " removed, workers size: " + (workers.size()-1));
				workers.remove(i);
				i--;				
			}
		}
	}
}
