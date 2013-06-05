import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IMaster extends Remote {
	void sendResult(ArrayList<Double> result) throws RemoteException;
	
	void start() throws RemoteException;
}
