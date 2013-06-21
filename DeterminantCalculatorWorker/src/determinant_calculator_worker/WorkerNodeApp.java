package determinant_calculator_worker;

/**
 * This class deploys one (or more) workers on a network node.
 *
 */
public class WorkerNodeApp {

	public static void main(String args[]) {
		WorkerNodeAppFrame frame = new WorkerNodeAppFrame();
		frame.setVisible(true);
//		installWorkerNode(nWorkersToDeploy);
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
