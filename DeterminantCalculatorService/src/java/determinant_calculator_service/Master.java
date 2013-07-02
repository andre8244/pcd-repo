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
		double[][] matrix = MatrixReader.read(order, fileValues);
		
		if (matrix == null) {
			l.l(me, reqId + ", matrix Error !!!");
			requestInfo.setFinalDeterminant(-0.0);
			requestInfo.setPercentageDone(100);
			return;
		}
		requestInfo.setOriginalMatrix(matrix);
		
		if (workers.isEmpty()) {
			l.l(me, reqId + ", WORKERS.SIZE() = 0 !!!");
			requestInfo.setFinalDeterminant(-0.0);
			requestInfo.setPercentageDone(100);
			return;
		}
		gauss(reqId);
	}
	
	// TODO eliminare
	private void handleOneRowResult(Messages.OneRowResult orr) {
		String reqId = orr.getReqId();
		double[] row = orr.getRow();
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
		int nRowsDone = requestInfo.getRowsDone();
		nRowsDone++;
		requestInfo.setRowsDone(nRowsDone);
		
		double[][] matrix = requestInfo.getCurrentMatrix();
		matrix[rowNumber] = row;
		
		if (nRowsDone == matrix.length - 1) {
			if (matrix.length % 500 == 0) {
				l.l(me,
						reqId + ", received all rows, submatrix " + matrix.length + ". Duration: "
								+ ((System.currentTimeMillis() - requestInfo.getStartTime()) / (double) 1000) + " sec");
			}
			if (matrix.length > 2) {
				requestInfo.setRowsDone(0);
				requestInfo.setMatrix(subMatrix(reqId, matrix));
				
				if (workers.isEmpty()) {
					l.l(me, reqId + ", WORKERS.SIZE() = 0 !!!");
					requestInfo.setFinalDeterminant(-0.0);
					requestInfo.setPercentageDone(100);
					return;
				}
				boolean zeroColumn = gauss(reqId);
				
				if (!zeroColumn) {
					long startTime = System.currentTimeMillis();
					long totalWorkToDo = requestInfo.getTotalWorkToDo();
					long workToDo = 0;
					
					for (long i = requestInfo.getCurrentMatrix().length - 1; i > 0; i--) {
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
						reqId + ", received all rows, submatrix " + matrix.length + ". Duration: "
								+ ((System.currentTimeMillis() - requestInfo.getStartTime()) / (double) 1000) + " sec");
				double oldDeterminant = requestInfo.getTempDeterminant();
				double determinant = oldDeterminant * matrix[1][1];
				
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
				requestInfo.setPercentageDone(100);
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
		int nRowsDone = requestInfo.getRowsDone();
		nRowsDone = nRowsDone + rows.length;
		requestInfo.setRowsDone(nRowsDone);
		l.l(me, reqId + ", receive rows from " + rowNumber + " to " + (rowNumber + rows.length - 1) + "(" + rows.length
				+ " rows)");
		l.l(me, reqId + ", nRowsDone: " + nRowsDone);
		
		double[][] matrix = requestInfo.getCurrentMatrix();
		System.arraycopy(rows, 0, matrix, rowNumber, rows.length);
		
		if (nRowsDone == matrix.length - 1) {
			if (matrix.length % 500 == 0) {
				l.l(me,
						reqId + ", received all rows, submatrix " + matrix.length + ". Duration: "
								+ ((System.currentTimeMillis() - requestInfo.getStartTime()) / (double) 1000) + " sec");
			}
			
			if (matrix.length > 2) {
				requestInfo.setRowsDone(0);
				requestInfo.setMatrix(subMatrix(reqId, matrix));
				
				if (workers.isEmpty()) {
					l.l(me, reqId + ", WORKERS.SIZE() = 0 !!!");
					requestInfo.setFinalDeterminant(-0.0);
					requestInfo.setPercentageDone(100);
					return;
				}
				boolean zeroColumn = gauss(reqId);
				
				if (!zeroColumn) {
					long startTime = System.currentTimeMillis();
					long totalWorkToDo = requestInfo.getTotalWorkToDo();
					long workToDo = 0;
					
					for (long i = requestInfo.getCurrentMatrix().length - 1; i > 0; i--) {
						workToDo += i * (i + 1);
					}
					long workDone = totalWorkToDo - workToDo;
					int percentage = (int) (((double) workDone / totalWorkToDo) * 100);
					// TODO testare la durata del calcolo di percentuale con 10000
					l.l(me, "percentage computation duration: " + (System.currentTimeMillis() - startTime) + " ms");
					requestInfo.setPercentageDone(percentage);
				}
			} else { // matrix.length = 2
				l.l(me,
						reqId + ", received all rows, submatrix " + matrix.length + ". Duration: "
								+ ((System.currentTimeMillis() - requestInfo.getStartTime()) / (double) 1000) + " sec");
				double oldDeterminant = requestInfo.getTempDeterminant();
				double determinant = oldDeterminant * matrix[1][1];
				
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
				requestInfo.setPercentageDone(100);
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
				WorkerInfo worker = workers.remove(i);
				worker.getActorRef().tell(new Messages.RemoveWorkerAck(), getSelf());
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
	
	private void sendOneRowPerMsg(String reqId, double[][] matrix) {
		double[] firstRow = matrix[0];
		
		for (int i = 1; i < matrix.length; i++) {
			double[] row = matrix[i];
			// per utilizzare la OneRow e i Work passo una matrice con una sola riga
			double[][] rows = new double[1][matrix.length];
			rows[0] = row;
			workers.get(((i - 1) % workers.size())).addJob(reqId, rows, i);
			workers.get(((i - 1) % workers.size())).getActorRef()
					.tell(new Messages.OneRow(reqId, firstRow, row, i), getSelf());
			// if (i % 500 == 0) {
			// l.l(me, reqId + ", sent row " + i + " to " + workers.get(((i - 1) % workers.size())).getRemoteAddress());
			// }
		}
	}
	
	private void sendManyRowsPerMsg(String reqId, double[][] matrix) {
		double[] firstRow = matrix[0];
		double nRowsPerWorker = (double) (matrix.length - 1) / workers.size();
		// l.l(me, "nRowsPerWorker: " + nRowsPerWorker);
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
				workers.get(i).addJob(reqId, rows, nRowsSent + 1);
				workers.get(i).getActorRef()
						.tell(new Messages.ManyRows(reqId, firstRow, rows, nRowsSent + 1), getSelf());
				l.l(me, reqId + ", sent rows from " + (nRowsSent + 1) + " to " + (nRowsSent + nRowsToSend) + "("
						+ nRowsToSend + " rows) to " + workers.get(i).getRemoteAddress());
				nRowsSent += nRowsToSend;
			}
		}
	}
	
	/**
	 * Implements the Gaussian elimination algorithm. If a zero column is detected the computation can stop, the
	 * determinant of the matrix is zero.
	 * 
	 * @param reqId the request of interest
	 * @return <code>true</code> if a zero column is detected, <code>false</code> otherwise
	 */
	private boolean gauss(String reqId) {
		RequestInfo requestInfo = manager.getRequestInfo(reqId);
		double[][] matrix = requestInfo.getCurrentMatrix();
		boolean swapped = false;
		
		if (matrix[0][0] == 0) {
			swapped = swapFirtsRow(matrix);
			
			if (!swapped) {
				l.l(me, reqId + ", zero column! determinant = 0. Duration: "
						+ ((System.currentTimeMillis() - requestInfo.getStartTime()) / (double) 1000) + " sec");
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
		// TODO SCELTA TRA MANYROWS E ONEROW
		sendManyRowsPerMsg(reqId, matrix); // TODO provare a passare solo reqId
		// sendOneRowPerMsg(reqId, matrix);
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
					double[][] rows = works.get(j).getRows();
					int rowNumber = works.get(j).getRowNumber();
					RequestInfo requestInfo = manager.getRequestInfo(reqId);
					
					// caso particolare: non abbiamo altri worker a disposizione
					if (workers.size() < 2) {
						l.l(me, reqId + ", WORKERS.SIZE() = 0 !!!");
						requestInfo.setFinalDeterminant(-0.0);
						requestInfo.setPercentageDone(100);
						continue;
					}
					double[] firstRow = requestInfo.getCurrentMatrix()[0];
					
					for (int k = 0; k < workers.size(); k++) {
						index = index % workers.size();
						tokens = workers.get(index).getRemoteAddress().split("/user");
						
						if (!tokens[0].equals(workerSystem)) {
							l.l(me, "index: " + index);
							workers.get(index).addJob(reqId, rows, rowNumber);
							// TODO SCELTA TRA MANYROWS E ONEROW
							workers.get(index).getActorRef()
									.tell(new Messages.ManyRows(reqId, firstRow, rows, rowNumber), getSelf());
							// per utilizzare la Work e il OneRow converto la matrice in un semplice array
							// double[] row = rows[0];
							// workers.get(index).getActorRef().tell(new Messages.OneRow(reqId, firstRow, row,
							// rowNumber), getSelf());
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
