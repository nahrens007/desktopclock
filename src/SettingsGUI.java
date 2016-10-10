import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SettingsGUI
{
	
	private JFrame frame;
	private JPanel mainPanel;
	private JLabel titleLabel;
	private JLabel extStateLabel;
	private JLabel useNetLabel;
	private JLabel queryInterval;
	private JLabel refreshInterval;
	private JLabel urlForSettings;
	private JTextField titleField;
	private JTextField extStateField;
	private JTextField queryIntervalField;
	private JTextField refreshIntervalField;
	private JTextField urlForSettingsField;
	private JCheckBox useNetCheckbox;
	private JButton save;
	private JButton cancel;
	
	public SettingsGUI()
	{
		frame = new JFrame( "Clock Settings" );
		frame.setSize( 800, 500 );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		
		mainPanel = new JPanel();
		mainPanel.setLayout( new GridLayout( 7, 2 ) );
		
		titleLabel = new JLabel( "Title:" );
		titleLabel.setToolTipText( "Title For Clock GUI" );
		titleField = new JTextField( 20 );
		titleField.setToolTipText( "Title For Clock GUI" );
		mainPanel.add( titleLabel, 0 );
		mainPanel.add( titleField, 1 );
		
		extStateLabel = new JLabel( "Extended State:" );
		extStateLabel.setToolTipText(
				"NORMAL=0, ICONIFIED=1, MAXIMIZED_HORIZ=2, MAXIMIZED_VERT=4, MAXIMIZED_BOTH=6" );
		extStateField = new JTextField( 20 );
		extStateField.setToolTipText(
				"NORMAL=0, ICONIFIED=1, MAXIMIZED_HORIZ=2, MAXIMIZED_VERT=4, MAXIMIZED_BOTH=6" );
		mainPanel.add( extStateLabel, 2 );
		mainPanel.add( extStateField, 3 );
		
		useNetLabel = new JLabel( "Use Web Settings:" );
		useNetLabel.setToolTipText( "Use settings found at the URL below" );
		useNetCheckbox = new JCheckBox();
		useNetCheckbox.setToolTipText( "Use settings found at the URL below" );
		mainPanel.add( useNetLabel, 4 );
		mainPanel.add( useNetCheckbox, 5 );
		
		queryInterval = new JLabel( "Query Interval:" );
		queryInterval.setToolTipText( "Interval to update settings from online file" );
		queryIntervalField = new JTextField( 20 );
		queryIntervalField.setToolTipText( "Interval to update settings from online file" );
		mainPanel.add( queryInterval, 6 );
		mainPanel.add( queryIntervalField, 7 );
		
		refreshInterval = new JLabel( "Refresh Interval:" );
		refreshInterval.setToolTipText( "Interval of refreshing date/time" );
		refreshIntervalField = new JTextField( 20 );
		refreshIntervalField.setToolTipText( "Interval of refreshing date/time" );
		mainPanel.add( refreshInterval, 8 );
		mainPanel.add( refreshIntervalField, 9 );
		
		urlForSettings = new JLabel( "URL Settings File:" );
		urlForSettings.setToolTipText( "URL to fetch settings from" );
		urlForSettingsField = new JTextField( 20 );
		urlForSettingsField.setToolTipText( "URL to fetch settings from" );
		mainPanel.add( urlForSettings, 10 );
		mainPanel.add( urlForSettingsField, 11 );
		
		save = new JButton( "Save" );
		save.addActionListener( new SaveListener() );
		cancel = new JButton( "Cancel" );
		cancel.addActionListener( new CancelListener() );
		mainPanel.add( save, 12 );
		mainPanel.add( cancel, 13 );
		
		frame.add( mainPanel );
		try
		{
			frame.setIconImage( ImageIO
					.read( BigClock.class.getClassLoader().getResourceAsStream( "clock.png" ) ) );
		} catch ( IOException e )
		{
			e.printStackTrace();
		}
		frame.setVisible( true );
	}
	
	private class SaveListener implements ActionListener
	{
		
		@Override
		public void actionPerformed( ActionEvent e )
		{
			
			// Need to add get/set methods for all settigns
			// In the BigClock class.
			
			frame.dispose();
		}
		
	}
	
	private class CancelListener implements ActionListener
	{
		
		@Override
		public void actionPerformed( ActionEvent e )
		{
			
			frame.dispose();
		}
		
	}
}
