package determinant_calculator_service;

import log.l;
import java.util.ArrayList;

import akka.actor.UntypedActor;
import java.net.URL;
import java.util.HashMap;
import messages.Messages.Job;
import messages.Messages.JobResult;
import messages.Messages.Compute;
import messages.Messages.RegisterWorker;
import messages.Messages.RemoveWorker;

public class Master extends UntypedActor {

	private ArrayList<RemoteWorker> workers;
	private HashMap<String, Integer> done;
	private HashMap<String, Double> results;
	private long startTime;
	private DeterminantCalculatorManager manager;
	private String me;
        private HashMap<String, double[][]> matrix;

	public Master() {
		// TODO è meglio che i workers stiano dentro al manager?
		workers = new ArrayList<RemoteWorker>();
		done = new HashMap<String, Integer>();
		results = new HashMap<String, Double>();
		me = getSelf().path().name();
                matrix = new HashMap<String, double[][]>();
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof Compute) {
			Compute c = (Compute) msg;
			handleCompute(c);
		} else if (msg instanceof JobResult) {
			JobResult jr = (JobResult) msg;
			handleJobResult(jr);
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
		// utilizzo per ora order come dimensione della lista
		int order = compute.getOrder();
		URL fileValue = compute.getFileValues();
                String reqId = compute.getReqId();

		String path = System.getProperty("user.home") + System.getProperty("file.separator");
		String fileName = path + "matrix.txt";

		MatrixUtil.genAndWriteToFile(order, 20, fileName);

                matrix.put(reqId,MatrixUtil.fromFileToList(order, fileName));
                
                l.l(me, "Matrix length: "+matrix.get(reqId).length);
                
                double media = 0;
                for (int i=0; i<matrix.get(reqId).length; i++){
                    for (int j=0; j<(matrix.get(reqId))[i].length; j++){
                        media = media + (matrix.get(reqId))[i][j];
                    }
                }
                media = media /(matrix.get(reqId).length*matrix.get(reqId).length);
                l.l(me, "Matrix media: "+media);

                done.put(reqId, 0);
		results.put(reqId, 0.0);

		if (workers.isEmpty()){
			l.l(me, "\nWORKERS.SIZE() = 0 !!!!\n");
                        return;
		}

		if (workers.size() > order){
			l.l(me, "\nWORKERS.SIZE() > ORDER !!!!\n");
			return;
		}

		for (int i = 0; i < matrix.get(reqId).length; i++) {
			double[] row = matrix.get(reqId)[i];
			workers.get((i%workers.size())).getActorRef().tell(new Job(row, reqId), getSelf());
			if (i % 500 == 0) { 
                                l.l(me, "sent row " + i + " to worker" + (i%workers.size()));
                        }
		}
	}

	private void handleJobResult(JobResult jr) {
		double result = jr.getResult();
		String reqId = jr.getReqId();
		//log("received jobresult from [" + getSender().path().name() + "]: " + result);
		int nRowsDone = done.get(reqId);
		nRowsDone++;
		done.put(reqId, nRowsDone);
                
                if (nRowsDone % 500 == 0) {
                        l.l(me, "nRowsDone: " + nRowsDone);
		}

		int percentageDone = nRowsDone * 100 / matrix.get(reqId).length;
		manager.setPercentageDone(reqId, percentageDone);
		double precRes = results.get(reqId);
		results.put(reqId, precRes + result);

		if (nRowsDone == matrix.get(reqId).length) {
			l.l(me, "Duration: " + ((System.currentTimeMillis() - startTime) / (double) 1000) + " sec");
			manager.setResult(reqId, results.get(reqId) / matrix.get(reqId).length);
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
}
