package untyped;

import akka.actor.ActorRef;
import java.util.ArrayList;
import java.util.Random;

import akka.actor.UntypedActor;
import java.net.URL;
import java.util.HashMap;
import untyped.Messages.Job;
import untyped.Messages.JobResult;
import untyped.Messages.Compute;
import untyped.Messages.PercentageDone;
import untyped.Messages.RegisterWorker;
import untyped.Messages.RemoveWorker;
import untyped.Messages.Result;

public class UntypedMaster extends UntypedActor {
	private Random rand;
	private ArrayList<Worker> workers;
        private HashMap<String,Integer> done;
        private HashMap<String,Double> results;
	private long startTime;
        private ActorRef manager;
	
        public UntypedMaster(){
            workers = new ArrayList<Worker>();
            done = new HashMap<String,Integer>();
            results = new HashMap<String,Double>();
        }
        
	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof Compute) {
                        Compute compute = (Compute)msg;
                        if (manager==null){
                            manager = getSender();
                        }
			startTime = System.currentTimeMillis();
			compute(compute);
		} else if (msg instanceof JobResult) {
                        JobResult jb = (JobResult)msg;
                        double result = jb.getResult();
                        String reqId = jb.getReqId();
                        log("received jobresult from [" + getSender().path().name() + "]: "+result);
			int nWorkersDone = done.get(reqId);
			nWorkersDone++;
                        done.put(reqId, nWorkersDone);
                        final PercentageDone pd = new PercentageDone(reqId,nWorkersDone*100/workers.size());
                        manager.tell(pd, getSelf());
                        double precRes = results.get(reqId);
                        results.put(reqId, precRes+result);
			if (nWorkersDone == workers.size()) {
				log("Duration: " + ((System.currentTimeMillis() - startTime) / (double) 1000)
						+ " sec");
                                final Result res = new Result(reqId,results.get(reqId)/workers.size());
                                manager.tell(res, getSelf());
			}
		} else if (msg instanceof RegisterWorker) {
                        RegisterWorker worker = (RegisterWorker)msg;
                        // worker registration
                        String name = worker.getName();
                        String ip = worker.getIp();
                        int port = worker.getPort();
                        workers.add(new Worker(name,ip,port,getContext().actorFor("akka://"+name+"@"+ip+":"+port+"/user/"+name)));
                        System.out.println("Workers size: "+workers.size());
                } else if (msg instanceof RemoveWorker) {
                        RemoveWorker worker = (RemoveWorker)msg;
                        String name = worker.getName();
                        String ip = worker.getIp();
                        int port = worker.getPort();
                        for (int i=0; i<workers.size(); i++){
                            if (workers.get(i).getName().equals(name)&& workers.get(i).getIp().equals(ip) && workers.get(i).getPort()==port){
                                workers.remove(i);
                            }
                        }
                        System.out.println("Worker size: "+workers.size());
                } else {
			unhandled(msg);
		}
	}
	
	private void compute(Compute compute) {
                // utilizzo per ora order come dimensione della lista
                int order = compute.getOrder();
                URL fileValue = compute.getFileValues();
                String reqId = compute.getReqId();
		rand = new Random();
		done.put(reqId, 0);
                results.put(reqId, 0.0);
		for (int i = 0; i < workers.size(); i++) {
			final ArrayList<Double> jobList = new ArrayList<Double>(order);
			
			for (int j = 0; j < order; j++) {
				Double val = new Double(rand.nextInt(100) + rand.nextDouble());
				jobList.add(val);
			}
			workers.get(i).getActorRef().tell(new Job(jobList,reqId), getSelf());
		}
	}
	
	private void log(String msg) {
		System.out.println("[" + getSelf().path().name() + "] " + msg);
	}
}
