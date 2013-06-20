package determinant_calculator_worker;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import java.util.ArrayList;

/**
 * This class deploys one (or more) workers on a network node.
 *
 */
public class WorkerNodeApp {

	private static final int nWorkersToDeploy = Runtime.getRuntime().availableProcessors();
	private static ActorSystem system;
	private static ArrayList<ActorRef> workers;

	// TODO in futuro questo metodo dovrebbe essere eliminato, s
	public static void installWorkerNode(int nWorkers) {
		long id = System.currentTimeMillis();
		workers = new ArrayList<>();
		system = ActorSystem.create("workerSystem_" + id, ConfigFactory.load().getConfig("worker"));

		for (int i = 0; i < nWorkers; i++) {
			workers.add(system.actorOf(new Props(Worker.class), "worker" + i));
		}
	}

	public static void main(String args[]) {
		installWorkerNode(nWorkersToDeploy);
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException ex) {
//			ex.printStackTrace();
//		}
		//workers.add(system.actorOf(new Props(Worker.class), "worker"+nWorkersToDeploy));
		/*for (int i = 0; i < nWorkersToDeploy; i++) {
			workers.get(i).tell(new Messages.Remove());
		}*/
	}
}
