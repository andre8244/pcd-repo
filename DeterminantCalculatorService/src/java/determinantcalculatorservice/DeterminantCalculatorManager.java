/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package determinantcalculatorservice;

import Log.L;
import untyped.Messages.Compute;
import untyped.UntypedMaster;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import java.net.URL;
import java.util.HashMap;
import untyped.Messages.RegisterWorker;
import untyped.Messages.RemoveWorker;

/**
 *
 * @author Marco
 */
public class DeterminantCalculatorManager {

	private static DeterminantCalculatorManager instance;
	private int reqNumber;
	private ActorRef master;
	private HashMap<String, Double> results;
	private HashMap<String, Integer> done;
	private String me = "manager";

	private DeterminantCalculatorManager() {
		reqNumber = 0;
		ActorSystem system = ActorSystem.create("masterSystem", ConfigFactory.load().getConfig("masterSystem"));
		master = system.actorOf(new Props(UntypedMaster.class), "untyped-master");
		results = new HashMap<String, Double>();
		done = new HashMap<String, Integer>();
	}

	public synchronized static DeterminantCalculatorManager getInstance() {
		if (instance == null) {
			instance = new DeterminantCalculatorManager();
		}
		return instance;
	}

	public synchronized String computeDeterminant(int order, URL fileValues) {
		String reqId = "req" + reqNumber;
		reqNumber = reqNumber + 1;
		done.put(reqId, 0);
		master.tell(new Compute(order, fileValues, reqId, this));
		return reqId;
	}

	public synchronized int getPercentageDone(String reqId) {
		if (done.get(reqId) != null) {
			return done.get(reqId);
		} else {
			return -1;
		}
	}

	public synchronized double getResult(String reqId) {
		if (results.get(reqId) != null) {
			return results.get(reqId);
		} else {
			// TODO questa operazione potrebbe essere sospensiva, oppure restituire un Future
			return -1;
		}
	}

	public synchronized boolean registerWorker(String name, String ip, int port) {
		master.tell(new RegisterWorker(name, ip, port));
		return true;
	}

	public synchronized boolean removeWorker(String name, String ip, int port) {
		master.tell(new RemoveWorker(name, ip, port));
		return true;
	}

	public void setPercentageDone(String reqId, int percentageDone) {
		done.put(reqId, percentageDone);
	}

	public void setResult(String reqId, double result) {
		results.put(reqId, result);
	}

	// TODO DA ELIMINAREEEE!!
	public static void main(String args[]){
		DeterminantCalculatorManager manager = DeterminantCalculatorManager.getInstance();
		manager.computeDeterminant(1000, null);
	}
}
