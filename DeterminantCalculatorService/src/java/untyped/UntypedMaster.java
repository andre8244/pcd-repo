package untyped;

import Log.L;
import java.util.ArrayList;
import java.util.Random;

import akka.actor.UntypedActor;
import determinantcalculatorservice.DeterminantCalculatorManager;
import determinantcalculatorservice.MatrixUtil;
import java.net.URL;
import java.util.HashMap;
import untyped.Messages.Job;
import untyped.Messages.JobResult;
import untyped.Messages.Compute;
import untyped.Messages.RegisterWorker;
import untyped.Messages.RemoveWorker;

public class UntypedMaster extends UntypedActor {

	private Random rand;
	private ArrayList<Worker> workers;
	private HashMap<String, Integer> done;
	private HashMap<String, Double> results;
	private long startTime;
	private DeterminantCalculatorManager manager;
	private String me;

	public UntypedMaster() {
		workers = new ArrayList<Worker>();
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
		if (manager == null) {
			manager = compute.getManager();
		}
		startTime = System.currentTimeMillis();
		// utilizzo per ora order come dimensione della lista
		int order = compute.getOrder();
		URL fileValue = compute.getFileValues();

//		String path = System.getProperty("user.home") + System.getProperty("file.separator");
//		String fileName = path + "matrix.txt";
//		MatrixUtil.genAndWriteToFile(3, 20, fileName);
//		MatrixUtil.fromFileToArrayList(fileName);

		String reqId = compute.getReqId();
		rand = new Random();
		done.put(reqId, 0);
		results.put(reqId, 0.0);

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
		int percentageDone = nWorkersDone * 100 / workers.size();
		manager.setPercentageDone(reqId, percentageDone);
		double precRes = results.get(reqId);
		results.put(reqId, precRes + result);

		if (nWorkersDone == workers.size()) {
			L.log(me, "Duration: " + ((System.currentTimeMillis() - startTime) / (double) 1000) + " sec");
			manager.setResult(reqId, results.get(reqId) / workers.size());
		}
	}

	private void handleRegisterWorker(RegisterWorker rw) {
		String name = rw.getName();
		String ip = rw.getIp();
		int port = rw.getPort();
		workers.add(new Worker(name, ip, port, getContext()
				.actorFor("akka://" + name + "@" + ip + ":" + port + "/user/" + name)));
		L.log(me, "workers size: " + workers.size());
	}

	private void handleRemoveWorker(RemoveWorker rw) {
		String name = rw.getName();
		String ip = rw.getIp();
		int port = rw.getPort();

		// TODO si potrebbe usare una hashmap per rendere la ricerca più performante
		for (int i = 0; i < workers.size(); i++) {
			if (workers.get(i).getName().equals(name) && workers.get(i).getIp().equals(ip)
					&& workers.get(i).getPort() == port) {
				workers.remove(i);
			}
		}
		L.log(me, "workers size: " + workers.size());
	}
}
