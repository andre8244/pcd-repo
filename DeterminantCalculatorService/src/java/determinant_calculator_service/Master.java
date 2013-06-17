package determinant_calculator_service;

import akka.actor.ActorRef;
import log.l;
import java.util.ArrayList;

import akka.actor.UntypedActor;
import akka.remote.RemoteClientShutdown;
import akka.remote.RemoteClientWriteFailed;
import akka.remote.RemoteLifeCycleEvent;
import messages.Messages;

public class Master extends UntypedActor {

	private ArrayList<RemoteWorker> workers;
	private DeterminantCalculatorManager manager;
	private String me;

	public Master() {
		workers = new ArrayList<RemoteWorker>();
		me = getSelf().path().name();
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
//		} else if (msg instanceof Messages.OneRowResult) {
//			Messages.OneRowResult orr = (Messages.OneRowResult) msg;
//			handleOneRowResult(orr);
		} else if (msg instanceof Messages.ManyRowsResult) {
			Messages.ManyRowsResult mrr = (Messages.ManyRowsResult) msg;
			handleManyRowsResult(mrr);
		} else if (msg instanceof Messages.AddWorkerNode) {
			Messages.AddWorkerNode rw = (Messages.AddWorkerNode) msg;
			handleAddWorkerNode(rw);
		} else if (msg instanceof Messages.RemoveWorkerNode) {
			Messages.RemoveWorkerNode rw = (Messages.RemoveWorkerNode) msg;
			handleRemoveWorkerNode(rw);
		} else if (msg instanceof RemoteClientWriteFailed) {
			l.l(me, "Remote Client Write Failed!");
			RemoteClientWriteFailed rcwf = (RemoteClientWriteFailed) msg;
			l.l(me, "address: " + rcwf.getRemoteAddress().toString());
			handleWorkerFailure(rcwf.getRemoteAddress().toString());
		} else if (msg instanceof RemoteClientShutdown) {
			l.l(me, "Remote Client Shutdown!");
			RemoteClientShutdown rcs = (RemoteClientShutdown) msg;
			l.l(me, "address: " + rcs.getRemoteAddress().toString());
			handleWorkerFailure(rcs.getRemoteAddress().toString());
		} else {
			unhandled(msg);
		}
	}

	private void handleCompute(Messages.Compute compute) {
		if (manager == null) {
			manager = compute.getManager(); // TODO è meglio passare il manager come parametro di costruzione del master
		}
		int order = compute.getOrder();
		String fileValues = compute.getFileValues();
		String reqId = compute.getReqId();
		l.l(me, "handleCompute, reqId: " + reqId + ", order: " + order + ", workers.size():" + workers.size());
		RequestInfo requestInfo = manager.getRequestInfo(reqId);
		requestInfo.setOriginalMatrix(MatrixUtil.fromFileToList(order, fileValues));

		if (workers.isEmpty()) {
			l.l(me, "\nWORKERS.SIZE() = 0 !!!!\n");
			requestInfo.setFinalDeterminant(-0.0);
			requestInfo.setPercentageDone(100); // TODO comunico al client di aver finito anche se non ho calcolato niente
			return;
		}

		gauss(reqId);
	}

	private void handleManyRowsResult(Messages.ManyRowsResult mrr) {
		String reqId = mrr.getReqId();
		double[][] rows = mrr.getRows();
		int rowNumber = mrr.getRowNumber();

		for (int i = 0; i < workers.size(); i++) {
			// works.size è generalmente uno, tranne in casi di failure
			ArrayList<Work> works = workers.get(i).getWorks();
			for (int j = 0; j < works.size(); j++) {
				if (works.get(j).getReqId().equals(reqId) && works.get(j).getRowNumber() == rowNumber) {
					workers.get(i).removeWork(works.get(j));
					break;
				}
			}
		}

		RequestInfo requestInfo = manager.getRequestInfo(reqId);

		int nRowsDone = requestInfo.getRowsDone();
		nRowsDone = nRowsDone + rows.length;
		requestInfo.setRowsDone(nRowsDone);

		//l.l(me, reqId + ", receive rows from " + rowNumber + " to " + (rowNumber+rows.length-1) + "(" + rows.length + " rows)");
		//l.l(me, reqId + ", nRowsDone: " + nRowsDone);

		double[][] matrix = requestInfo.getMatrix();
		System.arraycopy(rows, 0, matrix, rowNumber, rows.length);

		// TODO serve? oppure è sufficiente il side-effect?
		requestInfo.setMatrix(matrix);

		if (nRowsDone == matrix.length - 1) {
			if (matrix.length % 500 == 0) {
				l.l(me, "Received all rows for " + reqId + ", submatrix " + matrix.length + ". Duration: " + ((System.currentTimeMillis() - requestInfo.getStartTime()) / (double) 1000) + " sec");
			}

			if (matrix.length > 2) {
				requestInfo.setRowsDone(0);
				requestInfo.setMatrix(subMatrix(reqId, matrix));

				if (workers.isEmpty()) {
					l.l(me, "\nWORKERS.SIZE() = 0 !!!!\n");
					requestInfo.setFinalDeterminant(-0.0);
					requestInfo.setPercentageDone(100);
					return;
				}
				boolean zeroColumn = gauss(reqId);

				if (!zeroColumn) {
					long startTime = System.currentTimeMillis();
					int totalWorkToDo = requestInfo.getTotalWorkToDo();
					//l.l(me, "totalWorkToDo: " + totalWorkToDo);
					int workToDo = 0;

					for (int i = requestInfo.getMatrix().length - 1; i > 0; i--) {
						workToDo += i * (i + 1);
					}
					//l.l(me, "WorkToDo: " + workToDo);
					int workDone = totalWorkToDo - workToDo;
					//l.l(me, "WorkDone: " + workDone);
					int percentage = (int) (((double) workDone / totalWorkToDo) * 100);
					//l.l(me, "percentage: " + percentage);
					//l.l(me, "percentage computation duration: " + (System.currentTimeMillis() - startTime) + " ms");
					requestInfo.setPercentageDone(percentage);
				}
			} else { // matrix.length = 2
				l.l(me, "Received ALL rows for " + reqId + ", submatrix " + matrix.length + ". Duration: " + ((System.currentTimeMillis() - requestInfo.getStartTime()) / (double) 1000) + " sec");
				double oldDeterminant = requestInfo.getTempDeterminant();
				double determinant = oldDeterminant * matrix[1][1];

				if (determinant == 0) { // needed in case determinant = -0.0
					l.l(me, "determinant == 0 || determinant == -0, determinant: " + determinant);
					requestInfo.setFinalDeterminant(0);
				} else {
					if (!requestInfo.getChangeSign()) {
						l.l(me, "NOT CHANGING SIGN: determinant: " + determinant);
						//requestInfo.setTempDeterminant(determinant);
						requestInfo.setFinalDeterminant(determinant);
					} else {
						l.l(me, "CHANGING SIGN: determinant: " + determinant);
						//requestInfo.setTempDeterminant(-determinant);
						requestInfo.setFinalDeterminant(-determinant);
					}
				}
				requestInfo.setPercentageDone(100);

//				if (!requestInfo.getChangeSign()) {
//					l.l(me, "NOT CHANGING SIGN: determinant: " + determinant);
//					//requestInfo.setTempDeterminant(determinant);
//					requestInfo.setFinalDeterminant(determinant);
//				} else {
//					l.l(me, "CHANGING SIGN: determinant: " + determinant);
//					if (determinant == 0) {
//						//requestInfo.setTempDeterminant(0);
//						l.l(me, "determinant == 0, determinant: " + determinant);
//						requestInfo.setFinalDeterminant(0);
//					} else {
//						l.l(me, "determinant != 0: determinant: " + determinant);
//						//requestInfo.setTempDeterminant(-determinant);
//						requestInfo.setFinalDeterminant(-determinant);
//					}
//				}
//				requestInfo.setPercentageDone(100);
			}
		}
	}

	private void handleAddWorkerNode(Messages.AddWorkerNode rw) {
		String remoteAddress = rw.getRemoteAddress();
		ActorRef actorRef = getContext().actorFor(remoteAddress);
		RemoteWorker worker = new RemoteWorker(remoteAddress, actorRef);
		workers.add(worker);
		actorRef.tell(new Messages.AddWorkerNodeAck(), getSelf());
		l.l(me, worker.getRemoteAddress() + " added, workers size: " + workers.size());
	}

	private void handleRemoveWorkerNode(Messages.RemoveWorkerNode rw) {
		String remoteAddress = rw.getRemoteAddress();

		// TODO si potrebbe usare una hashmap per rendere la ricerca più performante
		for (int i = 0; i < workers.size(); i++) {
			if (workers.get(i).getRemoteAddress().equals(remoteAddress)) {
				RemoteWorker worker = workers.remove(i);
				worker.getActorRef().tell(new Messages.RemoveWorkerNodeAck(), getSelf());
				l.l(me, worker.getRemoteAddress() + " removed, workers size: " + workers.size());
				return;
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

	private void oldSendManyRowsPerMsg(String reqId, double[][] matrix) {
		double[] firstRow = matrix[0];

		// TODO provare a migliorare il bilanciamento del lavoro
		int nRowsPerMsg = (matrix.length - 1) / workers.size();

		if (nRowsPerMsg > 0) {
			for (int i = 0; i < (workers.size() - 1); i++) {
				double[][] rows = new double[nRowsPerMsg][matrix.length];

				for (int j = 0; j < rows.length; j++) {
					rows[j] = matrix[i * nRowsPerMsg + j + 1];
				}
				workers.get(i).addWork(reqId, rows, i * nRowsPerMsg + 1);
				workers.get(i).getActorRef().tell(new Messages.ManyRows(reqId, firstRow, rows, i * nRowsPerMsg + 1), getSelf());
				//l.l(me, "sent rows from " + (i*nRowsPerMsg+1) + " to " + (i*nRowsPerMsg+nRowsPerMsg) + " to " + workers.get(i).getRemoteAddress());
			}
		}
		double[][] rows = new double[matrix.length - 1 - nRowsPerMsg * (workers.size() - 1)][matrix.length];

		for (int j = 0; j < rows.length; j++) {
			rows[j] = matrix[(workers.size() - 1) * nRowsPerMsg + j + 1];
		}
		workers.get(workers.size() - 1).addWork(reqId, rows, (workers.size() - 1) * nRowsPerMsg + 1);
		workers.get(workers.size() - 1).getActorRef().tell(new Messages.ManyRows(reqId, firstRow, rows, (workers.size() - 1) * nRowsPerMsg + 1), getSelf());
		//l.l(me, "sent rows from " + ((workers.size()-1)*nRowsPerMsg+1) + " to " + (matrix.length-1) + " to " + workers.get(workers.size()-1).getRemoteAddress());
	}

	private void sendManyRowsPerMsg(String reqId, double[][] matrix) {
		double[] firstRow = matrix[0];

		double nRowsPerWorker = (double) (matrix.length - 1) / workers.size();
		//l.l(me, "nRowsPerWorker: " + nRowsPerWorker);
		int nRowsSent = 0;
		int nRowsToSend;

		for (int i = 0; i < (workers.size()); i++) {
			// number of rows to send to worker i:
			nRowsToSend = (int) (Math.round((i + 1) * nRowsPerWorker) - nRowsSent);

			if (nRowsToSend > 0) {
				double[][] rows = new double[nRowsToSend][matrix.length];

				for (int j = 0; j < rows.length; j++) {
					rows[j] = matrix[nRowsSent + j + 1];
				}
				workers.get(i).addWork(reqId, rows, nRowsSent + 1);
				workers.get(i).getActorRef().tell(new Messages.ManyRows(reqId, firstRow, rows, nRowsSent + 1), getSelf());
				//l.l(me, reqId + ", sent rows from " + (nRowsSent+1) + " to " + (nRowsSent+nRowsToSend) + "(" + nRowsToSend + " rows) to " + workers.get(i).getRemoteAddress());
				nRowsSent += nRowsToSend;
			}
		}
	}

	private boolean gauss(String reqId) {
		RequestInfo requestInfo = manager.getRequestInfo(reqId);
		double[][] matrix = requestInfo.getMatrix();
		boolean swapped = false;

		if (matrix[0][0] == 0) {
			swapped = swapFirtsRow(matrix);

			if (!swapped) {
				l.l(me, "zero column! determinant = 0. Duration: " + ((System.currentTimeMillis() - requestInfo.getStartTime()) / (double) 1000) + " sec");
				requestInfo.setFinalDeterminant(0);
				requestInfo.setPercentageDone(100);
				return true;
			}
		}

		if (swapped) {
			requestInfo.setChangeSign();
		}
		double oldDeterminant = requestInfo.getTempDeterminant();
		requestInfo.setTempDeterminant(oldDeterminant * matrix[0][0]);
		sendManyRowsPerMsg(reqId, matrix); // TODO provare a passare solo reqId
		//oldSendManyRowsPerMsg(reqId,matrix);
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

	private void handleWorkerFailure(String workerSystem) {
		String[] tokens;
		l.l(me, "handle failed");
		boolean first = true;
		int index = 0;
		for (int i = 0; i < workers.size(); i++) {
			tokens = workers.get(i).getRemoteAddress().split("/user");
			String workerSystemMain = tokens[0];
			//l.l(me, "worker system "+tokens[0]);
			if (workerSystemMain.equals(workerSystem)) {
				RemoteWorker worker = workers.get(i);
				ArrayList<Work> works = worker.getWorks();
				l.l(me, worker.getRemoteAddress() + " pending request: " + works.size());
				// nel caso di shutdown -> pending request = 0 !
				for (int j = 0; j < works.size(); j++) {
					if (first) {
						index = (i + 1) % workers.size();
						first = false;
					}
					String reqId = works.get(j).getReqId();
					double[][] rows = works.get(j).getRows();
					int rowNumber = works.get(j).getRowNumber();
					RequestInfo requestInfo = manager.getRequestInfo(reqId);
					// caso particolare: non abbiamo altri worker a disposizione
					if (workers.size() < 2) {
						l.l(me, "\nWORKERS.SIZE() = 0 !!!!\n");
						requestInfo.setFinalDeterminant(-0.0);
						requestInfo.setPercentageDone(100);
						continue;
					}
					double[] firstRow = requestInfo.getMatrix()[0];
					for (int k = 0; k < workers.size(); k++) {
						index = index % workers.size();
						tokens = workers.get(index).getRemoteAddress().split("/user");
						if (!tokens[0].equals(workerSystem)) {
							l.l(me, "index: " + index);
							workers.get(index).addWork(reqId, rows, rowNumber);
							workers.get(index).getActorRef().tell(new Messages.ManyRows(reqId, firstRow, rows, rowNumber), getSelf());
							l.l(me, reqId + ", call " + workers.get(index).getRemoteAddress() + " to do the job of the dead worker " + worker.getRemoteAddress());
							if (j == works.size() - 1 && index < i) {
								index = (index + 1) % workers.size();
							}
							break;
						}
						index = (index + 1) % workers.size();
					}
				}
				// shutdown
				workers.remove(i);
				l.l(me, worker.getRemoteAddress() + " removed, workers size: " + workers.size());
				i--;
			}
		}
	}
}
