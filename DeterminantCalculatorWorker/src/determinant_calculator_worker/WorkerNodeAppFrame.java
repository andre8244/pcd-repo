package determinant_calculator_worker;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import messages.Messages;

public class WorkerNodeAppFrame extends JFrame implements ActionListener{

	private final JPanel globalPanel, controlPanel, infoPanel, initPanel, addPanel, removePanel, workersPanel, consolePanel;
	private final JButton initWorkerSystemButton,  addWorkerButton, removeWorkerButton;
	private final JTextField addWorkerText, removeWorkerText, consoleText;
	private final JTextArea workersText;
	private final JScrollPane scrollPane;
	
	private static int nWorkersToDeploy;
	private static ActorSystem system;
	private static HashMap<String,ActorRef> workers;
	
	public WorkerNodeAppFrame(){
		super("WorkerNodeApp");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Container cp = getContentPane();
		globalPanel = new JPanel((new BorderLayout()));
		controlPanel = new JPanel((new BorderLayout()));
		infoPanel = new JPanel((new BorderLayout()));
		
		initPanel = new JPanel();
		addPanel = new JPanel();
		removePanel = new JPanel();
		
		initWorkerSystemButton = new JButton("Init WorkerSystem");
		initWorkerSystemButton.setPreferredSize(new Dimension(200, 30));
		addWorkerButton = new JButton("Add Worker");
		addWorkerButton.setPreferredSize(new Dimension(200, 30));
		removeWorkerButton = new JButton("Remove Worker");
		removeWorkerButton.setPreferredSize(new Dimension(200, 30));
		
		initWorkerSystemButton.addActionListener(this);
		addWorkerButton.addActionListener(this);
		removeWorkerButton.addActionListener(this);
		
		addWorkerText = new JTextField("", 15);
		removeWorkerText = new JTextField("", 15);
		
		initPanel.add(initWorkerSystemButton);
		addPanel.add(addWorkerText);
		addPanel.add(addWorkerButton);
		removePanel.add(removeWorkerText);
		removePanel.add(removeWorkerButton);
		
		controlPanel.add(BorderLayout.NORTH, initPanel);
		controlPanel.add(BorderLayout.CENTER, addPanel);
		controlPanel.add(BorderLayout.SOUTH, removePanel);
		
		workersPanel = new JPanel();
		consolePanel = new JPanel();
		
		workersText = new JTextArea("Worker's list: no workers");
		workersText.setEditable(false);
		scrollPane = new JScrollPane(workersText);
		scrollPane.setPreferredSize(new Dimension(350, 350));
		consoleText = new JTextField("");
		consoleText.setPreferredSize(new Dimension(350, 30));
		consoleText.setEditable(false);
		
		workersPanel.add(scrollPane);
		consolePanel.add(consoleText);
		
		infoPanel.add(BorderLayout.NORTH, workersPanel);
		infoPanel.add(BorderLayout.SOUTH, consolePanel);	
		
		globalPanel.add(BorderLayout.NORTH, controlPanel);
		globalPanel.add(BorderLayout.SOUTH, infoPanel);
		
		cp.add(globalPanel);
		setSize(600,600);
		setLocationRelativeTo(null);
		
		nWorkersToDeploy = Runtime.getRuntime().availableProcessors();
		workers = new HashMap<String,ActorRef>();
		system = ActorSystem.create("workerSystem_" + System.currentTimeMillis(), ConfigFactory.load().getConfig("worker"));				
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		String cmd = ev.getActionCommand();
		switch (cmd) {
			case "Init WorkerSystem":
				initWorkerSystem();
				break;
			case "Add Worker":
				addWorker(addWorkerText.getText());
				break;
			case "Remove Worker": 
				removeWorker(removeWorkerText.getText());
				break;
		}
	}		

	private void initWorkerSystem() {
        for (int i=0; i<nWorkersToDeploy; i++){
			workers.put("worker"+i,system.actorOf(new Props(Worker.class), "worker" + i));
		}
		refreshWorkersList();
		consoleText.setText("Init worker system completed");
	}

	private void addWorker(String workerName) {
        if (workerName.equals("")){
			consoleText.setText("Error: empty string!");
		} else if (workers.get(workerName)!=null){
			consoleText.setText(workerName + " already exists");
		} else {
			workers.put(workerName, system.actorOf(new Props(Worker.class), workerName));
			refreshWorkersList();
			consoleText.setText(workerName + " added to worker's list");
		}
	}

	private void removeWorker(String workerName) {
        if (workerName.equals("")){
			consoleText.setText("Error: empty string!");
		} else if (workers.get(workerName)==null){
			consoleText.setText(workerName + " not exists");
		} else {
			workers.get(workerName).tell(new Messages.Remove());
			workers.remove(workerName);
			refreshWorkersList();
			consoleText.setText(workerName + " removed from worker's list");
		}
	}

	private void refreshWorkersList() {
		String list = "Worker's list: ";
		if (workers.isEmpty()){
			list = list + "no workers";
			initWorkerSystemButton.setEnabled(true);
		} else {
			for (String worker : workers.keySet()){
				list = list + "\n" + worker;
			}
			if (initWorkerSystemButton.isEnabled()){
				initWorkerSystemButton.setEnabled(false);
			}
		}
		workersText.setText(list);
	}
}
