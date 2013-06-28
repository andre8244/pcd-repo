package user;

// IMPORT DEL WEB SERVICE CLIENT:
//import localhost_client.*;
//import marco_client.*;
import marco_client.*;
//import marcoXP_client.*;
//import windows8dualCore_client.*;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import log.l;

public class UserApp extends JFrame implements ActionListener {

	private JPanel globalPanel, topPanel, centralPanel, bottomPanel;
	private JTextField orderText, fileValuesText;
	private JRadioButton synchronousButton, pollingButton, callbackButton;
	private ButtonGroup group = new ButtonGroup();
	private JButton computeButton;
	private DeterminantCalculatorService servicePort;
	//private static final String INITIAL_FILE_VALUES = System.getProperty("user.home") + System.getProperty("file.separator") + "matrix.txt";
	private static final String INITIAL_FILE_VALUES = "http://pcddeterminant.altervista.org/matrix300@6.03e60.txt";
	private static final int INITIAL_ORDER = 300;
	private String me = "userApp";

	public UserApp() {
		super("User App");

		DeterminantCalculatorService_Service service =
				new DeterminantCalculatorService_Service();
		servicePort = service.getDeterminantCalculatorServicePort();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container cp = getContentPane();
		globalPanel = new JPanel();
		globalPanel.setLayout(new BoxLayout(globalPanel, BoxLayout.Y_AXIS));

		topPanel = new JPanel();

		orderText = new JTextField("" + INITIAL_ORDER, 5);
		orderText.setHorizontalAlignment(JTextField.RIGHT);
		fileValuesText = new JTextField("" + INITIAL_FILE_VALUES, 40);

		topPanel.add(new JLabel("File values:"));
		topPanel.add(fileValuesText);
		topPanel.add(new JLabel("Order:"));
		topPanel.add(orderText);

		centralPanel = new JPanel();
		centralPanel.setLayout(new BoxLayout(centralPanel, BoxLayout.Y_AXIS));

		callbackButton = new JRadioButton("Callback");
		callbackButton.setSelected(true);
		callbackButton.addActionListener(this);
		pollingButton = new JRadioButton("Polling");
		pollingButton.addActionListener(this);
		synchronousButton = new JRadioButton("Synchronous");
		synchronousButton.addActionListener(this);
		// setActionCommand!!
		group.add(callbackButton);
		group.add(pollingButton);
		group.add(synchronousButton);

		centralPanel.add(callbackButton);
		centralPanel.add(pollingButton);
		centralPanel.add(synchronousButton);

		bottomPanel = new JPanel();

		computeButton = new JButton("Compute determinant");
		computeButton.addActionListener(this);

		bottomPanel.add(computeButton);

		globalPanel.add(Box.createVerticalStrut(20));
		globalPanel.add(topPanel);
		globalPanel.add(Box.createVerticalStrut(10));
		globalPanel.add(centralPanel);
		globalPanel.add(Box.createVerticalStrut(10));
		globalPanel.add(bottomPanel);
		globalPanel.add(Box.createVerticalStrut(20));
		cp.add(globalPanel);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public static void main(String args[]) {
		new UserApp();
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		String cmd = ev.getActionCommand();

		if (cmd.equals("Compute determinant")){
			handleCompute();
		}
	}

	private void handleCompute(){
		int order;

		try {
			order = Integer.parseInt(orderText.getText());
		} catch (NumberFormatException ex){
			ex.printStackTrace();
			return;
		}
		String fileValues = fileValuesText.getText();

		if (fileValues.equals(System.getProperty("user.home") + System.getProperty("file.separator") + "matrix.txt")){
			MatrixUtil.genAndWriteToFile(order, 0.1, 0.2, fileValues);
		}
		final String reqId = servicePort.computeDeterminant(order, fileValues);

		if (callbackButton.isSelected()){
			new CallbackThread(reqId,servicePort,new AsynchFrame(reqId+" - Callback")).start();
		} else if (pollingButton.isSelected()) {
			new PollingThread(reqId,servicePort,new AsynchFrame(reqId+" - Polling")).start();
		} else if (synchronousButton.isSelected()) {
			new SynchThread(reqId,servicePort,new SynchFrame(reqId+" - Synchronous")).start();
		}
	}
}
