package user;

import java.awt.Color;
import java.net.URL;
// IMPORT DEL WEB SERVICE CLIENT:
import localhost_client.*;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class UserAppFrame extends JFrame implements ActionListener {

	private JPanel globalPanel, topPanel, centralPanel, bottomPanel;
	private JTextField orderText, fileValuesText;
	private JButton computeButton, synchButton, pollingButton, callbackButton;
	private JLabel lbInfo;
	private DeterminantCalculatorService servicePort;
	private static final int SYNCHRONOUS = 0;
	private static final int POLLING = 1;
	private static final int CALLBACK = 2;
	private String path = System.getProperty("user.home") + System.getProperty("file.separator");
	private String fileValues;
	private URL fileValuesURL;
	private int order = 1000;
	// select execution policy:
	private int policy = POLLING;
	private String me = "userApp";

	public UserAppFrame() {
		super("User App");
		
		DeterminantCalculatorService_Service service =
				new DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();
		
		fileValues = path + "matrix.txt";
		//fileValues = path + "matrix" + order + ".txt";
		//fileValues = path + "matrix300@6.03e60.txt";
		//fileValues = "http://pcddeterminant.altervista.org/matrix300@6.03e60.txt";

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container cp = getContentPane();
		globalPanel = new JPanel();
		globalPanel.setLayout(new BoxLayout(globalPanel, BoxLayout.Y_AXIS));
		topPanel = new JPanel();
		centralPanel = new JPanel();
		bottomPanel = new JPanel();
		bottomPanel.setBackground(Color.LIGHT_GRAY);
		bottomPanel.setOpaque(true);
		
		orderText = new JTextField(""+order);
		fileValuesText = new JTextField(""+fileValues);
		computeButton = new JButton("Compute determinant");
		computeButton.addActionListener(this);
		topPanel.add(new JLabel("order"));
		topPanel.add(orderText);
		topPanel.add(new JLabel("fileValues"));
		topPanel.add(fileValuesText);
		topPanel.add(computeButton);

		synchButton = new JButton("Synchronous");
		synchButton.addActionListener(this);
		pollingButton = new JButton("Polling");
		pollingButton.setEnabled(false);
		pollingButton.addActionListener(this);
		callbackButton = new JButton("Callback");
		callbackButton.addActionListener(this);
		centralPanel.add(synchButton);
		centralPanel.add(pollingButton);
		centralPanel.add(callbackButton);
		
		lbInfo = new JLabel("Set POLLING");
		bottomPanel.add(lbInfo);

		globalPanel.add(Box.createVerticalStrut(25));
		globalPanel.add(topPanel);
		globalPanel.add(Box.createVerticalStrut(25));
		globalPanel.add(centralPanel);
		globalPanel.add(Box.createVerticalStrut(25));
		globalPanel.add(bottomPanel);		
		cp.add(globalPanel);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public static void main(String args[]) {
		new UserAppFrame();
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		String cmd = ev.getActionCommand();
		switch (cmd) {
			case "Compute determinant":
				handleCompute(Integer.parseInt(orderText.getText()),fileValuesText.getText());
				break;
			case "Synchronous":
				policy = SYNCHRONOUS;
				lbInfo.setText("Set SYNCHRONOUS");
				synchButton.setEnabled(false);
				pollingButton.setEnabled(true);
				callbackButton.setEnabled(true);
				break;
			case "Polling":
				policy = POLLING;
				lbInfo.setText("Set POLLING");
				synchButton.setEnabled(true);
				pollingButton.setEnabled(false);
				callbackButton.setEnabled(true);
				break;
			case "Callback":
				policy = CALLBACK;
				lbInfo.setText("Set CALLBACK");
				synchButton.setEnabled(true);
				pollingButton.setEnabled(true);
				callbackButton.setEnabled(false);
				break;
		}
	}

	private void handleCompute(final int order, final String fileValues) {
		this.order = order;
		this.fileValues = fileValues;
		
		MatrixUtil.genAndWriteToFile(order, 0.1, 0.2, fileValues);
		
		final String reqId = servicePort.computeDeterminant(order, fileValues);
		
		switch (policy) {
			case SYNCHRONOUS:
				new SynchThread(reqId,servicePort,new SynchFrame(reqId+" - SYNCHRONOUS")).start();
				break;
			case POLLING:
				new PollingThread(reqId,servicePort,new AsynchFrame(reqId+" - POLLING")).start();
				break;
			case CALLBACK:
				new CallbackThread(reqId,servicePort,new AsynchFrame(reqId+" - CALLBACK")).start();
				break;
		}
	}

}
