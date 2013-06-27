package user;

import java.awt.Color;
import java.awt.Container;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class SynchFrame extends JFrame{

	private JPanel globalPanel,topPanel,bottomPanel;
	private JLabel lbResult, lbDuration;

	public SynchFrame(String reqId) {
		super(reqId);

		Container cp = getContentPane();
		globalPanel = new JPanel();
		globalPanel.setLayout(new BoxLayout(globalPanel, BoxLayout.Y_AXIS));
		topPanel = new JPanel();
		topPanel.setBackground(Color.LIGHT_GRAY);
		topPanel.setOpaque(true);
		bottomPanel = new JPanel();
		bottomPanel.setBackground(Color.LIGHT_GRAY);
		bottomPanel.setOpaque(true);

		lbDuration = new JLabel("Duration: --");
		topPanel.add(lbDuration);
		lbResult = new JLabel("Waiting for result...");
		bottomPanel.add(lbResult);

		globalPanel.add(topPanel);
		globalPanel.add(bottomPanel);
		cp.add(globalPanel);
		setSize(300,100);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void updateData(final String result, final String duration) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				lbResult.setText(result);
				lbDuration.setText(duration);
			}
		});
	}

}
