import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import com.sun.glass.events.KeyEvent;

public class BigClock
{
	
	public final static String DELIMITER = "::";
	private static String settingsFileURL = "http://nateshot.homenet.org:8025/clock_settings.txt";
	private final static float VERSION_NUMBER = 2.2f;
	private final static String defaultSettings = "background-color" + DELIMITER +
			"(45, 54, 45)\n" + "font-color" + DELIMITER + "(155,172,134)\n" + "title" + DELIMITER +
			"Clock%v" + VERSION_NUMBER + "\n" + "extended-state" + DELIMITER + "6\n" +
			"query-interval" + DELIMITER + "60\n" + "refresh-interval" + DELIMITER + "500\n" +
			"settings-url" + DELIMITER + settingsFileURL + "\n" + "use-net-settings" + DELIMITER +
			"true";
	
	private final String DEFAULT_FILEPATH = "clock_settings";
	
	private JFrame frame;
	private JLabel timeLabel;
	private JLabel dateLabel;
	private JPanel container;
	private JPanel main;
	
	private JMenuBar menuBar;
	private JMenu fileMenu; // File menu - will contain exit
	private JMenu editMenu; // Exit menu - will contain settings and preferences
	private JMenuItem exitItem; // To exit
	private JMenuItem bgColorItem;
	private JMenuItem fgColorItem;
	private JMenuItem preferenceItem;
	
	private Calendar calendar;
	private SimpleDateFormat timeFormatter;
	private SimpleDateFormat dateFormatter;
	private String defaultTitle = "CLOCK v" + VERSION_NUMBER;
	private int updateQueryInterval = 30;
	private long refreshInterval = 200;
	private boolean useNetSettings = true;
	
	private Scanner settingsScanner;
	
	public BigClock() throws IOException
	{
		/*
		 * Create window and initialize clock.
		 */
		frame = new JFrame( defaultTitle );
		frame.setSize( 460, 400 );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		
		buildMenuBar();
		
		main = new JPanel();
		
		BigClock.setUIFont( new FontUIResource( new Font( "Arial", 0, 40 ) ) );
		
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
		setBackgroundColor( Color.LIGHT_GRAY );
		
		frame.add( main );
		frame.setExtendedState( 6 );
		frame.setIconImage( ImageIO
				.read( BigClock.class.getClassLoader().getResourceAsStream( "clock.png" ) ) );
		frame.setVisible( true );
		
		timeFormatter = new SimpleDateFormat( "kk:mm:ss" );
		dateFormatter = new SimpleDateFormat( "EEEE, MMM d, yyyy" );
		start();
	}
	
	private void start() throws IOException
	{
		
		/*
		 * load the settings
		 */
		useFileSettings();
		
		/*
		 * If using the net settings has been selected from the 
		 * settings file, then we use the net settings before
		 * using the local file settings.
		 */
		if ( useNetSettings )
		{
			useNetSettings();
		}
		
		int updateSettingsCounter = 0;
		
		/*
		 * Continue updating the time while the program is running...
		 */
		while ( true )
		{
			calendar = Calendar.getInstance();
			
			timeLabel.setText( timeFormatter.format( calendar.getTime() ) );
			dateLabel.setText( dateFormatter.format( calendar.getTime() ) );
			
			/* 
			 * Slow down the refresh rate so it doesn't consume
			 * all the machine's resources. 
			 */
			try
			{
				Thread.sleep( refreshInterval );
			} catch ( InterruptedException e )
			{
				System.out.println( "Error: " + e.getMessage() );
			}
			
			// update the settings only every interval period ( * 5 because
			// there are 5 * 200ms in a second)
			if ( updateSettingsCounter >= updateQueryInterval * (int) (1000 / refreshInterval) )
			{
				if ( updateSettings( settingsScanner ) )
					updateSettingsCounter = 0;
				else
					// Double the amount of time before checking for updated
					// settings if it was unsuccessful, that way,
					// we don't waste so many resources trying to update the
					// settings when its not even working.
					updateSettingsCounter = -updateQueryInterval * (int) (1000 / refreshInterval);
			}
			
			updateSettingsCounter++;
		}
	}
	
	private void useNetSettings()
	{
		
		try
		{
			URL settingsURL = new URL( settingsFileURL );
			settingsScanner = new Scanner( settingsURL.openStream() );
		} catch ( Exception ex )
		{
			try
			{
				/*
				 * If a settings file doesn't exist, settings from the Internet will be loaded.
				 */
				settingsScanner = new Scanner( new File( DEFAULT_FILEPATH ) );
			} catch ( Exception e ) // Do the following for all exceptions
									// thrown
			{
				settingsScanner = new Scanner( defaultSettings );
			}
		}
		
		updateSettings( settingsScanner );
		return;
	}
	
