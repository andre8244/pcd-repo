package determinant_calculator_service;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import log.l;
import messages.Messages;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActorFactory;

import com.typesafe.config.ConfigFactory;

/**
 * The manager of the requests sent to the web service.
 *
 */
public class DeterminantCalculatorManager {

	private static DeterminantCalculatorManager instance;
	private int reqNumber;
	private final int nProcessors = Runtime.getRuntime().availableProcessors();
	private ArrayList<ActorRef> masters;
	private ConcurrentHashMap<String, RequestInfo> requestsInfo;
	private static Lock getInstanceLock = new ReentrantLock(true);
	private static Lock computeDeterminantLock = new ReentrantLock(true);
	private String me = "manager";
	private int masterIndex = 0;

	private DeterminantCalculatorManager() {
		reqNumber = 0;
		masters = new ArrayList<ActorRef>();
		final DeterminantCalculatorManager manager = this;
		ActorSystem system = ActorSystem.create("masterSystem", ConfigFactory.load().getConfig("masterSystem"));

		for (int i = 0; i < nProcessors; i++) {
			String masterId = "master-" + i;
			// masters.add(system.actorOf(new Props(Master.class), masterId));
			masters.add(system.actorOf(new Props(new UntypedActorFactory() {
				@Override
				public Actor create() {
					return new Master(manager);
				}
			}), masterId));
		}
		requestsInfo = new ConcurrentHashMap<String, RequestInfo>();
	}

	/**
	 * Returns an istance of the manager, using the singleton pattern.
	 *
	 * @return an istance of the manager.
	 */
	public static DeterminantCalculatorManager getInstance() {
		getInstanceLock.lock();

		try {
			if (instance == null) {
				instance = new DeterminantCalculatorManager();
			}
			return instance;
		} finally {
			getInstanceLock.unlock();
		}
	}

	/**
	 * Create a new determinant computation request and forwards it to a master actor.
	 *
	 * @param order the order of the matrix
	 * @param fileValues the URL to the file that stores the values of the matrix
	 * @return a <code>String</code> that identifies the request
	 */
	public String computeDeterminant(int order, String fileValues) {
		computeDeterminantLock.lock();

		try {
			String reqId = "req" + reqNumber;
			reqNumber = reqNumber + 1;
			RequestInfo requestInfo = new RequestInfo();
			requestsInfo.put(reqId, requestInfo);
			new MatrixReaderThread(order, fileValues, reqId, requestInfo, masters.get(masterIndex)).start();
			masterIndex = (masterIndex + 1) % masters.size();
			return reqId;
		} finally {
			computeDeterminantLock.unlock();
		}
	}

	/**
	 * Returns an estimation of percentage of a previously requested computation.
	 *
	 * @param reqId the request of interest
	 * @return an estimation of percentage of a previously requested computation.
	 */
	public int getPercentageDone(String reqId) {
		RequestInfo requestInfo = requestsInfo.get(reqId);

		if (requestInfo != null) {
			return requestInfo.getPercentageDone();
		} else {
			l.l(me, reqId + ": invalid requestId");
			return -1;
		}
	}

	/**
	 * Returns the determinant computed
	 *
	 * @param reqId the request of interest
	 * @return the determinant computed
	 */
	public double getResult(String reqId) {
		RequestInfo requestInfo = requestsInfo.get(reqId);

		if (requestInfo != null) {
			// blocking operation
			return requestInfo.getFinalDeterminant();
		} else {
			l.l(me, reqId + ": invalid requestId");
			return -0.0;
		}
	}

	/**
	 * Adds a worker actor to the system.
	 *
	 * @param remoteAddress the remote path of the worker actor
	 */
	public void addWorker(String remoteAddress) {
		for (int i = 0; i < masters.size(); i++) {
			masters.get(i).tell(new Messages.AddWorker(remoteAddress));
		}
	}

	/**
	 * Removes a worker actor from the system.
	 *
	 * @param remoteAddress the remote path of the worker actor
	 */
	public void removeWorker(String remoteAddress) {
		for (int i = 0; i < masters.size(); i++) {
			masters.get(i).tell(new Messages.RemoveWorker(remoteAddress));
		}
	}

	/**
	 * Returns the <code>RequestInfo</code> associated to the given <code>reqId</code>.
	 *
	 * @param reqId the request of interest
	 * @return a <code>RequestInfo</code>
	 */
	public RequestInfo getRequestInfo(String reqId) {
		return requestsInfo.get(reqId);
	}
}