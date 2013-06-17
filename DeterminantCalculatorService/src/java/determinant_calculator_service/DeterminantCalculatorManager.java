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
 */
public class DeterminantCalculatorManager {

	private static DeterminantCalculatorManager instance;
	private int reqNumber;
	private ActorRef master;
	private HashMap<String, RequestInfo> requestsInfo;
	private String me = "manager";

	private DeterminantCalculatorManager() {
		reqNumber = 0;
		ActorSystem system = ActorSystem.create("masterSystem", ConfigFactory.load().getConfig("masterSystem"));
		master = system.actorOf(new Props(Master.class), "master");
		requestsInfo = new HashMap<String, RequestInfo>();
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
		requestsInfo.put(reqId, new RequestInfo());
		master.tell(new Messages.Compute(order, fileValues, reqId, this));
		return reqId;
	}

	public synchronized int getPercentageDone(String reqId) {
		RequestInfo requestInfo = requestsInfo.get(reqId);

		if (requestInfo != null){
			return requestInfo.getPercentageDone();
		} else {
			l.l(me, reqId + ": invalid requestId");
			return -1;
		}
	}

	public synchronized double getResult(String reqId) {
		// TODO assicurarsi che il "synchronized" non impedisca ad altri client di usare il metodo getResult()
		RequestInfo requestInfo = requestsInfo.get(reqId);

		if (requestInfo != null){
			 // blocking operation:
			return requestInfo.getFinalDeterminant();
		} else {
			l.l(me, reqId + ": invalid requestId");
			return -0.0;
		}
	}

	public synchronized boolean addWorkerNode(String remoteAddress) {
		master.tell(new Messages.AddWorkerNode(remoteAddress));
		// TODO ha senso che restituisca sempre true?
		return true;
	}

	public synchronized boolean removeWorkerNode(String remoteAddress) {
		master.tell(new Messages.RemoveWorkerNode(remoteAddress));
		return true;
	}

	public RequestInfo getRequestInfo(String reqId){
		return requestsInfo.get(reqId);
	}

}
