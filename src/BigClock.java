import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class BigClock
{
	
	private static String SETTING_FILE_URL = "http://nateshot.homenet.org:8025/clock_settings.txt";
	
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
		frame.setIconImage( ImageIO.read( BigClock.class.getClassLoader().getResourceAsStream( "clock.png" ) ) );
		frame.setVisible( true );
		
		timeFormatter = new SimpleDateFormat( "kk:mm:ss" );
		dateFormatter = new SimpleDateFormat( "EEEE, MMM d, yyyy" );
		
		updateSettings();
		int updateSettingsCounter = 0;
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
			
			// update the settings every 30 seconds (200 * 150 = 30,000ms = 30s
			if ( updateSettingsCounter >= 150 )
			{
				if ( updateSettings() )
					updateSettingsCounter = 0;
				else
					// Double the amount of time before checking for updated settings if it was unsuccessful, that way,
					// we don't waste so many resources trying to update the settings when its not even working.
					updateSettingsCounter = -150;
			}
			
			updateSettingsCounter++;
		}
	}
	
	/**
	 * Update the clock settings from the url text file<br>
	 * http://nateshot.homenet.org:8025/clock_settings.txt
	 */
	private boolean updateSettings()
	{
		
		try
		{
			URL settingsURL = new URL( SETTING_FILE_URL );
			Scanner settings = new Scanner( settingsURL.openStream() );
			String setting, selector[];
			
			/*
			 * Read all the settings from the file
			 */
			while ( settings.hasNextLine() )
			{
				
				/*
				 * Deblank the string for simpler use
				 */
				setting = deblankString( settings.nextLine() ).toLowerCase();
				
				/*
				 * if the line is empty, move to the next one.
				 */
				if ( setting.isEmpty() )
					continue;
				
				/*
				 * Ignore the line if it starts with a non-letter character (such as a // or # for a comment)
				 */
				if ( !Character.isLetter( setting.charAt( 0 ) ) )
					continue;
				
				/*
				 * Check if the setting is valid - with two parts. If not, move
				 * to the next line.
				 */
				selector = setting.split( ":" );
				if ( selector.length != 2 )
					continue;
				
				String isolatedValues;
				Color newColor;
				/*
				 * Switch through possible settings
				 */
				switch ( selector[0] )
				{
					case "background-color":
						/*
						 * Extract and set the color
						 */
						
						// gets the numbers separated by commas
						isolatedValues = selector[1].substring( 1, selector[1].length() - 1 );
						
						newColor = verifyColor( isolatedValues );
						
						main.setBackground( newColor );
						container.setBackground( newColor );
						continue;
					case "font-color":
						/*
						 * Extract and set the color
						 */
						// gets the numbers separated by commas
						isolatedValues = selector[1].substring( 1, selector[1].length() - 1 );
						
						newColor = verifyColor( isolatedValues );
						
						timeLabel.setForeground( newColor );
						dateLabel.setForeground( newColor );
						continue;
					
					default:
						continue;
				}
			}
			
			settings.close();
		} catch ( MalformedURLException e )
		{
			frame.setTitle( "~CLOCK" );
			return false;
		} catch ( IOException e )
		{
			frame.setTitle( "~CLOCK" );
			return false;
		} catch ( NumberFormatException e )
		{
			frame.setTitle( "~CLOCK" );
			return false;
		} catch ( Exception e )
		{
			System.out.println( "The clock has encoutered an unexpected error retrieving the settings file..." );
			System.out.println( e.getMessage() );
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private static Color verifyColor( String color )
	{
		
		int red, green, blue;
		
		color = deblankString( color );
		
		red = Integer.parseInt( color.split( "," )[0] );
		green = Integer.parseInt( color.split( "," )[1] );
		blue = Integer.parseInt( color.split( "," )[2] );
		
		/*
		 * Verify that the new colors will work.
		 */
		if ( red > 255 )
			red = 255;
		else if ( 0 > red )
			red = 0;
		if ( green > 255 )
			green = 255;
		else if ( 0 > green )
			green = 0;
		if ( blue > 255 )
			blue = 255;
		else if ( 0 > blue )
			blue = 0;
		
		return new Color( red, green, blue );
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
	
	public static String deblankString( String str )
	{
		
		int index, length = str.length();
		char ch;
		String deblanked = "";
		
		for ( index = 0; index < length; index++ )
		{
			ch = str.charAt( index );
			if ( !Character.isSpaceChar( ch ) )
			{
				deblanked += ch;
			}
		}
		
		return deblanked;
	}
	
}
