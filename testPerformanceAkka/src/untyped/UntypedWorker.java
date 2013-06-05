package untyped;

import java.util.ArrayList;

import akka.actor.UntypedActor;
import untyped.Messages.Job;
import untyped.Messages.JobResult;

public class UntypedWorker extends UntypedActor {
	
	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof Job) {
			Job job = (Job) msg;
			compute(job);
		} else {
			unhandled(msg);
		}
	}
	
	private void compute(Job job) {
		log("received job");
		final ArrayList<Double> list = job.getList();
		
		for (int i = 0; i < list.size(); i++) {
			Double val = Math.sqrt(list.get(i) * Math.sqrt(2));
			list.set(i, val);
		}
		final JobResult jr = new JobResult(list);
		getSender().tell(jr, getSelf());
	}
	
	private void log(String msg) {
		System.out.println("[" + getSelf().path().name() + "] " + msg);
	}
}
