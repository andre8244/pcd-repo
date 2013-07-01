package user;

import java.awt.Color;
import java.awt.Container;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class AsynchFrame extends JFrame{

	private JPanel globalPanel,firstPanel,secondPanel,thirdPanel, fourthPanel;
	private JLabel lbResult, lbElapsedTime, lbEta;
	private JProgressBar progressBar;

	public AsynchFrame(String reqId) {
		super(reqId);

		Container cp = getContentPane();
		globalPanel = new JPanel();
		globalPanel.setLayout(new BoxLayout(globalPanel, BoxLayout.Y_AXIS));
		firstPanel = new JPanel();
		secondPanel = new JPanel();
		secondPanel.setBackground(Color.LIGHT_GRAY);
		secondPanel.setOpaque(true);
		thirdPanel = new JPanel();
		thirdPanel.setBackground(Color.LIGHT_GRAY);
		thirdPanel.setOpaque(true);
		fourthPanel = new JPanel();
		fourthPanel.setBackground(Color.LIGHT_GRAY);
		fourthPanel.setOpaque(true);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		firstPanel.add(new JLabel("Computing "));
		firstPanel.add(progressBar);

		lbElapsedTime = new JLabel("Elapsed: 0 sec");
		secondPanel.add(lbElapsedTime);
		lbEta = new JLabel("ETA: --");
		thirdPanel.add(lbEta);
		lbResult = new JLabel("Waiting for result...");
		fourthPanel.add(lbResult);

		globalPanel.add(Box.createVerticalStrut(25));
		globalPanel.add(firstPanel);
		globalPanel.add(Box.createVerticalStrut(25));
		globalPanel.add(secondPanel);
		globalPanel.add(thirdPanel);
		globalPanel.add(fourthPanel);
		cp.add(globalPanel);
		setSize(300,200);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void updateLabelResult(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				lbResult.setText(text);
			}
		});
	}

	public void updateReqData(final int percentage, final int elapsedTimeSecs, final int eta) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				progressBar.setValue(percentage);

				if (elapsedTimeSecs < 60){
					lbElapsedTime.setText("Elapsed: " + elapsedTimeSecs + " sec");
				} else {
					int minutes = elapsedTimeSecs / 60;
					int seconds  = elapsedTimeSecs - (minutes * 60);
					lbElapsedTime.setText("Elapsed: " + minutes + " min " + seconds + " sec");
				}

				if (eta != -1){
					if (eta < 60){
						lbEta.setText("ETA: "+ eta +" sec");
					} else {
						int minutes = eta / 60;
						int seconds  = eta - (minutes * 60);
						lbEta.setText("ETA: "+ minutes +" min " + seconds + " sec");
					}
				} else {
					lbEta.setText("ETA: --");
				}
			}
		});
	}

}
