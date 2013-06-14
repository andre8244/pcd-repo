package determinant_calculator_worker;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import java.util.ArrayList;
import messages.Messages;
import log.l;

/**
 * This class deploys one (or more) workers on a network node.
 *
 */
public class RegisterWorkerApp {

	// TODO in futuro forse sarà meglio poter installare un solo worker su ogni nodo di rete per valutare le performance
	private static final int nWorkersToDeploy = 1;
	private static ActorSystem system;
	private static ArrayList<ActorRef> workers;

	// TODO in futuro questo metodo dovrebbe essere eliminato, s
	public static void deploy(int nWorkers) {
		long id = System.currentTimeMillis();
		workers = new ArrayList<ActorRef>();
		system = ActorSystem.create("workerSystem_" + id, ConfigFactory.load().getConfig("worker"));

		for (int i = 0; i < nWorkers; i++) {
			workers.add(system.actorOf(new Props(Worker.class), "worker" + i));
		}
	}

	public static void main(String args[]) {
		deploy(nWorkersToDeploy);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		//workers.add(system.actorOf(new Props(Worker.class), "worker"+nWorkersToDeploy));
		/*for (int i = 0; i < nWorkersToDeploy; i++) {
			workers.get(i).tell(new Messages.Remove());
		}*/	
	}
}
