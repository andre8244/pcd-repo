import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IWorker extends Remote {
	void submitJob(ArrayList<Double> job) throws RemoteException;
}