	private void useFileSettings()
	{
		
		try
		{
			/*
			 * If a settings file doesn't exist, settings from the Internet will be loaded.
			 */
			settingsScanner = new Scanner( new File( DEFAULT_FILEPATH ) );
		} catch ( Exception e ) // Do the following for all exceptions thrown
		{
			try
			{
				URL settingsURL = new URL( settingsFileURL );
				settingsScanner = new Scanner( settingsURL.openStream() );
			} catch ( Exception ex )
			{
				settingsScanner = new Scanner( defaultSettings );
			}
		}
		updateSettings( settingsScanner );
		return;
	}
	
	public void setBackgroundColor( Color color )
	{
		
		main.setBackground( color );
		container.setBackground( color );
		return;
	}
	
	public Color getBackgroundColor()
	{
		
		return main.getBackground();
	}
	
	public void setTextColor( Color color )
	{
		
		timeLabel.setForeground( color );
		dateLabel.setForeground( color );
		return;
	}
	
	public Color getTextColor()
	{
		
		return timeLabel.getForeground();
	}
	
	private void buildMenuBar()
	{
		
		menuBar = new JMenuBar();
		
		buildFileMenu();
		buildEditMenu();
		
		menuBar.add( fileMenu );
		menuBar.add( editMenu );
		
		frame.setJMenuBar( menuBar );
	}
	
	private void buildFileMenu()
	{
		
		exitItem = new JMenuItem( "Exit" );
		exitItem.setMnemonic( KeyEvent.VK_X );
		exitItem.addActionListener( new ExitListener() );
		
		// Create a JMenu object for the file menu
		fileMenu = new JMenu( "File" );
		fileMenu.setMnemonic( KeyEvent.VK_F );
		
		fileMenu.add( exitItem );
		
		return;
	}
	
	private void buildEditMenu()
	{
		
		bgColorItem = new JMenuItem( "Background Color" );
		bgColorItem.setMnemonic( KeyEvent.VK_B );
		bgColorItem.addActionListener( new ColorListener() );
		
		fgColorItem = new JMenuItem( "Text Color" );
		fgColorItem.setMnemonic( KeyEvent.VK_T );
		fgColorItem.addActionListener( new ColorListener() );
		
		preferenceItem = new JMenuItem( "Preferences" );
		preferenceItem.setMnemonic( KeyEvent.VK_P );
		preferenceItem.addActionListener( new PreferenceListener() );
		
		editMenu = new JMenu( "Edit" );
		editMenu.setMnemonic( KeyEvent.VK_E );
		
		editMenu.add( bgColorItem );
		editMenu.add( fgColorItem );
		editMenu.add( preferenceItem );
		
		return;
		
	}
	
	private void saveSettings( String filePath )
	{
		
		/*
		 * don't save the net settings to file
		 */
		if ( useNetSettings )
		{
			String tempSettings = "";
			try
			{
				Scanner fileIn = new Scanner( new File( filePath ) );
				String read;
				while ( fileIn.hasNextLine() )
				{
					read = fileIn.nextLine();
					if ( !read.split( "::" )[0].equals( "use-net-settings" ) )
					{
						tempSettings += read + "\n";
					}
				}
				fileIn.close();
			} catch ( FileNotFoundException e )
			{
				System.out
						.println( "Unable to save settings to the file path: " + filePath + "\n" );
				e.printStackTrace();
			}
			
			try
			{
				PrintWriter fileOut = new PrintWriter( new File( filePath ) );
				fileOut.println( tempSettings );
				fileOut.println( "use-net-settings" + DELIMITER + useNetSettings );
				fileOut.close();
			} catch ( FileNotFoundException e )
			{
				System.out
						.println( "Unable to save settings to the file path: " + filePath + "\n" );
				e.printStackTrace();
			}
			
			return;
		}
		
		try
		{
			PrintWriter fileOut = new PrintWriter( new File( filePath ) );
			
			fileOut.println( "background-color" + DELIMITER + "(" + getBackgroundColor().getRed() +
					"," + getBackgroundColor().getGreen() + "," + getBackgroundColor().getBlue() +
					")" );
			fileOut.println( "font-color" + DELIMITER + "(" + getTextColor().getRed() + "," +
					getTextColor().getGreen() + "," + getTextColor().getBlue() + ")" );
			fileOut.println( "title" + DELIMITER + BigClock.despace( frame.getTitle() ) ); // Need
			// to
			// replace
			// spaces with a %
			fileOut.println( "extended-state" + DELIMITER + frame.getExtendedState() );
			fileOut.println( "query-interval" + DELIMITER + updateQueryInterval );
			fileOut.println( "refresh-interval" + DELIMITER + refreshInterval );
			fileOut.println( "settings-url" + DELIMITER + settingsFileURL );
			fileOut.println( "use-net-settings" + DELIMITER + useNetSettings );
			fileOut.close();
		} catch ( FileNotFoundException e )
		{
			System.out.println( "Unable to save settings to the file path: " + filePath + "\n" );
			e.printStackTrace();
		}
		
		return;
	}
	
