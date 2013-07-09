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
	private static final int MAX_ELEMS_PER_MSG = 500000; // TODO default: 10 milioni

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
		} else if (msg instanceof Messages.RowsResult) {
			Messages.RowsResult rr = (Messages.RowsResult) msg;
			handleRowsResult(rr);
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
		String reqId = compute.getReqId();
		double[][] matrix = compute.getMatrix();
		l.l(me, "handleCompute, reqId: " + reqId + ", order: " + matrix.length + ", workers.size():" + workers.size());
		RequestInfo requestInfo = manager.getRequestInfo(reqId);

		requestInfo.setOriginalMatrix(matrix);

		if (workers.isEmpty()) {
			l.l(me, reqId + ", WORKERS.SIZE() = 0 !!!");
			requestInfo.setPercentageDone(100);
			requestInfo.setFinalDeterminant(-0.0);
			return;
		}
		gauss(requestInfo, reqId);
	}

	private void handleRowsResult(Messages.RowsResult rr) {
		String reqId = rr.getReqId();
		double[][] rows = rr.getRows();
		int rowNumber = rr.getRowNumber();

		if (!removeJob(getSender(), reqId, rowNumber)) {
			l.l(me, reqId + ", job non valido!");
			return;
		}

		RequestInfo requestInfo = manager.getRequestInfo(reqId);
		int nRowsDone = requestInfo.updateRowsDone(rows.length);
		//l.l(me, reqId + ", receive rows from " + rowNumber + " to " + (rowNumber + rows.length - 1) + "(" + rows.length
		//		+ " rows)");
		//l.l(me, reqId + ", nRowsDone: " + nRowsDone);

		requestInfo.updateCurrentMatrix(rows, rowNumber);
		int currentOrder = requestInfo.getCurrentOrder();

		if (nRowsDone == currentOrder - 1) {
//			if (currentOrder % 500 == 0) {
			l.l(me,
					reqId + ", received all rows, submatrix " + currentOrder + ". Duration: "
					+ ((System.currentTimeMillis() - requestInfo.getStartTime()) / (double) 1000) + " sec");
//			}

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
					//l.l(me, reqId + ", percentage computation duration: " + (System.currentTimeMillis() - startTime) + " ms");
					requestInfo.setPercentageDone(percentage);
				}
			} else { // matrix.length = 2
				l.l(me,
						reqId + ", received ALL rows, submatrix " + currentOrder + ". Duration: "
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
				WorkerInfo workerToRemove = workers.get(i);
				// index of the worker to reassign the jobs to
				int index = (i + 1) % workers.size();
				ArrayList<GaussJob> jobsToReassign = workerToRemove.getJobs();
				l.l(me, workerToRemove.getRemoteAddress() + " pending request: " + jobsToReassign.size());
				// nel caso di shutdown -> pending request = 0 !

				while (!jobsToReassign.isEmpty()) {
					// vado sempre a leggere il primo GaussJob della lista e poi lo rimuovo
					GaussJob job = jobsToReassign.get(0);
					String reqId = job.getReqId();
					int nRows = job.getNRows();
					int rowNumber = job.getRowNumber();
					RequestInfo requestInfo = manager.getRequestInfo(reqId);

					workerToRemove.removeJob(job);

					// caso particolare: non abbiamo altri worker a disposizione
					if (workers.size() < 2) {
						l.l(me, reqId + ", WORKERS.SIZE() = 0 !!!");
						requestInfo.setPercentageDone(100);
						requestInfo.setFinalDeterminant(-0.0);
						continue;
					}
					//l.l(me, "index: " + index);

					// Reassigning the jobs of the worker removed
					workers.get(index).addJob(reqId, nRows, rowNumber);
					workers.get(index).getActorRef()
							.tell(new Messages.Rows(reqId, requestInfo.getFirstRow(), requestInfo.getRows(nRows, rowNumber), rowNumber), getSelf());
					l.l(me, reqId + ", handleRemove, sent rows from " + rowNumber + " to " + (rowNumber + nRows - 1) + "("
							+ nRows + " rows) to " + workers.get(index).getRemoteAddress());
					l.l(me, reqId + ", call " + workers.get(index).getRemoteAddress()
							+ " to do the job of the removed worker " + workerToRemove.getRemoteAddress());
				}
				// shutdown
				workers.remove(i);
				l.l(me, workerToRemove.getRemoteAddress() + " removed, workers size: " + workers.size());
				return;
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
			//l.l(me, reqId + ", nRowsToSend: " + nRowsToSend);

			if (nRowsToSend > 0) {

				int maxNRowsPerMsg = MAX_ELEMS_PER_MSG / currentOrder; // MAX_ELEMS_PER_MSG deve essere maggiore dell'ordine massimo possibile

				int nMsgToSend = (int) (nRowsToSend / maxNRowsPerMsg);
				//l.l(me, reqId + ", nMsgToSend with " + (maxNRowsPerMsg*currentOrder) + " elements: " + nMsgToSend);

				int nRowsOfFirstMsg = nRowsToSend % maxNRowsPerMsg;
				//l.l(me, reqId + ", nRowsOfFirstMsg: " + nRowsOfFirstMsg);

				if (nRowsOfFirstMsg != 0) {
					workers.get(i).addJob(reqId, nRowsOfFirstMsg, nRowsSent + 1);
					workers.get(i).getActorRef()
							.tell(new Messages.Rows(reqId, requestInfo.getFirstRow(), requestInfo.getRows(nRowsOfFirstMsg, nRowsSent + 1), nRowsSent + 1), getSelf());
					l.l(me, reqId + ", sent rows from " + (nRowsSent + 1) + " to " + (nRowsSent + nRowsOfFirstMsg) + "("
							+ nRowsOfFirstMsg + " rows) to " + workers.get(i).getRemoteAddress());
					nRowsSent += nRowsOfFirstMsg;
				}

				for (int j = 0; j < nMsgToSend; j++) {
					workers.get(i).addJob(reqId, maxNRowsPerMsg, nRowsSent + 1);
					workers.get(i).getActorRef()
							.tell(new Messages.Rows(reqId, requestInfo.getFirstRow(), requestInfo.getRows(maxNRowsPerMsg, nRowsSent + 1), nRowsSent + 1), getSelf());
					l.l(me, reqId + ", sent rows from " + (nRowsSent + 1) + " to " + (nRowsSent + maxNRowsPerMsg) + "("
							+ maxNRowsPerMsg + " rows) to " + workers.get(i).getRemoteAddress());
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
	 * @return <code>true</code> if a zero column is 	 * detected, <code>false</code> otherwise
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
		sendRowsToWorkers(requestInfo, reqId);
		//sendOneRowPerMsg(requestInfo,reqId); // TODO eliminare
		return false;
	}

	private boolean removeJob(ActorRef worker, String reqId, int rowNumber) {
		for (int i = 0; i < workers.size(); i++) {
			if (workers.get(i).getRemoteAddress().equals(worker.path().toString())) {
				// works.size è generalmente uguale al numero di richieste in corso di quel master, tranne in casi di failure
				ArrayList<GaussJob> works = workers.get(i).getJobs();

				for (int j = 0; j < works.size(); j++) {
					if (works.get(j).getReqId().equals(reqId) && works.get(j).getRowNumber() == rowNumber) {
						workers.get(i).removeJob(works.get(j));
						return true;
					}
				}
			}
		}
		return false;
	}

	private void handleWorkerFailure(String workerNodeFailedAddress) {
		l.l(me, "handleWorkerFailure");
		String[] tokens;
		boolean first = true;
		int index = 0;

		for (int i = 0; i < workers.size(); i++) {
			tokens = workers.get(i).getRemoteAddress().split("/user");
			String workerINodeAddress = tokens[0];
			l.l(me, "workerINodeAddress: " + workerINodeAddress);

			if (workerINodeAddress.equals(workerNodeFailedAddress)) {
				WorkerInfo workerToRemove = workers.get(i);
				ArrayList<GaussJob> jobsToReassign = workerToRemove.getJobs();
				l.l(me, workerToRemove.getRemoteAddress() + " pending request: " + jobsToReassign.size());
				// nel caso di shutdown -> pending request = 0 !

				while (!jobsToReassign.isEmpty()) {
					if (first) {
						index = (i + 1) % workers.size();
						first = false;
					}
					// vado sempre a leggere il primo GaussJob della lista e poi lo rimuovo
					GaussJob job = jobsToReassign.get(0);
					String reqId = job.getReqId();
					int nRows = job.getNRows();
					int rowNumber = job.getRowNumber();
					RequestInfo requestInfo = manager.getRequestInfo(reqId);

					workerToRemove.removeJob(job);

					// caso particolare: non abbiamo altri worker a disposizione
					if (workers.size() < 2) {
						l.l(me, reqId + ", WORKERS.SIZE() = 0 !!!");
						requestInfo.setPercentageDone(100);
						requestInfo.setFinalDeterminant(-0.0);
						continue;
					}

					for (int k = 0; k < workers.size(); k++) {
						index = index % workers.size();
						tokens = workers.get(index).getRemoteAddress().split("/user");

						if (!tokens[0].equals(workerNodeFailedAddress)) {
							//l.l(me, "index: " + index);
							workers.get(index).addJob(reqId, nRows, rowNumber);
							workers.get(index).getActorRef()
									.tell(new Messages.Rows(reqId, requestInfo.getFirstRow(), requestInfo.getRows(nRows, rowNumber), rowNumber), getSelf());
							l.l(me, reqId + ", reassigning to " + workers.get(index).getRemoteAddress()
									+ " the job of the dead worker " + workerToRemove.getRemoteAddress());

							if (jobsToReassign.isEmpty() && index < i) {
								index = (index + 1) % workers.size();
							}
							break;
						}
						index = (index + 1) % workers.size();
					}
				}
				// shutdown
				workers.remove(i);
				l.l(me, workerToRemove.getRemoteAddress() + " removed, workers size: " + workers.size());
				i--;
			}
		}
	}
}