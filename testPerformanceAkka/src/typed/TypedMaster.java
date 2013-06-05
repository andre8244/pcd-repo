package typed;

import java.util.ArrayList;
import java.util.Random;

import akka.actor.TypedActor;
import akka.actor.TypedProps;

public class TypedMaster implements ITypedMaster {
	private static final int jobSize = 500000;
	private static final int nWorkers = 5;
	private Random rand;
	private ArrayList<ITypedWorker> workers;
	private int nWorkersDone;
	private long startTime;
	
	@Override
	public void start() {
		startTime = System.currentTimeMillis();
		rand = new Random();
		workers = new ArrayList<ITypedWorker>();
		nWorkersDone = 0;
		
		// workers creation
		for (int i = 0; i < nWorkers; i++) {
			workers.add((ITypedWorker) TypedActor.get(TypedActor.context()).typedActorOf(
					new TypedProps<TypedWorker>(ITypedWorker.class, TypedWorker.class),
					"typed-worker-" + i));
		}
		
		for (int i = 0; i < nWorkers; i++) {
			ArrayList<Double> job = new ArrayList<Double>(jobSize);
			
			for (int j = 0; j < jobSize; j++) {
				Double val = new Double(rand.nextInt(100) + rand.nextDouble());
				job.add(val);
			}
			workers.get(i).submitJob(job, this);
		}
	}
	
	@Override
	public void sendResult(ArrayList<Double> result) {
		log("received jobresult");
		nWorkersDone++;
		
		if (nWorkersDone == nWorkers) {
			log("Duration: " + ((System.currentTimeMillis() - startTime) / (double) 1000) + " sec");
		}
	}
	
	private void log(String msg) {
		System.out.println("[typed-master] " + msg);
	}
}
