package typed;

import java.util.ArrayList;

public class TypedWorker implements ITypedWorker {
	
	@Override
	public void submitJob(ArrayList<Double> job, ITypedMaster master) {
		log("received job");
		
		for (int i = 0; i < job.size(); i++) {
			Double val = Math.sqrt(job.get(i) * Math.sqrt(2));
			job.set(i, val);
		}
		master.sendResult(job);
	}
	
	private void log(String msg) {
		System.out.println("[typed-worker] " + msg);
	}
}
