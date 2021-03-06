package determinant_calculator_service;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	private ConcurrentHashMap<String, RequestManager> requests;
	private static Lock getInstanceLock = new ReentrantLock(true);
	private Lock computeDeterminantLock = new ReentrantLock(true);
	private int masterIndex = 0;
	
	private DeterminantCalculatorManager() {
		reqNumber = 0;
		masters = new ArrayList<ActorRef>();
		final DeterminantCalculatorManager manager = this; // passed to the masters
		ActorSystem system = ActorSystem.create("masterSystem", ConfigFactory.load().getConfig("masterSystem"));
		
		for (int i = 0; i < nProcessors; i++) {
			String masterId = "master-" + i;
			masters.add(system.actorOf(new Props(new UntypedActorFactory() {
				@Override
				public Actor create() {
					return new Master(manager);
				}
			}), masterId));
		}
		requests = new ConcurrentHashMap<String, RequestManager>();
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
	 * Create a new determinant computation request.
	 * 
	 * @param order the order of the matrix
	 * @param fileValues the URL to the file that stores the values of the matrix
	 * @return a {@code String} that identifies the request
	 */
	public String computeDeterminant(int order, String fileValues) {
		computeDeterminantLock.lock();
		
		try {
			String reqId = "req" + reqNumber;
			reqNumber = reqNumber + 1;
			RequestManager requestManager = new RequestManager();
			requests.put(reqId, requestManager);
			new MatrixReaderThread(order, fileValues, reqId, requestManager, masters.get(masterIndex)).start();
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
		RequestManager requestManager = requests.get(reqId);
		
		if (requestManager != null) {
			return requestManager.getPercentageDone();
		} else {
			System.err.println(reqId + ": invalid requestId");
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
		RequestManager requestManager = requests.get(reqId);
		
		if (requestManager != null) {
			// blocking operation
			return requestManager.getFinalDeterminant();
		} else {
			System.err.println(reqId + ": invalid requestId");
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
	 * Returns the <code>RequestManager</code> associated to the given <code>reqId</code>.
	 * 
	 * @param reqId the request of interest
	 * @return a <code>RequestManager</code>
	 */
	public RequestManager getRequestManager(String reqId) {
		return requests.get(reqId);
	}
}