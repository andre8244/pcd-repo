package determinant_calculator_service;

import java.util.ArrayList;

import log.l;
import messages.Messages;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.remote.RemoteClientShutdown;
import akka.remote.RemoteClientWriteFailed;
import akka.remote.RemoteLifeCycleEvent;

/**
 * An actor that manages one or more computation requests.
 *
 */
public class Master extends UntypedActor {

	private ArrayList<WorkerInfo> workers;
	private DeterminantCalculatorManager manager;
	private String me;
	private static final int MAX_ELEMS_PER_MSG = 1000000;

	/**
	 * Constructs a master actor.
	 */
	public Master(DeterminantCalculatorManager manager) {
		this.manager = manager;
		workers = new ArrayList<WorkerInfo>();
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
		} else if (msg instanceof Messages.OneRowResult) {
			Messages.OneRowResult orr = (Messages.OneRowResult) msg;
			handleOneRowResult(orr);
		} else if (msg instanceof Messages.ManyRowsResult) {
			Messages.ManyRowsResult mrr = (Messages.ManyRowsResult) msg;
			handleManyRowsResult(mrr);
		} else if (msg instanceof Messages.AddWorker) {
			Messages.AddWorker rw = (Messages.AddWorker) msg;
			handleAddWorker(rw);
		} else if (msg instanceof Messages.RemoveWorker) {
			Messages.RemoveWorker rw = (Messages.RemoveWorker) msg;
			handleRemoveWorker(rw);
		} else if (msg instanceof RemoteClientWriteFailed) {
			RemoteClientWriteFailed rcwf = (RemoteClientWriteFailed) msg;
			l.l(me, "Remote Client Write Failed: " + rcwf.getRemoteAddress().toString());
			handleWorkerFailure(rcwf.getRemoteAddress().toString());
		} else if (msg instanceof RemoteClientShutdown) {
			RemoteClientShutdown rcs = (RemoteClientShutdown) msg;
			l.l(me, "Remote Client Shutdown: " + rcs.getRemoteAddress().toString());
			handleWorkerFailure(rcs.getRemoteAddress().toString());
		} else {
			unhandled(msg);
		}
	}

	private void handleCompute(Messages.Compute compute) {
		int order = compute.getOrder();
		String fileValues = compute.getFileValues();
		String reqId = compute.getReqId();
		l.l(me, "handleCompute, reqId: " + reqId + ", order: " + order + ", workers.size():" + workers.size());
		RequestInfo requestInfo = manager.getRequestInfo(reqId);
		MatrixReader matrixReader = new MatrixReader();
		boolean set = requestInfo.setOriginalMatrix(matrixReader.read(order, fileValues));

		if (!set) {
			l.l(me, reqId + ", matrix Error !!!");
			requestInfo.setPercentageDone(100);
			requestInfo.setFinalDeterminant(-0.0);
			return;
		}

		if (workers.isEmpty()) {
			l.l(me, reqId + ", WORKERS.SIZE() = 0 !!!");
			requestInfo.setPercentageDone(100);
			requestInfo.setFinalDeterminant(-0.0);
			return;
		}
		gauss(requestInfo,reqId);
	}

	// TODO eliminare
	private void handleOneRowResult(Messages.OneRowResult orr) {
		String reqId = orr.getReqId();
		int rowNumber = orr.getRowNumber();

		for (int i = 0; i < workers.size(); i++) {
			// works.size è generalmente uguale al numero di richieste in corso, tranne in casi di failure
			ArrayList<GaussJob> works = workers.get(i).getJobs();
			for (int j = 0; j < works.size(); j++) {
				if (works.get(j).getReqId().equals(reqId) && works.get(j).getRowNumber() == rowNumber) {
					workers.get(i).removeJob(works.get(j));
					break;
				}
			}
		}
		RequestInfo requestInfo = manager.getRequestInfo(reqId);
		int nRowsDone = requestInfo.updateRowsDone(1);

		if (nRowsDone % 500 == 0){
			l.l(me, reqId + ", 	received row " + rowNumber + " , nRowsDone: " + nRowsDone);
		}

		requestInfo.updateCurrentMatrix(orr.getRow(), rowNumber);
		int currentOrder = requestInfo.getCurrentOrder();
		
		if (nRowsDone == currentOrder - 1) {
			if (currentOrder % 500 == 0) {
				l.l(me,
						reqId + ", received all rows, submatrix " + currentOrder + ". Duration: "
						+ ((System.currentTimeMillis() - requestInfo.getStartTime()) / (double) 1000) + " sec");
			}
			if (currentOrder > 2) {
				requestInfo.subMatrix();

				if (workers.isEmpty()) {
					l.l(me, reqId + ", WORKERS.SIZE() = 0 !!!");
					requestInfo.setPercentageDone(100);
					requestInfo.setFinalDeterminant(-0.0);
					return;
				}
				boolean zeroColumn = gauss(requestInfo,reqId);

				if (!zeroColumn) {
					long startTime = System.currentTimeMillis();
					long totalWorkToDo = requestInfo.getTotalWorkToDo();
					long workToDo = 0;

					for (long i = requestInfo.getCurrentOrder() - 1; i > 0; i--) {
						workToDo += i * (i + 1);
					}
					// l.l(me, "WorkToDo: " + workToDo);
					long workDone = totalWorkToDo - workToDo;
					// l.l(me, "workDone(" + workDone + ") = totalWorkToDo("+ totalWorkToDo + ") - workToDo(" + workToDo
					// + ")");
					// l.l(me, "WorkDone: " + workDone);
					int percentage = (int) (((double) workDone / totalWorkToDo) * 100);
					// l.l(me, "percentage: " + percentage);
					// l.l(me, "percentage computation duration: " + (System.currentTimeMillis() - startTime) + " ms");
					requestInfo.setPercentageDone(percentage);
				}
			} else { // matrix.length = 2
				l.l(me,
						reqId + ", received all rows, submatrix " + currentOrder + ". Duration: "
						+ ((System.currentTimeMillis() - requestInfo.getStartTime()) / (double) 1000) + " sec");
				double determinant = requestInfo.updateLastTempDeterminant();
				requestInfo.setPercentageDone(100);

				if (determinant == 0) { // needed in case determinant = -0.0
					l.l(me, reqId + ", determinant == 0 || determinant == -0, determinant: " + determinant);
					requestInfo.setFinalDeterminant(0);
				} else {
					if (!requestInfo.getChangeSign()) {
						l.l(me, reqId + ", NOT CHANGING SIGN: determinant: " + determinant);
						requestInfo.setFinalDeterminant(determinant);
					} else {
						l.l(me, reqId + ", CHANGING SIGN: determinant: " + (-determinant));
						requestInfo.setFinalDeterminant(-determinant);
					}
				}
			}
		}
	}

	private void handleManyRowsResult(Messages.ManyRowsResult mrr) {
		String reqId = mrr.getReqId();
		double[][] rows = mrr.getRows();
		int rowNumber = mrr.getRowNumber();

		for (int i = 0; i < workers.size(); i++) {
			// works.size è generalmente uguale al numero di richieste in corso, tranne in casi di failure
			ArrayList<GaussJob> works = workers.get(i).getJobs();

			for (int j = 0; j < works.size(); j++) {
				if (works.get(j).getReqId().equals(reqId) && works.get(j).getRowNumber() == rowNumber) {
					workers.get(i).removeJob(works.get(j));
					break;
				}
			}
		}
		RequestInfo requestInfo = manager.getRequestInfo(reqId);
		int nRowsDone = requestInfo.updateRowsDone(rows.length);
		l.l(me, reqId + ", receive rows from " + rowNumber + " to " + (rowNumber + rows.length - 1) + "(" + rows.length
				+ " rows)");
		l.l(me, reqId + ", nRowsDone: " + nRowsDone);

		requestInfo.updateCurrentMatrix(rows, rowNumber);
		int currentOrder = requestInfo.getCurrentOrder();
		
		if (nRowsDone == currentOrder - 1) {
			if (currentOrder % 500 == 0) {
				l.l(me,
						reqId + ", received all rows, submatrix " + currentOrder + ". Duration: "
						+ ((System.currentTimeMillis() - requestInfo.getStartTime()) / (double) 1000) + " sec");
			}

			if (currentOrder > 2) {
				requestInfo.subMatrix();

				if (workers.isEmpty()) {
					l.l(me, reqId + ", WORKERS.SIZE() = 0 !!!");
					requestInfo.setPercentageDone(100);
					requestInfo.setFinalDeterminant(-0.0);
					return;
				}
				boolean zeroColumn = gauss(requestInfo, reqId);

				if (!zeroColumn) {
					long startTime = System.currentTimeMillis();
					long totalWorkToDo = requestInfo.getTotalWorkToDo();
					long workToDo = 0;

					for (long i = requestInfo.getCurrentOrder() - 1; i > 0; i--) {
						workToDo += i * (i + 1);
					}
					long workDone = totalWorkToDo - workToDo;
					int percentage = (int) (((double) workDone / totalWorkToDo) * 100);
					// TODO testare la durata del calcolo di percentuale con 10000
//					l.l(me, "percentage computation duration: " + (System.currentTimeMillis() - startTime) + " ms");
					requestInfo.setPercentageDone(percentage);
				}
			} else { // matrix.length = 2
				l.l(me,
						reqId + ", received all rows, submatrix " + currentOrder + ". Duration: "
						+ ((System.currentTimeMillis() - requestInfo.getStartTime()) / (double) 1000) + " sec");
				double determinant = requestInfo.updateLastTempDeterminant();
				requestInfo.setPercentageDone(100);

				if (determinant == 0) { // needed when determinant = -0.0
					l.l(me, reqId + ", determinant == 0 || determinant == -0, determinant: " + determinant);
					requestInfo.setFinalDeterminant(0);
				} else {
					if (!requestInfo.getChangeSign()) {
						l.l(me, reqId + ", NOT CHANGING SIGN: determinant: " + determinant);
						requestInfo.setFinalDeterminant(determinant);
					} else {
						l.l(me, reqId + ", CHANGING SIGN: determinant: " + (-determinant));
						requestInfo.setFinalDeterminant(-determinant);
					}
				}
			}
		}
	}

	private void handleAddWorker(Messages.AddWorker rw) {
		String remoteAddress = rw.getRemoteAddress();
		ActorRef actorRef = getContext().actorFor(remoteAddress);
		WorkerInfo worker = new WorkerInfo(remoteAddress, actorRef);
		workers.add(0, worker);
		actorRef.tell(new Messages.AddWorkerAck(), getSelf());
		l.l(me, worker.getRemoteAddress() + " added, workers size: " + workers.size());
	}

	private void handleRemoveWorker(Messages.RemoveWorker rw) {
		String remoteAddress = rw.getRemoteAddress();

		for (int i = 0; i < workers.size(); i++) {
			if (workers.get(i).getRemoteAddress().equals(remoteAddress)) {
				WorkerInfo worker = workers.get(i);
				int index = (i + 1) % workers.size();
				ArrayList<GaussJob> works = worker.getJobs();
				l.l(me, worker.getRemoteAddress() + " pending request: " + works.size());
				// nel caso di shutdown -> pending request = 0 !
				for (int j = 0; j < works.size(); j++) {
					String reqId = works.get(j).getReqId();
					int nRows = works.get(j).getNRows();
					int rowNumber = works.get(j).getRowNumber();
					RequestInfo requestInfo = manager.getRequestInfo(reqId);

					// caso particolare: non abbiamo altri worker a disposizione
					if (workers.size() < 2) {
						l.l(me, reqId + ", WORKERS.SIZE() = 0 !!!");
						requestInfo.setPercentageDone(100);
						requestInfo.setFinalDeterminant(-0.0);
						continue;
					}
					
					worker.removeJob(works.get(j)); // rimuovo prima il job dal worker per liberare memoria
					//l.l(me, "index: " + index);
					workers.get(index).addJob(reqId, nRows, rowNumber);
					workers.get(index).getActorRef()
							.tell(new Messages.ManyRows(reqId, requestInfo.getFirstRow(), requestInfo.getRows(nRows,rowNumber), rowNumber), getSelf());
					//l.l(me, reqId + ", handleRemove, sent row " + rowNumber + " to " + workers.get(index).getRemoteAddress());
					//l.l(me, reqId + ", call " + workers.get(index).getRemoteAddress()
					//		+ " to do the job of the removed worker " + worker.getRemoteAddress());
				}
				// shutdown
				workers.remove(i);
				l.l(me, worker.getRemoteAddress() + " removed, workers size: " + workers.size());
				return;
			}
		}
	}

	private void sendOneRowPerMsg(RequestInfo requestInfo, String reqId) {
		for (int i = 1; i < requestInfo.getCurrentOrder(); i++) {
			workers.get(((i - 1) % workers.size())).addJob(reqId, 1, i);
			workers.get(((i - 1) % workers.size())).getActorRef()
					.tell(new Messages.OneRow(reqId, requestInfo.getFirstRow(), requestInfo.getRow(i), i), getSelf());
			if (i % 500 == 0) {
				l.l(me, reqId + ", sent row " + i + " to " + workers.get(((i - 1) % workers.size())).getRemoteAddress());
			}
		}
	}

	private void sendRowsToWorkers(RequestInfo requestInfo, String reqId) {
		int currentOrder = requestInfo.getCurrentOrder();
		double nRowsPerWorker = (double) (currentOrder - 1) / workers.size();
		// l.l(me, "nRowsPerWorker: " + nRowsPerWorker);
		int nRowsSent = 0;
		int nRowsToSend;

		for (int i = 0; i < (workers.size()); i++) {
			// number of rows to send to worker i:
			nRowsToSend = (int) (Math.round((i + 1) * nRowsPerWorker) - nRowsSent);
//			l.l(me, "nRowsToSend: " + nRowsToSend);

			if (nRowsToSend > 0) {

				int maxNRowsPerMsg = MAX_ELEMS_PER_MSG / currentOrder; // MAX_ELEMS_PER_MSG deve essere maggiore dell'ordine massimo possibile

				int nMsgToSend = (int) (nRowsToSend / maxNRowsPerMsg);
//				l.l(me, "nMsgToSend with " + (maxNRowsPerMsg*matrix.length) + " elements: " + nMsgToSend);

				int nRowsOfFirstMsg = nRowsToSend % maxNRowsPerMsg;
//				l.l(me, "nRowsOfFirstMsg: " + nRowsOfFirstMsg);

				if (nRowsOfFirstMsg != 0) {
					workers.get(i).addJob(reqId, nRowsOfFirstMsg, nRowsSent + 1);
					workers.get(i).getActorRef()
							.tell(new Messages.ManyRows(reqId, requestInfo.getFirstRow(), requestInfo.getRows(nRowsOfFirstMsg, nRowsSent+1), nRowsSent + 1), getSelf());
//					l.l(me, reqId + ", sent rows from " + (nRowsSent + 1) + " to " + (nRowsSent + nRowsOfFirstMsg) + "("
//							+ nRowsOfFirstMsg + " rows) to " + workers.get(i).getRemoteAddress());
					nRowsSent += nRowsOfFirstMsg;
				}

				for (int j = 0; j < nMsgToSend; j++) {
					workers.get(i).addJob(reqId, maxNRowsPerMsg, nRowsSent + 1);
					workers.get(i).getActorRef()
							.tell(new Messages.ManyRows(reqId, requestInfo.getFirstRow(), requestInfo.getRows(maxNRowsPerMsg, nRowsSent+1), nRowsSent + 1), getSelf());
//					l.l(me, reqId + ", sent rows from " + (nRowsSent + 1) + " to " + (nRowsSent + maxNRowsPerMsg) + "("
//							+ maxNRowsPerMsg + " rows) to " + workers.get(i).getRemoteAddress());
					nRowsSent += maxNRowsPerMsg;
				}
			}
		}
	}

	/**
	 * Implements the Gaussian elimination algorithm. If a zero column is
	 * detected the computation can stop, the determinant of the matrix is zero.
	 *
	 * @param reqId the request of interest
	 * @return <code>true</code> if a zero column is
	 * detected, <code>false</code> otherwise
	 */
	private boolean gauss(RequestInfo requestInfo, String reqId) {
		boolean swapped = false;

		if (requestInfo.getFirstElement() == 0) {
			swapped = requestInfo.swapFirtsRow();

			if (!swapped) {
				l.l(me, reqId + ", zero column! determinant = 0. Duration: "
						+ ((System.currentTimeMillis() - requestInfo.getStartTime()) / (double) 1000) + " sec");
				requestInfo.setPercentageDone(100);
				requestInfo.setFinalDeterminant(0);
				return true;
			}
		}

		if (swapped) {
			requestInfo.setChangeSign();
		}
		requestInfo.updateTempDeterminant();
		sendRowsToWorkers(requestInfo,reqId);
		//sendOneRowPerMsg(requestInfo,reqId);
		return false;
	}

	private void handleWorkerFailure(String workerSystem) {
		String[] tokens;
		l.l(me, "handle failed");
		boolean first = true;
		int index = 0;

		for (int i = 0; i < workers.size(); i++) {
			tokens = workers.get(i).getRemoteAddress().split("/user");
			String workerSystemMain = tokens[0];
			// l.l(me, "worker system "+tokens[0]);

			if (workerSystemMain.equals(workerSystem)) {
				WorkerInfo worker = workers.get(i);
				ArrayList<GaussJob> works = worker.getJobs();
				l.l(me, worker.getRemoteAddress() + " pending request: " + works.size());
				// nel caso di shutdown -> pending request = 0 !
				for (int j = 0; j < works.size(); j++) {
					if (first) {
						index = (i + 1) % workers.size();
						first = false;
					}
					String reqId = works.get(j).getReqId();
					int nRows = works.get(j).getNRows();
					int rowNumber = works.get(j).getRowNumber();
					RequestInfo requestInfo = manager.getRequestInfo(reqId);

					// caso particolare: non abbiamo altri worker a disposizione
					if (workers.size() < 2) {
						l.l(me, reqId + ", WORKERS.SIZE() = 0 !!!");
						requestInfo.setPercentageDone(100);
						requestInfo.setFinalDeterminant(-0.0);
						continue;
					}
					
					worker.removeJob(works.get(j)); // rimuovo prima il job dal worker per liberare memoria

					for (int k = 0; k < workers.size(); k++) {
						index = index % workers.size();
						tokens = workers.get(index).getRemoteAddress().split("/user");

						if (!tokens[0].equals(workerSystem)) {
							l.l(me, "index: " + index);
							workers.get(index).addJob(reqId, nRows, rowNumber);
							workers.get(index).getActorRef()
									.tell(new Messages.ManyRows(reqId, requestInfo.getFirstRow(), requestInfo.getRows(nRows, rowNumber), rowNumber), getSelf());
							l.l(me, reqId + ", call " + workers.get(index).getRemoteAddress()
									+ " to do the job of the dead worker " + worker.getRemoteAddress());

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
