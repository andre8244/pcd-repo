import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;

public class Master extends java.rmi.server.UnicastRemoteObject implements IMaster {
	private static final int nWorkers = 5;
	private static final int jobSize = 500000;
	private Random rand;
	private int nWorkersDone;
	private long startTime;
	
	protected Master() throws RemoteException {
		super();
	}
	
	@Override
	public void sendResult(ArrayList<Double> result) throws RemoteException {
		log("received jobresult");
		nWorkersDone++;
		
		if (nWorkersDone == nWorkers) {
			log("Duration: " + ((System.currentTimeMillis() - startTime) / (double) 1000) + " sec");
		}
	}
	
	@Override
	public void start() throws RemoteException {
		log("started");
		startTime = System.currentTimeMillis();
		rand = new Random();
		nWorkersDone = 0;
		
		for (int i = 0; i < nWorkers; i++) {
			ArrayList<Double> job = new ArrayList<Double>(jobSize);
			
			for (int j = 0; j < jobSize; j++) {
				Double val = new Double(rand.nextInt(100) + rand.nextDouble());
				job.add(val);
			}
			
			try {
				IWorker worker = (IWorker) Naming.lookup("rmi://localhost:2000/worker-" + i);
				worker.submitJob(job);
			} catch (RemoteException ex) {
				ex.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (java.rmi.NotBoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void log(String msg) {
		System.out.println("[rmi-master] " + msg);
	}
}
