import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Main {
	private static final int nWorkers = 5;
	
	public static void main(String[] args) {
		try {
			LocateRegistry.createRegistry(2000);
			IMaster master = new Master();
			String name = "rmi://localhost:2000/master";
			Naming.bind(name, master);
			log("master bound with the name: " + name);
			
			for (int i = 0; i < nWorkers; i++) {
				IWorker worker = new Worker();
				name = "rmi://localhost:2000/worker-" + i;
				Naming.bind(name, worker);
				log("worker bound with the name: " + name);
			}
			master.start();
		} catch (RemoteException ex) {
			ex.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		}
	}
	
	private static void log(String msg) {
		System.out.println("[main] " + msg);
	}
}
