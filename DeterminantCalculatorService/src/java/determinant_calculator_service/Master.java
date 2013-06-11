package determinant_calculator_service;

import log.l;
import java.util.ArrayList;

import akka.actor.UntypedActor;
import java.net.URL;
import java.util.HashMap;
import messages.Messages.Compute;
import messages.Messages.OneRow;
import messages.Messages.OneRowResult;
import messages.Messages.RegisterWorker;
import messages.Messages.RemoveWorker;

public class Master extends UntypedActor {

    private HashMap<String, MatrixInfo> matricesInfo;
	private ArrayList<RemoteWorker> workers;
	private HashMap<String, Integer> done;
	private long startTime;
	private DeterminantCalculatorManager manager;
	private String me;
	private HashMap<String, double[][]> matrices;
    private HashMap<String, Integer> matricesLength;
	private HashMap<String, Double> determinants;
	private HashMap<String, Boolean> changeSign;
	// TODO fare un unica struttura dati che comprende una richiesta, la matrice, lo stadio a cui si è arrivati ecc

	public Master() {
		// TODO è meglio che i workers stiano dentro al manager?
		workers = new ArrayList<RemoteWorker>();
		done = new HashMap<String, Integer>();
		me = getSelf().path().name();
		matrices = new HashMap<String, double[][]>();
        matricesLength = new HashMap<String, Integer>();
		determinants = new HashMap<String, Double>();
		changeSign = new HashMap<String, Boolean>();
        matricesInfo = new HashMap<String, MatrixInfo>();
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof Compute) {
			Compute c = (Compute) msg;
			handleCompute(c);
		} else if (msg instanceof OneRowResult) {
			OneRowResult orr = (OneRowResult) msg;
			handleOneRowResult(orr);
		} else if (msg instanceof RegisterWorker) {
			RegisterWorker rw = (RegisterWorker) msg;
			handleRegisterWorker(rw);
		} else if (msg instanceof RemoveWorker) {
			RemoveWorker rw = (RemoveWorker) msg;
			handleRemoveWorker(rw);
		} else {
			unhandled(msg);
		}
	}

	private void handleCompute(Compute compute) {
		l.l(me, "handleCompute, workers.size():" + workers.size());

		if (manager == null) {
			manager = compute.getManager();
		}
		startTime = System.currentTimeMillis();
		
        int order = compute.getOrder();
		URL fileValue = compute.getFileValues();
		String reqId = compute.getReqId();

		String path = System.getProperty("user.home") + System.getProperty("file.separator");
		String fileName = path + "matrix.txt";

		matrices.put(reqId, MatrixUtil.fromFileToList(order, fileName));
        
        int length = matrices.get(reqId).length;
        matricesLength.put(reqId,length);
		l.l(me, "Matrix length: " + length);

		done.put(reqId, 0);
		determinants.put(reqId, 1.0);
		changeSign.put(reqId, false);

		if (workers.isEmpty()) {
			l.l(me, "\nWORKERS.SIZE() = 0 !!!!\n");
			return;
		}

        gauss(reqId);
	}

	private void handleOneRowResult(OneRowResult orr) {
		String reqId = orr.getReqId();
		double[] row = orr.getRow();
		int rowNumber = orr.getRowNumber();
		
        int nRowsDone = done.get(reqId);
		nRowsDone++;
		done.put(reqId, nRowsDone);

		/*if (nRowsDone % 500 == 0) {
			l.l(me, "nRowsDone: " + nRowsDone);
		}*/

		double[][] matrix = matrices.get(reqId);
		matrix[rowNumber] = row;
		// TODO è necessario fare la put o è implicita come side effect?
		matrices.put(reqId, matrix);

		if (nRowsDone == matrix.length - 1) {
            if (matrix.length % 500 == 0){
                l.l(me, "Received all rows for submatrix " + matrix.length + ". Duration: " + ((System.currentTimeMillis() - startTime) / (double) 1000) + " sec");
            }
                
			if (matrix.length > 2){
				done.put(reqId, 0);
				subMatrix(reqId, matrix);
				gauss(reqId);
			} else {
                l.l(me, "Received all rows for submatrix " + matrix.length + ". Duration: " + ((System.currentTimeMillis() - startTime) / (double) 1000) + " sec");
				double oldDeterminant = determinants.get(reqId);
				double determinant = oldDeterminant * matrix[1][1];

				if (!changeSign.get(reqId)){
					determinants.put(reqId, determinant);
					manager.setResult(reqId, determinant); // TODO
				} else {
					determinants.put(reqId, -determinant);
					manager.setResult(reqId, -determinant); // TODO
				}
				//manager.setPercentageDone(reqId, 100);
			}
            manager.setPercentageDone(reqId, (matricesLength.get(reqId)-matrices.get(reqId).length)*100/(matricesLength.get(reqId)-2));
		}
	}

	private void handleRegisterWorker(RegisterWorker rw) {
		String remoteAddress = rw.getRemoteAddress();
		RemoteWorker worker = new RemoteWorker(remoteAddress, getContext().actorFor(remoteAddress));
		workers.add(worker);
		l.l(me, "worker added, workers size: " + workers.size());
	}

	private void handleRemoveWorker(RemoveWorker rw) {
		String remoteAddress = rw.getRemoteAddress();

		// TODO si potrebbe usare una hashmap per rendere la ricerca più performante
		for (int i = 0; i < workers.size(); i++) {
			if (workers.get(i).getRemoteAddress().equals(remoteAddress)) {
				workers.remove(i);
			}
		}
		l.l(me, "worker removed, workers size: " + workers.size());
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
			workers.get(((i - 1) % workers.size())).getActorRef().tell(new OneRow(reqId, firstRow, row, i), getSelf());
			/*if (i % 500 == 0) {
				l.l(me, "sent row " + i + " to worker" + ((i - 1) % workers.size()));
			}*/
		}
	}

	private void gauss(String reqId){
		double[][] matrix = matrices.get(reqId);
		boolean swapped = false;

		if (matrix[0][0] == 0) {
			swapped = swapFirtsRow(matrix);

			if (!swapped) {
				manager.setResult(reqId, 0.0);
				return;
			}
		}

		if (swapped){
			changeSign.put(reqId, !changeSign.get(reqId));
		}
		double oldDeterminant = determinants.get(reqId);
		determinants.put(reqId, oldDeterminant * matrix[0][0]);
		sendOneRowPerMsg(reqId, matrix); // TODO provare a passare solo reqId
	}

	private void subMatrix(String reqId, double[][] matrix){
		double[][] subMatrix = new double[matrix.length-1][matrix.length-1];

		for (int i = 0; i < subMatrix.length; i++){
			for (int j = 0; j < subMatrix.length; j++){
				subMatrix[i][j] = matrix[i + 1][j + 1];
			}
		}
		matrices.put(reqId, subMatrix);
	}
}
