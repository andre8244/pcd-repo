package worker;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;

/**
 * This class deploys one (or more) workers on a network node.
 *
 */
public class RegisterWorkerApp {

	// TODO in futuro forse sarà meglio poter installare un solo worker su ogni nodo di rete per valutare le performance
	private static final int nWorkersToDeploy = 7;

	// TODO in futuro questo metodo dovrebbe essere eliminato, s
	public static void deploy(int nWorkers) {
		long id = System.currentTimeMillis();
		ActorSystem system = ActorSystem.create("workerSystem_" + id, ConfigFactory.load().getConfig("worker"));

		for (int i = 0; i < nWorkers; i++) {
			system.actorOf(new Props(Worker.class), "worker-" + i);
		}
	}

	public static void main(String args[]) {
		deploy(nWorkersToDeploy);
	}
}
