package determinant_calculator_service;

import log.l;
import messages.Messages;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DeterminantCalculatorManager {

	private static DeterminantCalculatorManager instance;
	private int reqNumber;
	private final int nProcessors = Runtime.getRuntime().availableProcessors();
	private ArrayList<ActorRef> masters;
	private HashMap<String, RequestInfo> requestsInfo;
	private static Lock lock = new ReentrantLock(true);
	private String me = "manager";
	private int index=0;

	private DeterminantCalculatorManager() {
		reqNumber = 0;
		masters = new ArrayList<ActorRef>();
		ActorSystem system = ActorSystem.create("masterSystem", ConfigFactory.load().getConfig("masterSystem"));
		//master = system.actorOf(new Props(Master.class), "master");
		for (int i = 0; i < nProcessors; i++) {
			String masterId = "master-" +i;
			masters.add(system.actorOf(new Props(Master.class), masterId));
		}
		requestsInfo = new HashMap<String, RequestInfo>();
	}

	public static DeterminantCalculatorManager getInstance() {
		lock.lock();

		try {
			if (instance == null) {
				instance = new DeterminantCalculatorManager();
			}
			return instance;
		} finally {
			lock.unlock();
		}
	}

	public String computeDeterminant(int order, String fileValues) {
		lock.lock();

		try {
			String reqId = "req" + reqNumber;
			reqNumber = reqNumber + 1;
			requestsInfo.put(reqId, new RequestInfo());
			masters.get(index).tell(new Messages.Compute(order, fileValues, reqId, this));
			index=(index+1)%masters.size();
			return reqId;
		} finally {
			lock.unlock();
		}
	}

	public int getPercentageDone(String reqId) {
		lock.lock();

		try {
			RequestInfo requestInfo = requestsInfo.get(reqId);

			if (requestInfo != null){
				return requestInfo.getPercentageDone();
			} else {
				l.l(me, reqId + ": invalid requestId");
				return -1;
			}
		} finally {
			lock.unlock();
		}
	}

	public double getResult(String reqId) {
		lock.lock();
		RequestInfo requestInfo = requestsInfo.get(reqId);
		lock.unlock();

		if (requestInfo != null){
			 // blocking operation:
			return requestInfo.getFinalDeterminant();
		} else {
			l.l(me, reqId + ": invalid requestId");
			return -0.0;
		}
	}

	public boolean addWorkerNode(String remoteAddress) {
		lock.lock();

		try {
			for (int i = 0; i < masters.size(); i++) {
				masters.get(i).tell(new Messages.AddWorkerNode(remoteAddress));
			}
			// TODO ha senso che restituisca sempre true?
			return true;
		} finally {
			lock.unlock();
		}
	}

	public boolean removeWorkerNode(String remoteAddress) {
		lock.lock();

		try {
			for (int i = 0; i < masters.size(); i++) {
				masters.get(i).tell(new Messages.RemoveWorkerNode(remoteAddress));
			}
			return true;
		} finally {
			lock.unlock();
		}
	}

	public RequestInfo getRequestInfo(String reqId){
		return requestsInfo.get(reqId);
	}

}