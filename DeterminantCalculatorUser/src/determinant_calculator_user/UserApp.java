package determinant_calculator_user;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

//IMPORT DEL WEB SERVICE CLIENT:
import localhost_client.*;
//import marco_client.*;
//import andreaf_client.*;
//import leardini_mac.*;

/**
 * An application to request determinant computations to the web service.
 *
 */
public class UserApp extends JFrame implements ActionListener {

	private JPanel globalPanel, topPanel, centralPanel, bottomPanel;
	private JTextField orderText;
	private JComboBox cbFileValues;
	private JRadioButton synchronousButton, pollingButton, callbackButton;
	private ButtonGroup group = new ButtonGroup();
	private JButton computeButton;
	private DeterminantCalculatorService servicePort;
	private static final int INITIAL_ORDER = 100;
	private boolean connected = false;
	private static final String[] FILE_VALUES = {
		"http://pcddeterminant.altervista.org/matrix100@-4.23e84.txt",
		"http://pcddeterminant.altervista.org/matrix2@3.txt",
		"http://pcddeterminant.altervista.org/matrix200@8.13e-178.txt",
		"http://pcddeterminant.altervista.org/matrix300@6.03e60.txt",
		"http://pcddeterminant.altervista.org/matrix1000@3.84e163.txt",
		"http://pcddeterminant.altervista.org/matrix2000@1.15e-44.txt",
		System.getProperty("user.home") + System.getProperty("file.separator") + "matrix.txt"
	};

	/**
	 * Constructs and displays the GUI of the application.
	 */
	public UserApp() {
		super("User App");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container cp = getContentPane();
		globalPanel = new JPanel();
		globalPanel.setLayout(new BoxLayout(globalPanel, BoxLayout.Y_AXIS));

		topPanel = new JPanel();

		orderText = new JTextField("" + INITIAL_ORDER, 5);
		orderText.setHorizontalAlignment(JTextField.RIGHT);
		cbFileValues = new JComboBox(FILE_VALUES);
		cbFileValues.setEditable(true);
		cbFileValues.addActionListener(this);

		topPanel.add(new JLabel("File values:"));
		topPanel.add(cbFileValues);
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

	/**
	 * Application main.
	 *
	 * @param args
	 */
	public static void main(String args[]) {
		new UserApp();
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		String cmd = ev.getActionCommand();

		if (cmd.equals("Compute determinant")) {
			new Thread() {
				@Override
				public void run() {
					handleCompute();
				}
			}.start();
		}
	}

	private void handleCompute() {
		if (!connected) {
			connected = true;
			DeterminantCalculatorService_Service service = new DeterminantCalculatorService_Service();
			servicePort = service.getDeterminantCalculatorServicePort();
		}
		int order;

		try {
			order = Integer.parseInt(orderText.getText());
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
			return;
		}
		String fileValues = (String) cbFileValues.getSelectedItem();

		// per generare una matrice in un file locale e calcolarene il determinante, usare il seguente path:
		if (fileValues.equals(System.getProperty("user.home") + System.getProperty("file.separator") + "matrix.txt")) {
			MatrixFileGenerator generator = new MatrixFileGenerator();
			generator.generate(order, 0.01, 0.02, fileValues);
		}
		final String reqId = servicePort.computeDeterminant(order, fileValues);

		if (callbackButton.isSelected()) {
			new CallbackThread(reqId, servicePort, new AsynchFrame(reqId + " - Callback")).start();
		} else if (pollingButton.isSelected()) {
			new PollingThread(reqId, servicePort, new AsynchFrame(reqId + " - Polling")).start();
		} else if (synchronousButton.isSelected()) {
			new SynchThread(reqId, servicePort, new SynchFrame(reqId + " - Synchronous")).start();
		}
	}
}
