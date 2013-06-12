/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package determinant_calculator_service;

import log.l;
import messages.Messages;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import java.util.HashMap;

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
		master = system.actorOf(new Props(Master.class), "master");
		results = new HashMap<String, Double>();
		done = new HashMap<String, Integer>();
	}

	public synchronized static DeterminantCalculatorManager getInstance() {
		if (instance == null) {
			instance = new DeterminantCalculatorManager();
		}
		return instance;
	}

	public synchronized String computeDeterminant(int order, String fileValues) {
		String reqId = "req" + reqNumber;
		reqNumber = reqNumber + 1;
		done.put(reqId, 0);
		master.tell(new Messages.Compute(order, fileValues, reqId, this));
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

	public synchronized boolean registerWorker(String remoteAddress) {
		master.tell(new Messages.RegisterWorker(remoteAddress));
		// TODO ha senso che restituisca sempre true?
		return true;
	}

	public synchronized boolean removeWorker(String remoteAddress) {
		master.tell(new Messages.RemoveWorker(remoteAddress));
		return true;
	}

	public void setPercentageDone(String reqId, int percentageDone) {
		done.put(reqId, percentageDone);
	}

	public void setResult(String reqId, double result) {
		results.put(reqId, result);
	}

}
