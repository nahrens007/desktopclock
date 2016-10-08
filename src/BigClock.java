import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class BigClock
{
	
	private JFrame frame;
	private JLabel timeLabel;
	private JLabel dateLabel;
	private JPanel container;
	private JPanel main;
	private Calendar calendar;
	private SimpleDateFormat timeFormatter;
	private SimpleDateFormat dateFormatter;
	
	public BigClock() throws IOException
	{
		
		start();
	}
	
	private void start() throws IOException
	{
		
		frame = new JFrame( "CLOCK" );
		frame.setSize( 460, 400 );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		
		main = new JPanel();
		
		setUIFont( new FontUIResource( new Font( "Arial", 0, 40 ) ) );
		
		timeLabel = new JLabel();
		dateLabel = new JLabel();
		container = new JPanel();
		timeLabel.setAlignmentX( Component.CENTER_ALIGNMENT );
		dateLabel.setAlignmentX( Component.CENTER_ALIGNMENT );
		
		container.setLayout( new BoxLayout( container, BoxLayout.Y_AXIS ) );
		
		container.add( timeLabel );
		container.add( dateLabel );
		
		main.setLayout( new GridBagLayout() );
		main.add( container );
		main.setBackground( Color.LIGHT_GRAY );
		container.setBackground( Color.LIGHT_GRAY );
		
		frame.add( main );
		frame.setExtendedState( 6 );
		frame.setIconImage( ImageIO
				.read( BigClock.class.getClassLoader().getResourceAsStream( "clock.png" ) ) );
		frame.setVisible( true );
		
		timeFormatter = new SimpleDateFormat( "kk:mm:ss" );
		dateFormatter = new SimpleDateFormat( "EEEE, MMM d, yyyy" );
		
		/*
		 * Continue updating the time while the program is running, and repaint the label with the time in it...
		 */
		while ( true )
		{
			calendar = Calendar.getInstance();
			
			timeLabel.setText( timeFormatter.format( calendar.getTime() ) );
			timeLabel.repaint();
			dateLabel.setText( dateFormatter.format( calendar.getTime() ) );
			dateLabel.repaint();
			
			/* 
			 * Slow down the refresh rate so it doesn't consume
			 * all the machine's resources.
			 */
			try
			{
				Thread.sleep( 200 );
			} catch ( InterruptedException e )
			{
				System.out.println( "Error: " + e.getMessage() );
			}
			
		}
	}
	
	private void setUIFont( FontUIResource f )
	{
		
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while ( keys.hasMoreElements() )
		{
			Object key = keys.nextElement();
			Object value = UIManager.get( key );
			if ( value instanceof FontUIResource )
			{
				FontUIResource orig = (FontUIResource) value;
				Font font = new Font( f.getFontName(), orig.getStyle(), f.getSize() );
				UIManager.put( key, new FontUIResource( font ) );
			}
		}
	}
	
	public static void main( String[] args ) throws IOException
	{
		
		new BigClock();
	}
	
}
