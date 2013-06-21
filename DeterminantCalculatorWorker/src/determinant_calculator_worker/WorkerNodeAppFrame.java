package determinant_calculator_worker;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import log.l;
import messages.Messages;

public class WorkerNodeAppFrame extends JFrame implements ActionListener{

	private final JPanel globalPanel, initPanel, addPanel, removePanel;
	private final JButton initWorkerSystemButton,  addWorkerButton, removeWorkerButton;
	private final JTextField addWorkerText, removeWorkerText;
	
	private static int nWorkersToDeploy;
	private static ActorSystem system;
	private static HashMap<String,ActorRef> workers;
	private String me;
	
	public WorkerNodeAppFrame(){
		super("WorkerNodeApp");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Container cp = getContentPane();
		globalPanel = new JPanel((new BorderLayout()));
		initPanel = new JPanel();
		addPanel = new JPanel();
		removePanel = new JPanel();
		
		initWorkerSystemButton = new JButton("Init WorkerSystem");
		addWorkerButton = new JButton("Add Worker");
		removeWorkerButton = new JButton("Remove Worker");
		
		initWorkerSystemButton.addActionListener(this);
		addWorkerButton.addActionListener(this);
		removeWorkerButton.addActionListener(this);
		
		addWorkerText = new JTextField("", 20);
		removeWorkerText = new JTextField("", 20);
		
		initPanel.add(initWorkerSystemButton);
		addPanel.add(addWorkerText);
		addPanel.add(addWorkerButton);
		removePanel.add(removeWorkerText);
		removePanel.add(removeWorkerButton);
		
		globalPanel.add(BorderLayout.NORTH, initPanel);
		globalPanel.add(BorderLayout.CENTER, addPanel);
		globalPanel.add(BorderLayout.SOUTH, removePanel);
		cp.add(globalPanel);
		
		me = "workerNodeAppPanel";
		nWorkersToDeploy = Runtime.getRuntime().availableProcessors();
		workers = new HashMap<String,ActorRef>();
		system = ActorSystem.create("workerSystem_" + System.currentTimeMillis(), ConfigFactory.load().getConfig("worker"));				
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		String cmd = ev.getActionCommand();
		
		if (cmd.equals("Init WorkerSystem")) {
			initWorkerSystem();
		} else if (cmd.equals("Add Worker")) {
			addWorker(addWorkerText.getText());
		}  else if (cmd.equals("Add Worker")) {
			removeWorker(removeWorkerText.getText());
		} 
	}		

	private void initWorkerSystem() {
        for (int i=0; i<nWorkersToDeploy; i++){
			workers.put("worker"+i,system.actorOf(new Props(Worker.class), "worker" + i));
		}
	}

	private void addWorker(String workerName) {
        if (workerName!=null && workers.get(workerName)==null){
			workers.put(workerName, system.actorOf(new Props(Worker.class), workerName));
		} else {
			l.l(me, "Name is null or Worker already exists");
		}
	}

	private void removeWorker(String workerName) {
		if (workerName!=null && workers.get(workerName)!=null){
			workers.get(workerName).tell(new Messages.Remove());
		}  else {
			l.l(me, "Name is null or Worker not exists");
		}
	}
}
