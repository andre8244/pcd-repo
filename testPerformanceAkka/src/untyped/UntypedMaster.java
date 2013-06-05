package untyped;

import java.util.ArrayList;
import java.util.Random;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import untyped.Messages.Job;
import untyped.Messages.JobResult;
import untyped.Messages.Start;

public class UntypedMaster extends UntypedActor {
	private static final int jobSize = 500000;
	private static final int nWorkers = 5;
	private Random rand;
	private ArrayList<ActorRef> workers;
	private int nWorkersDone;
	private long startTime;
	
	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof Start) {
			startTime = System.currentTimeMillis();
			start();
		} else if (msg instanceof JobResult) {
			log("received jobresult");
			nWorkersDone++;
			
			if (nWorkersDone == nWorkers) {
				log("Duration: " + ((System.currentTimeMillis() - startTime) / (double) 1000)
						+ " sec");
			}
		} else {
			unhandled(msg);
		}
	}
	
	private void start() {
		rand = new Random();
		workers = new ArrayList<ActorRef>();
		nWorkersDone = 0;
		
		// workers creation
		for (int i = 0; i < nWorkers; i++) {
			workers.add(getContext().actorOf(new Props(UntypedWorker.class), "worker-" + i));
		}
		
		for (int i = 0; i < nWorkers; i++) {
			final ArrayList<Double> jobList = new ArrayList<Double>(jobSize);
			
			for (int j = 0; j < jobSize; j++) {
				Double val = new Double(rand.nextInt(100) + rand.nextDouble());
				jobList.add(val);
			}
			workers.get(i).tell(new Job(jobList), getSelf());
		}
	}
	
	private void log(String msg) {
		System.out.println("[" + getSelf().path().name() + "] " + msg);
	}
}
