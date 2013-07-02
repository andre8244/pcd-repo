package determinant_calculator_user;

import java.awt.Color;
import java.awt.Container;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class SynchFrame extends JFrame {

	private JPanel globalPanel, topPanel, bottomPanel;
	private JLabel lbResult, lbElapsedTime;

	public SynchFrame(String reqId) {
		super(reqId);

		Container cp = getContentPane();
		globalPanel = new JPanel();
		globalPanel.setBackground(Color.LIGHT_GRAY);
		globalPanel.setOpaque(true);
		globalPanel.setLayout(new BoxLayout(globalPanel, BoxLayout.Y_AXIS));

		topPanel = new JPanel();
		topPanel.setBackground(Color.LIGHT_GRAY);
		topPanel.setOpaque(true);
		bottomPanel = new JPanel();
		bottomPanel.setBackground(Color.LIGHT_GRAY);
		bottomPanel.setOpaque(true);

		lbResult = new JLabel("Waiting for result...");
		topPanel.add(lbResult);
		lbElapsedTime = new JLabel(" ");
		bottomPanel.add(lbElapsedTime);

		globalPanel.add(Box.createVerticalStrut(20));
		globalPanel.add(topPanel);
		globalPanel.add(bottomPanel);

		cp.add(globalPanel);
		setSize(300, 140);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void updateReqData(final double result, final int elapsedTimeSecs) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (!("" + result).equals("-0.0")) {
					lbResult.setText("Result: " + result);
				} else { // result == -0.0
					lbResult.setText("Result: ERROR");
				}

				if (elapsedTimeSecs < 60) {
					lbElapsedTime.setText("Elapsed: " + elapsedTimeSecs + " sec");
				} else {
					int minutes = elapsedTimeSecs / 60;
					int seconds = elapsedTimeSecs - (minutes * 60);
					lbElapsedTime.setText("Elapsed: " + minutes + " min " + seconds + " sec");
				}
			}
		});
	}
}
