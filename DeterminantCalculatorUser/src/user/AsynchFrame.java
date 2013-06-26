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
	private JLabel lbResult, lbTimeElapsed, lbDuration;
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
		firstPanel.add(progressBar);
		
		lbTimeElapsed = new JLabel("Time elapsed: 0.0 sec");
		secondPanel.add(lbTimeElapsed);
		lbDuration = new JLabel("Duration estimated: : ...");
		thirdPanel.add(lbDuration);
		lbResult = new JLabel("waiting for web service response...");
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
	
	public void updatingData(final int percentage, final String timeElapsed, final String duration) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				progressBar.setValue(percentage);
				lbTimeElapsed.setText(timeElapsed);
				if (!duration.equals("")){
					lbDuration.setText(duration);
				}
			}
		});
	}

}