	/**
	 * Update the clock settings from the url text file<br>
	 * http://nateshot.homenet.org:8025/clock_settings.txt
	 */
	private boolean updateSettings( Scanner settings )
	{
		
		// Reset the title just in case it is set to an error title.
		frame.setTitle( defaultTitle );
		
		try
		{
			
			String setting, selector[];
			
			/*
			 * Read all the settings from the file
			 */
			while ( settings.hasNextLine() )
			{
				
				/*
				 * Deblank the string for simpler use
				 */
				setting = removeWhiteSpaces( settings.nextLine() );
				
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
				selector = setting.split( DELIMITER );
				if ( selector.length != 2 )
					continue;
				
				String tempString;
				Color newColor;
				/*
				 * Switch through possible settings
				 */
				switch ( selector[0].toLowerCase() )
				{
					case "background-color":
						/*
						 * Extract and set the color
						 */
						
						// gets the numbers separated by commas
						tempString = selector[1].substring( 1, selector[1].length() - 1 );
						
						newColor = verifyColor( tempString );
						
						setBackgroundColor( newColor );
						;
						continue;
					case "font-color":
						/*
						 * Extract and set the color
						 */
						// gets the numbers separated by commas
						tempString = selector[1].substring( 1, selector[1].length() - 1 );
						
						newColor = verifyColor( tempString );
						
						setTextColor( newColor );
						continue;
					
					case "title":
						frame.setTitle( respace( selector[1] ) );
						continue;
					
					case "extended-state":
						frame.setExtendedState( Integer.parseInt( selector[1] ) );
						continue;
					
					case "query-interval":
						updateQueryInterval = Integer.parseInt( selector[1] );
						continue;
					
					case "refresh-interval":
						refreshInterval = Long.parseLong( selector[1] );
						continue;
					
					case "settings-url":
						settingsFileURL = selector[1];
						continue;
					
					case "use-net-settings":
						useNetSettings = Boolean.parseBoolean( selector[1] );
						continue;
					
					default:
						continue;
				}
			}
			
		} catch ( NumberFormatException e )
		{
			System.out.println( "Error: " + e.getMessage() );
			frame.setTitle( "CLOCK - Parse Error" );
			return false;
		} catch ( Exception e )
		{
			System.out.println(
					"The clock has encoutered an unexpected error retrieving the settings file..." );
			System.out.println( e.getMessage() );
			e.printStackTrace();
			frame.setTitle( "CLOCK - Unexpected Error" );
			return false;
		}
		
		return true;
	}
	
	/**
	 * This method will replace all '%' symbols in a string with a single blank
	 * space.
	 * 
	 * @param deblankedString
	 *            The string to be re-spaced.
	 * @return A string void of any '%' symbols, which have been replaced by a
	 *         space.
	 */
	private static String respace( String deblankedString )
	{
		
		String str = "";
		char[] charArray = deblankedString.toCharArray();
		
		for ( int i = 0; i < charArray.length; i++ )
		{
			if ( charArray[i] == '%' )
				str += " ";
			else
				str += charArray[i];
		}
		
		return str;
	}
	
	private static String despace( String spacedString )
	{
		
		String str = "";
		char[] charArray = spacedString.toCharArray();
		
		for ( int i = 0; i < charArray.length; i++ )
		{
			if ( charArray[i] == ' ' )
				str += "%";
			else
				str += charArray[i];
		}
		
		return str;
	}
	
