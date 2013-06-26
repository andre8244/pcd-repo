package user;

import java.awt.Color;
import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class SynchFrame extends JFrame{

	private JPanel globalPanel;
	private JLabel lbResult;
	
	public SynchFrame(String reqId) {
		super(reqId);
		
		Container cp = getContentPane();
		globalPanel = new JPanel();
		globalPanel.setBackground(Color.LIGHT_GRAY);
		globalPanel.setOpaque(true);
		lbResult = new JLabel("waiting for web service response...");
		globalPanel.add(lbResult);
		cp.add(globalPanel);
		setSize(300,100);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	public void updateLabel(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				lbResult.setText(text);
			}
		});
	}
	
}
