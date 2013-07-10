package determinant_calculator_service;

import java.util.ArrayList;

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
	private static final int MAX_ELEMS_PER_MSG = 10000000; // default: 10 millions

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
			System.out.println("Remote Client Write Failed: " + rcwf.getRemoteAddress().toString());
			handleWorkerFailure(rcwf.getRemoteAddress().toString());
		} else if (msg instanceof RemoteClientShutdown) {
			RemoteClientShutdown rcs = (RemoteClientShutdown) msg;
			System.out.println("Remote Client Shutdown: " + rcs.getRemoteAddress().toString());
			handleWorkerFailure(rcs.getRemoteAddress().toString());
		} else {
			unhandled(msg);
		}
	}

	private void handleCompute(Messages.Compute compute) {
		String reqId = compute.getReqId();
		double[][] matrix = compute.getMatrix();
		System.out.println("Received new request: "+ reqId + " for the matrix of order: "+ matrix.length);
		RequestManager requestManager = manager.getRequestManager(reqId);

		requestManager.setOriginalMatrix(matrix);

		if (workers.isEmpty()) {
			System.err.println("No worker available");
			requestManager.setPercentageDone(100);
			requestManager.setFinalDeterminant(-0.0);
			return;
		}
		gauss(requestManager, reqId);
	}

	private void handleRowsResult(Messages.RowsResult rr) {
		String reqId = rr.getReqId();
		double[][] rows = rr.getRows();
		int rowNumber = rr.getRowNumber();

		if (!removeJob(getSender(), reqId, rowNumber)) {
			// message already reassigned
			return;
		}

		RequestManager requestManager = manager.getRequestManager(reqId);
		int nRowsDone = requestManager.updateRowsDone(rows.length);
		//System.out.println(reqId + ", receive rows from " + rowNumber + " to " + (rowNumber + rows.length - 1) + 
		//						" (" + rows.length + " rows)");

		requestManager.updateCurrentMatrix(rows, rowNumber);
		int currentOrder = requestManager.getCurrentOrder();

		if (nRowsDone == currentOrder - 1) {
			//System.out.println(reqId + ", received all rows, submatrix " + currentOrder + ". Duration: "
			//		+ ((System.currentTimeMillis() - requestManager.getStartTime()) / (double) 1000) + " sec");

			if (currentOrder > 2) {
				requestManager.subMatrix();

				if (workers.isEmpty()) {
					System.err.println("No worker available");
					requestManager.setPercentageDone(100);
					requestManager.setFinalDeterminant(-0.0);
					return;
				}
				boolean zeroColumn = gauss(requestManager, reqId);

				if (!zeroColumn) {
					long totalWorkToDo = requestManager.getTotalWorkToDo();
					long workToDo = 0;

					for (long i = requestManager.getCurrentOrder() - 1; i > 0; i--) {
						workToDo += i * (i + 1);
					}
					long workDone = totalWorkToDo - workToDo;
					int percentage = (int) (((double) workDone / totalWorkToDo) * 100);
					requestManager.setPercentageDone(percentage);
				}
			} else { // matrix.length = 2
				System.out.println(reqId+ ", determinant computed. Duration: "
						+ ((System.currentTimeMillis() - requestManager.getStartTime()) / (double) 1000) + " sec");
				double determinant = requestManager.updateLastTempDeterminant();
				requestManager.setPercentageDone(100);

				if (determinant == 0) { // needed when determinant = -0.0
					requestManager.setFinalDeterminant(0);
				} else {
					if (!requestManager.getChangeSign()) {
						requestManager.setFinalDeterminant(determinant);
					} else {
						requestManager.setFinalDeterminant(-determinant);
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
		System.out.println("["+me+"] " +worker.getRemoteAddress() + " added. Number of workers: " + workers.size());
	}

	private void handleRemoveWorker(Messages.RemoveWorker rw) {
		String remoteAddress = rw.getRemoteAddress();

		for (int i = 0; i < workers.size(); i++) {
			if (workers.get(i).getRemoteAddress().equals(remoteAddress)) {
				WorkerInfo workerToRemove = workers.get(i);
				// index of the worker to reassign the jobs to
				int index = (i + 1) % workers.size();
				ArrayList<GaussJob> jobsToReassign = workerToRemove.getJobs();
				
				while (!jobsToReassign.isEmpty()) {
					// vado sempre a leggere il primo GaussJob della lista e poi lo rimuovo
					GaussJob job = jobsToReassign.get(0);
					String reqId = job.getReqId();
					int nRows = job.getNRows();
					int rowNumber = job.getRowNumber();
					RequestManager requestManager = manager.getRequestManager(reqId);

					workerToRemove.removeJob(job);

					if (workers.size() < 2) {
						System.err.println("No worker available");
						requestManager.setPercentageDone(100);
						requestManager.setFinalDeterminant(-0.0);
						continue;
					}
					// Reassigning the jobs of the worker to remove
					workers.get(index).addJob(reqId, nRows, rowNumber);
					workers.get(index).getActorRef()
							.tell(new Messages.Rows(reqId, requestManager.getFirstRow(), requestManager.getRows(nRows, rowNumber), rowNumber), getSelf());
					System.out.println(reqId + ", reassigned rows from " + rowNumber + " to " + (rowNumber + nRows - 1) + " ("
										+ nRows + " rows) to " + workers.get(index).getRemoteAddress());
				}
				workers.remove(i);
				//System.out.println(workerToRemove.getRemoteAddress() + " removed. Number of workers: " + workers.size());
				return;
			}
		}
	}

	private void sendRowsToWorkers(RequestManager requestManager, String reqId) {
		int currentOrder = requestManager.getCurrentOrder();
		double nRowsPerWorker = (double) (currentOrder - 1) / workers.size();
		int nRowsSent = 0;
		int nRowsToSend;

		for (int i = 0; i < (workers.size()); i++) {
			// number of rows to send to worker i:
			nRowsToSend = (int) (Math.round((i + 1) * nRowsPerWorker) - nRowsSent);
			
			if (nRowsToSend > 0) {
				int maxNRowsPerMsg = MAX_ELEMS_PER_MSG / currentOrder; // MAX_ELEMS_PER_MSG deve essere maggiore dell'ordine massimo possibile
				int nMsgToSend = (int) (nRowsToSend / maxNRowsPerMsg);
				int nRowsOfFirstMsg = nRowsToSend % maxNRowsPerMsg;
				
				if (nRowsOfFirstMsg != 0) {
					workers.get(i).addJob(reqId, nRowsOfFirstMsg, nRowsSent + 1);
					workers.get(i).getActorRef()
							.tell(new Messages.Rows(reqId, requestManager.getFirstRow(), requestManager.getRows(nRowsOfFirstMsg, nRowsSent + 1), nRowsSent + 1), getSelf());
					//System.out.println(reqId + ", sent rows from " + (nRowsSent + 1) + " to " + (nRowsSent + nRowsOfFirstMsg) + " ("
					//		+ nRowsOfFirstMsg + " rows) to " + workers.get(i).getRemoteAddress());
					nRowsSent += nRowsOfFirstMsg;
				}

				for (int j = 0; j < nMsgToSend; j++) {
					workers.get(i).addJob(reqId, maxNRowsPerMsg, nRowsSent + 1);
					workers.get(i).getActorRef()
							.tell(new Messages.Rows(reqId, requestManager.getFirstRow(), requestManager.getRows(maxNRowsPerMsg, nRowsSent + 1), nRowsSent + 1), getSelf());
					//System.out.println(reqId + ", sent rows from " + (nRowsSent + 1) + " to " + (nRowsSent + maxNRowsPerMsg) + " ("
					//		+ maxNRowsPerMsg + " rows) to " + workers.get(i).getRemoteAddress());
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
	private boolean gauss(RequestManager requestManager, String reqId) { // TODO aggiungere requestManager alla doc
		boolean swapped = false;
		if (requestManager.getFirstElement() == 0) {
			swapped = requestManager.swapFirtsRow();

			if (!swapped) {
				System.out.println(reqId + ", zero column found. Determinant = 0. Duration: "
						+ ((System.currentTimeMillis() - requestManager.getStartTime()) / (double) 1000) + " sec");
				requestManager.setPercentageDone(100);
				requestManager.setFinalDeterminant(0);
				return true;
			}
		}
		if (swapped) {
			requestManager.setChangeSign();
		}
		requestManager.updateTempDeterminant();
		sendRowsToWorkers(requestManager, reqId);
		return false;
	}

	private boolean removeJob(ActorRef worker, String reqId, int rowNumber) {
		for (int i = 0; i < workers.size(); i++) {
			if (workers.get(i).getRemoteAddress().equals(worker.path().toString())) {
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
		boolean first = true;
		int index = 0;

		for (int i = 0; i < workers.size(); i++) {
			String workerINodeAddress = workers.get(i).getRemoteAddress().split("/user")[0];

			if (workerINodeAddress.equals(workerNodeFailedAddress)) {
				WorkerInfo workerToRemove = workers.get(i);
				ArrayList<GaussJob> jobsToReassign = workerToRemove.getJobs();

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
					RequestManager requestManager = manager.getRequestManager(reqId);

					workerToRemove.removeJob(job);

					if (workers.size() < 2) {
						System.err.println("No worker available");
						requestManager.setPercentageDone(100);
						requestManager.setFinalDeterminant(-0.0);
						continue;
					}

					for (int k = 0; k < workers.size(); k++) {
						index = index % workers.size();
						String workerIndexNodeAddress = workers.get(index).getRemoteAddress().split("/user")[0];

						if (!workerIndexNodeAddress.equals(workerNodeFailedAddress)) {
							workers.get(index).addJob(reqId, nRows, rowNumber);
							workers.get(index).getActorRef()
									.tell(new Messages.Rows(reqId, requestManager.getFirstRow(), requestManager.getRows(nRows, rowNumber), rowNumber), getSelf());
							System.out.println(reqId + ", reassigned rows from " + rowNumber + " to " + (rowNumber + nRows - 1) + " ("
										+ nRows + " rows) to " + workers.get(index).getRemoteAddress());

							if (jobsToReassign.isEmpty() && index < i) {
								index = (index + 1) % workers.size();
							}
							break;
						}
						index = (index + 1) % workers.size();
					}
				}
				workers.remove(i);
				i--; // decremento indice per non saltare nessun elemento
			}
		}
	}
}