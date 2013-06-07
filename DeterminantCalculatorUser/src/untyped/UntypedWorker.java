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
		log("received job from [" + getSender().path().name() + "]");
		final ArrayList<Double> list = job.getList();
		final String reqId = job.getReqId();
		double result = 0;

		// per ora come job calcolo la media dei valori della lista
		for (int i = 0; i < list.size(); i++) {
			result = result + list.get(i);
		}
		result = result / list.size();

		final JobResult jr = new JobResult(result, reqId);
		getSender().tell(jr, getSelf());
	}

	private void log(String msg) {
		System.out.println("[" + getSelf().path().name() + "] " + msg);
	}
}