	/**
	 * This method takes a string in the format of "N,N,N" (excluding the
	 * quotes) and parses each N into a number which will be used for its
	 * corresponding RGB color. The string must be deblanked.
	 * 
	 * @param color
	 *            The string representing the RGB values
	 * @return A color object created from the RGB values passed in (unless the
	 *         numbers passed it were larger than 255 or less than 0, then that
	 *         R, G, or B value will be 255 or 0, respectively.
	 */
	private static Color verifyColor( String color )
	{
		
		int red, green, blue;
		
		color = removeWhiteSpaces( color );
		
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
	
	/**
	 * Sets the font of the UI FontUIResource passed in.
	 * 
	 * @param f
	 *            The FontUIResource to edit the font of.
	 */
	private static void setUIFont( FontUIResource f )
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
	
	/**
	 * This method removes any blanks (new line character, space, or new line
	 * character) from a string.
	 * 
	 * @param str
	 *            The string to remove white spaces from.
	 * @return The same string passed in, void of any white spaces.
	 */
	public static String removeWhiteSpaces( String str )
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
	
	public static void main( String[] args ) throws IOException
	{
		
		new BigClock();
	}
	
	private class ExitListener implements ActionListener
	{
		
		public void actionPerformed( ActionEvent e )
		{
			
			saveSettings( DEFAULT_FILEPATH );
			System.exit( 0 );
		}
	}
	
	private class ColorListener implements ActionListener
	{
		
		public void actionPerformed( ActionEvent e )
		{
			
			// Show color picker and set the setting accordingly (BG or FG)
			Color newColor = JColorChooser.showDialog( frame, "Select a Color", Color.BLACK );
			
			if ( e.getSource() == bgColorItem )
			{
				// Handle Background Color and return
				setBackgroundColor( newColor );
				
				saveSettings( DEFAULT_FILEPATH );
				return;
			}
			
			// Handle FG color and return
			setTextColor( newColor );
			saveSettings( DEFAULT_FILEPATH );
			return;
		}
	}
	
	private class PreferenceListener implements ActionListener
	{
		
		public void actionPerformed( ActionEvent e )
		{
			
			// Open preferences GUI
			new PreferenceWindow();
			saveSettings( DEFAULT_FILEPATH );
			return;
		}
	}
	
	private class PreferenceWindow
	{
		
		private JFrame pFrame;
		private JPanel mainPanel;
		private JCheckBox useNetCheckbox;
		private JLabel titleLabel;
		private JLabel extStateLabel;
		private JLabel useNetLabel;
		private JLabel queryIntervalLabel;
		private JLabel refreshIntervalLabel;
		private JLabel urlForSettingsLabel;
		private JTextField titleField;
		private JTextField extStateField;
		private JTextField queryIntervalField;
		private JTextField refreshIntervalField;
		private JTextField urlForSettingsField;
		private JButton save;
		private JButton cancel;
		
		private Font font;
		
		public PreferenceWindow()
		{
			pFrame = new JFrame( "Clock Settings" );
			pFrame.setSize( 800, 500 );
			pFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
			
			mainPanel = new JPanel();
			mainPanel.setLayout( new GridLayout( 7, 2 ) );
			
			font = new Font( "Arial", Font.PLAIN, 23 );
			
			useNetLabel = new JLabel( "Use Web Settings:" );
			useNetLabel.setToolTipText( "Use settings found at the URL below" );
			useNetLabel.setFont( font );
			useNetCheckbox = new JCheckBox();
			useNetCheckbox.setToolTipText( "Use settings found at the URL below" );
			useNetCheckbox.addItemListener( new NetCheckboxListener() );
			useNetCheckbox.setFont( font );
			mainPanel.add( useNetLabel, 0 );
			mainPanel.add( useNetCheckbox, 1 );
			
			titleLabel = new JLabel( "Title:" );
			titleLabel.setToolTipText( "Title For Clock GUI" );
			titleLabel.setFont( font );
			titleField = new JTextField( 20 );
			titleField.setText( frame.getTitle() );
			titleField.setToolTipText( "Title For Clock GUI" );
			titleField.setFont( font );
			mainPanel.add( titleLabel, 2 );
			mainPanel.add( titleField, 3 );
			
			extStateLabel = new JLabel( "Extended State:" );
			extStateLabel.setToolTipText(
					"NORMAL=0, ICONIFIED=1, MAXIMIZED_HORIZ=2, MAXIMIZED_VERT=4, MAXIMIZED_BOTH=6" );
			extStateLabel.setFont( font );
			extStateField = new JTextField( 20 );
			extStateField.setText( String.valueOf( frame.getExtendedState() ) );
			extStateField.setToolTipText(
					"NORMAL=0, ICONIFIED=1, MAXIMIZED_HORIZ=2, MAXIMIZED_VERT=4, MAXIMIZED_BOTH=6" );
			extStateField.setFont( font );
			mainPanel.add( extStateLabel, 4 );
			mainPanel.add( extStateField, 5 );
			
			refreshIntervalLabel = new JLabel( "Refresh Interval:" );
			refreshIntervalLabel.setToolTipText( "Interval of refreshing date/time" );
			refreshIntervalLabel.setFont( font );
			refreshIntervalField = new JTextField( 20 );
			refreshIntervalField.setText( String.valueOf( BigClock.this.refreshInterval ) );
			refreshIntervalField.setToolTipText( "Interval of refreshing date/time" );
			refreshIntervalField.setFont( font );
			mainPanel.add( refreshIntervalLabel, 6 );
			mainPanel.add( refreshIntervalField, 7 );
			
			urlForSettingsLabel = new JLabel( "URL Settings File:" );
			urlForSettingsLabel.setToolTipText( "URL to fetch settings from" );
			urlForSettingsLabel.setFont( font );
			urlForSettingsField = new JTextField();
			urlForSettingsField.setText( settingsFileURL );
			urlForSettingsField.setToolTipText( "URL to fetch settings from" );
			urlForSettingsField.setFont( font );
			mainPanel.add( urlForSettingsLabel, 8 );
			mainPanel.add( urlForSettingsField, 9 );
			
			queryIntervalLabel = new JLabel( "Query Interval:" );
			queryIntervalLabel.setToolTipText( "Interval to update settings from online file" );
			queryIntervalLabel.setFont( font );
			queryIntervalField = new JTextField( 20 );
			queryIntervalField.setText( String.valueOf( updateQueryInterval ) );
			queryIntervalField.setToolTipText( "Interval to update settings from online file" );
			queryIntervalField.setFont( font );
			mainPanel.add( queryIntervalLabel, 10 );
			mainPanel.add( queryIntervalField, 11 );
			
			save = new JButton( "Save" );
			save.addActionListener( new SaveListener() );
			save.setFont( font );
			cancel = new JButton( "Cancel" );
			cancel.addActionListener( new CancelListener() );
			cancel.setFont( font );
			mainPanel.add( save, 12 );
			mainPanel.add( cancel, 13 );
			
			pFrame.add( mainPanel );
			try
			{
				pFrame.setIconImage( ImageIO.read(
						BigClock.class.getClassLoader().getResourceAsStream( "clock.png" ) ) );
			} catch ( IOException e )
			{
				e.printStackTrace();
			}
			mainPanel.setFont( new Font( "Time New Roman", Font.BOLD, 35 ) );
			
			useNetCheckbox.setSelected( true );
			useNetCheckbox.setSelected( useNetSettings );
			pFrame.setVisible( true );
		}
		
		private class NetCheckboxListener implements ItemListener
		{
			
			@Override
			public void itemStateChanged( ItemEvent e )
			{
				
				if ( e.getStateChange() == ItemEvent.SELECTED )
				{
					titleField.setEditable( false );
					extStateField.setEditable( false );
					refreshIntervalField.setEditable( false );
					queryIntervalField.setEditable( false );
					urlForSettingsField.setEditable( false );
				} else
				{
					titleField.setEditable( true );
					extStateField.setEditable( true );
					refreshIntervalField.setEditable( true );
					queryIntervalField.setEditable( true );
					urlForSettingsField.setEditable( true );
				}
			}
			
		}
		
		private class SaveListener implements ActionListener
		{
			
			@Override
			public void actionPerformed( ActionEvent e )
			{
				
				frame.setTitle( titleField.getText() );
				try
				{
					frame.setExtendedState( Integer.parseInt( extStateField.getText() ) );
				} catch ( NumberFormatException pE )
				{
					System.out.println( "Error retrieving integer from extended state field: " +
							pE.getMessage() );
				}
				useNetSettings = useNetCheckbox.isSelected();
				
				try
				{
					updateQueryInterval = Integer.parseInt( queryIntervalField.getText() );
				} catch ( NumberFormatException pE )
				{
					System.out.println( "Error retrieving integer from query interval field: " +
							pE.getMessage() );
				}
				
				try
				{
					refreshInterval = Long.parseLong( refreshIntervalField.getText() );
				} catch ( NumberFormatException pE )
				{
					System.out.println( "Error retrieving long from refresh interval field: " +
							pE.getMessage() );
				}
				
				settingsFileURL = urlForSettingsField.getText();
				
				if ( useNetSettings )
					useNetSettings();
				saveSettings( DEFAULT_FILEPATH );
				
				pFrame.dispose();
			}
			
		}
		
		private class CancelListener implements ActionListener
		{
			
			@Override
			public void actionPerformed( ActionEvent e )
			{
				
				pFrame.dispose();
			}
			
		}
	}
	
}
