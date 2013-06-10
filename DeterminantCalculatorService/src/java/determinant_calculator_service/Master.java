package determinant_calculator_service;

import log.l;
import java.util.ArrayList;
import java.util.Random;

import akka.actor.UntypedActor;
import java.net.URL;
import java.util.HashMap;
import messages.Messages.Job;
import messages.Messages.JobResult;
import messages.Messages.Compute;
import messages.Messages.RegisterWorker;
import messages.Messages.RemoveWorker;

public class Master extends UntypedActor {

	private Random rand;
	private ArrayList<RemoteWorker> workers;
	private HashMap<String, Integer> done;
	private HashMap<String, Double> results;
	private long startTime;
	private DeterminantCalculatorManager manager;
	private String me;

	public Master() {
		// TODO è meglio che i workers stiano dentro al manager?
		workers = new ArrayList<RemoteWorker>();
		done = new HashMap<String, Integer>();
		results = new HashMap<String, Double>();
		me = getSelf().path().name();
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

		String path = System.getProperty("user.home") + System.getProperty("file.separator");
		String fileName = path + "matrix.txt";
		MatrixUtil.genAndWriteToFile(3000, 20, fileName);
		MatrixUtil.fromFileToArrayList(fileName);
		MatrixUtil.fromFileToHashMap(fileName);

		String reqId = compute.getReqId();
		rand = new Random();
		done.put(reqId, 0);
		results.put(reqId, 0.0);

		if (workers.size() == 0){
			l.l(me, "\nWORKERS.SIZE() = 0 !!!!\n");
		}

		for (int i = 0; i < workers.size(); i++) {
			final ArrayList<Double> jobList = new ArrayList<Double>(order);

			for (int j = 0; j < order; j++) {
				Double val = new Double(rand.nextInt(1000) + rand.nextDouble());
				jobList.add(val);
			}
			workers.get(i).getActorRef().tell(new Job(jobList, reqId), getSelf());
		}
	}

	private void handleJobResult(JobResult jr) {
		double result = jr.getResult();
		String reqId = jr.getReqId();
		//log("received jobresult from [" + getSender().path().name() + "]: " + result);
		int nWorkersDone = done.get(reqId);
		nWorkersDone++;
		done.put(reqId, nWorkersDone);

		l.l(me, "nWorkersDone: " + nWorkersDone);

		int percentageDone = nWorkersDone * 100 / workers.size();
		manager.setPercentageDone(reqId, percentageDone);
		double precRes = results.get(reqId);
		results.put(reqId, precRes + result);

		if (nWorkersDone == workers.size()) {
			l.l(me, "Duration: " + ((System.currentTimeMillis() - startTime) / (double) 1000) + " sec");
			manager.setResult(reqId, results.get(reqId) / workers.size());
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
