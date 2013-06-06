package untyped;

import java.util.ArrayList;
import java.util.Random;

import akka.actor.UntypedActor;
import java.net.URL;
import untyped.Messages.Job;
import untyped.Messages.JobResult;
import untyped.Messages.Compute;
import untyped.Messages.RegisterWorker;
import untyped.Messages.RemoveWorker;

public class UntypedMaster extends UntypedActor {
	private static final int jobSize = 10000;//500000;
	private Random rand;
	private ArrayList<Worker> workers;
	private int nWorkersDone;
	private long startTime;
	
        public UntypedMaster(){
            workers = new ArrayList<Worker>();
        }
        
	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof Compute) {
                        Compute compute = (Compute)msg;
			startTime = System.currentTimeMillis();
			compute(compute);
		} else if (msg instanceof JobResult) {
                        JobResult jb = (JobResult)msg;
                        log("received jobresult from [" + getSender().path().name() + "]: "+jb.getList().get(0));
			nWorkersDone++;
			
			if (nWorkersDone == workers.size()) {
				log("Duration: " + ((System.currentTimeMillis() - startTime) / (double) 1000)
						+ " sec");
			}
		} else if (msg instanceof RegisterWorker) {
                        RegisterWorker worker = (RegisterWorker)msg;
                        // worker registration
                        String name = worker.getName();
                        String ip = worker.getIp();
                        int port = worker.getPort();
                        workers.add(new Worker(name,ip,port,getContext().actorFor("akka://"+name+"@"+ip+":"+port+"/user/"+name)));
                        System.out.println("Size: "+workers.size());
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
                        System.out.println("Size: "+workers.size());
                } else {
			unhandled(msg);
		}
	}
	
	private void compute(Compute compute) {
                int order = compute.getOrder();
                URL fileValue = compute.getFileValues();
                String reqId = compute.getReqId();
		rand = new Random();
		nWorkersDone = 0;
		for (int i = 0; i < workers.size(); i++) {
			final ArrayList<Double> jobList = new ArrayList<Double>(jobSize);
			
			for (int j = 0; j < jobSize; j++) {
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
