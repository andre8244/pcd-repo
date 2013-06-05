import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class Worker extends java.rmi.server.UnicastRemoteObject implements IWorker {
	
	protected Worker() throws RemoteException {
		super();
	}
	
	@Override
	public void submitJob(ArrayList<Double> job) throws RemoteException {
		log("received job");
		
		for (int i = 0; i < job.size(); i++) {
			Double val = Math.sqrt(job.get(i) * Math.sqrt(2));
			job.set(i, val);
		}
		
		try {
			IMaster master = (IMaster) Naming.lookup("rmi://localhost:2000/master");
			master.sendResult(job);
		} catch (RemoteException ex) {
			ex.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (java.rmi.NotBoundException e) {
			e.printStackTrace();
		}
	}
	
	private void log(String msg) {
		System.out.println("[worker] " + msg);
	}
}
