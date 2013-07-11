package determinant_calculator_worker;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import messages.Messages;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.typesafe.config.ConfigFactory;

/**
 * An application to deploy a set of worker actors on a network node.
 * 
 */
public class WorkerNodeApp extends JFrame implements ActionListener {
	
	private JPanel globalPanel, topPanel, bottomPanel;
	private JLabel lbWorkersToDeploy, lbWorkersDeployed;
	private JSpinner spWorkers;
	private JButton btDeploy;
	private final int nProcessors = Runtime.getRuntime().availableProcessors();
	private ActorSystem actorSystem;
	private ArrayList<ActorRef> workers;
	
	/**
	 * Constructs and displays the GUI of the application.
	 */
	public WorkerNodeApp() {
		super("Worker Node App");
		workers = new ArrayList<>();
		actorSystem = ActorSystem.create("workerSystem_" + System.currentTimeMillis(),
				ConfigFactory.load().getConfig("worker"));
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container cp = getContentPane();
		globalPanel = new JPanel();
		globalPanel.setLayout(new BoxLayout(globalPanel, BoxLayout.Y_AXIS));
		topPanel = new JPanel();
		bottomPanel = new JPanel();
		bottomPanel.setBackground(Color.LIGHT_GRAY);
		bottomPanel.setOpaque(true);
		
		lbWorkersToDeploy = new JLabel("<html>Workers to deploy<br>(recommended: " + nProcessors + ")</html>");
		SpinnerModel model = new SpinnerNumberModel(nProcessors, 0, 100, 1);
		spWorkers = new JSpinner(model);
		btDeploy = new JButton("Deploy");
		btDeploy.addActionListener(this);
		topPanel.add(lbWorkersToDeploy);
		topPanel.add(spWorkers);
		topPanel.add(btDeploy);
		
		lbWorkersDeployed = new JLabel("0 workers deployed");
		bottomPanel.add(lbWorkersDeployed);
		
		globalPanel.add(Box.createVerticalStrut(25));
		globalPanel.add(topPanel);
		globalPanel.add(Box.createVerticalStrut(25));
		globalPanel.add(bottomPanel);
		cp.add(globalPanel);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	/**
	 * Application main.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		new WorkerNodeApp();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		int nWorkersToDeploy = (int) spWorkers.getValue();
		
		if (workers.size() < nWorkersToDeploy) {
			int nWorkersToAdd = nWorkersToDeploy - workers.size();
			
			for (int i = 0; i < nWorkersToAdd; i++) {
				String workerId = "worker" + i + "-" + System.currentTimeMillis();
				workers.add(actorSystem.actorOf(new Props(Worker.class), workerId));
			}
			refreshWorkersDeployedLabel(workers.size());
		} else if (workers.size() > nWorkersToDeploy) {
			int nWorkersToRemove = workers.size() - nWorkersToDeploy;
			
			for (int i = 0; i < nWorkersToRemove; i++) {
				int last = workers.size() - 1;
				workers.get(last).tell(new Messages.Remove());
				workers.remove(last);
			}
			refreshWorkersDeployedLabel(workers.size());
		}
	}
	
	private void refreshWorkersDeployedLabel(final int nWorkers) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (nWorkers == 1) {
					lbWorkersDeployed.setText(nWorkers + " worker deployed");
				} else {
					lbWorkersDeployed.setText(nWorkers + " workers deployed");
				}
			}
		});
	}
}
