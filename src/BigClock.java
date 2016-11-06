import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

public class BigClock
{
	
	public final static String DELIMITER = "::";
	private static String settingsFileURL = "http://nateshot.homenet.org:8025/clock_settings.txt";
	private final static float VERSION_NUMBER = 2.5f;
	private final static String DEFAULT_TITLE = "CLOCK v" + VERSION_NUMBER;
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
	private int updateQueryInterval = 30;
	private long refreshInterval = 200;
	private boolean useNetSettings = true;
	
	private ClockSettings defSettings, fileSettings, netSettings;
	
	public BigClock() throws IOException
	{
		/*
		 * Create window and initialize clock.
		 */
		frame = new JFrame( DEFAULT_TITLE );
		frame.setSize( 660, 500 );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		
		buildMenuBar();
		
		main = new JPanel();
		
		// BigClock.setUIFont( new FontUIResource( new Font( "Arial", 0, 40 ) )
		// );
		
		timeLabel = new JLabel();
		dateLabel = new JLabel();
		container = new JPanel();
		timeLabel.setFont( new Font( "Arial", Font.BOLD, 150 ) );
		dateLabel.setFont( new Font( "Arial", Font.BOLD, 40 ) );
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
		
		defSettings = new ClockSettings();
		fileSettings = new ClockSettings();
		netSettings = new ClockSettings();
		
		loadFileSettings();
		
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
				// No need to update settings at a regular interval if file
				// settings are being used as they are loaded at start and they
				// can only be changed by the user when use-net is not selected.
				if ( useNetSettings )
				{
					useNetSettings();
					updateSettingsCounter = 0;
				}
			}
			
			updateSettingsCounter++;
		}
	}
	
	private void useNetSettings()
	{
		
		// upate the net settings before updating the settings
		loadNetSettings();
		updateSettings( netSettings );
		
		return;
	}
	
	private void useFileSettings()
	{
		
		updateSettings( fileSettings );
		
		return;
	}
	
	private void loadFileSettings()
	{
		
		// Load file settings
		try
		{
			// use default file path
			Scanner settings = new Scanner( new File( DEFAULT_FILEPATH ) );
			
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
						
						fileSettings.setBackgroundColor( newColor );
						continue;
					case "font-color":
						/*
						 * Extract and set the color
						 */
						// gets the numbers separated by commas
						tempString = selector[1].substring( 1, selector[1].length() - 1 );
						
						newColor = verifyColor( tempString );
						
						fileSettings.setForegroundColor( newColor );
						continue;
					
					case "title":
						fileSettings.setTitle( respace( selector[1] ) );
						continue;
					
					case "extended-state":
						fileSettings.setExtendedState( Integer.parseInt( selector[1] ) );
						continue;
					
					case "query-interval":
						fileSettings.setQueryInterval( Integer.parseInt( selector[1] ) );
						continue;
					
					case "refresh-interval":
						fileSettings.setRefreshInterval( Long.parseLong( selector[1] ) );
						continue;
					
					case "settings-url":
						fileSettings.setNetURL( selector[1] );
						continue;
					
					case "use-net-settings":
						fileSettings.setUseNetSettings( Boolean.parseBoolean( selector[1] ) );
						continue;
					
					default:
						continue;
				}
			}
			
			settings.close();
			
		} catch ( NumberFormatException e )
		{
			System.out.println( "Error: " + e.getMessage() );
			frame.setTitle( "CLOCK - Parse Error in Settings Load (file)" );
		} catch ( FileNotFoundException ef )
		{
			
			// The file does not exist so we create it using the default
			// settings.
			PrintWriter fileOut;
			try
			{
				fileOut = new PrintWriter( new File( DEFAULT_FILEPATH ) );
				fileOut.println( defSettings.toString() );
				fileOut.close();
			} catch ( FileNotFoundException e )
			{
				System.out.println( "Creating the settings file failed... \n" + e.getMessage() );
			}
		} catch ( Exception e )
		{
			System.out.println(
					"The clock has encoutered an unexpected error retrieving the settings file..." );
			System.out.println( e.getMessage() );
			e.printStackTrace();
			frame.setTitle( "CLOCK - Unexpected Error in Settings Load (file)" );
		}
		
		return;
		
	}
	
	private boolean loadNetSettings()
	{
		
		/*
		 * BEGIN LOADING NET SETTINGS
		 */
		try
		{
			// use default file path
			Scanner settings = new Scanner( new URL( settingsFileURL ).openStream() );
			
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
						
						netSettings.setBackgroundColor( newColor );
						continue;
					case "font-color":
						/*
						 * Extract and set the color
						 */
						// gets the numbers separated by commas
						tempString = selector[1].substring( 1, selector[1].length() - 1 );
						
						newColor = verifyColor( tempString );
						
						netSettings.setForegroundColor( newColor );
						continue;
					
					case "title":
						netSettings.setTitle( respace( selector[1] ) );
						continue;
					
					case "extended-state":
						netSettings.setExtendedState( Integer.parseInt( selector[1] ) );
						continue;
					
					case "query-interval":
						netSettings.setQueryInterval( Integer.parseInt( selector[1] ) );
						continue;
					
					case "refresh-interval":
						netSettings.setRefreshInterval( Long.parseLong( selector[1] ) );
						continue;
					
					case "settings-url":
						netSettings.setNetURL( selector[1] );
						continue;
					
					case "use-net-settings":
						netSettings.setUseNetSettings( Boolean.parseBoolean( selector[1] ) );
						continue;
					
					default:
						continue;
				}
			}
			settings.close();
			
		} catch ( NumberFormatException e )
		{
			System.out.println( "Error: " + e.getMessage() );
			frame.setTitle( "CLOCK - Parse Error in Settings Load (net)" );
			
			return false;
		} catch ( Exception e )
		{
			System.out.println(
					"The clock has encoutered an unexpected error retrieving the settings file..." );
			System.out.println( e.getMessage() );
			e.printStackTrace();
			frame.setTitle( "CLOCK - Unexpected Error in Settings Load (net)" );
			
			return false;
		}
		return true;
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
		
		// Save settings from fileSettings only because we don't want the net
		// settings overwriting the text file.
		try
		{
			PrintWriter fileOut = new PrintWriter( new File( filePath ) );
			
			fileOut.println( fileSettings.toString() );
			fileOut.close();
		} catch ( FileNotFoundException e )
		{
			System.out.println( "Unable to save settings to the file path: " + filePath + "\n" );
			System.out.println( e.getMessage() );
		}
		
		return;
	}
	
	private void updateSettings( ClockSettings cs )
	{
		
		this.setBackgroundColor( cs.getBackgroundColor() );
		this.setTextColor( cs.getForegroundColor() );
		frame.setTitle( respace( cs.getTitle() ) );
		frame.setExtendedState( cs.getExtendedState() );
		this.updateQueryInterval = cs.getQueryInterval();
		this.refreshInterval = cs.getRefreshInterval();
		settingsFileURL = cs.getNetURL();
		this.useNetSettings = cs.getUseNetSettings();
		return;
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
			
			if ( newColor == null )
				return; // If the user presses "cancel"
				
			if ( e.getSource() == bgColorItem )
			{
				// Handle Background Color and return
				fileSettings.setBackgroundColor( newColor );
				
				if ( !fileSettings.getUseNetSettings() )
					useFileSettings();
				saveSettings( DEFAULT_FILEPATH );
				return;
			}
			
			// Handle FG color and return
			fileSettings.setForegroundColor( newColor );
			
			if ( !fileSettings.getUseNetSettings() )
				useFileSettings();
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
			
			font = new Font( "Arial", Font.PLAIN, 16 );
			
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
			titleField.setText( fileSettings.getTitle() );
			titleField.setToolTipText( "Title For Clock GUI" );
			titleField.setFont( font );
			mainPanel.add( titleLabel, 2 );
			mainPanel.add( titleField, 3 );
			
			extStateLabel = new JLabel( "Extended State:" );
			extStateLabel.setToolTipText(
					"NORMAL=0, ICONIFIED=1, MAXIMIZED_HORIZ=2, MAXIMIZED_VERT=4, MAXIMIZED_BOTH=6" );
			extStateLabel.setFont( font );
			extStateField = new JTextField( 20 );
			extStateField.setText( String.valueOf( fileSettings.getExtendedState() ) );
			extStateField.setToolTipText(
					"NORMAL=0, ICONIFIED=1, MAXIMIZED_HORIZ=2, MAXIMIZED_VERT=4, MAXIMIZED_BOTH=6" );
			extStateField.setFont( font );
			mainPanel.add( extStateLabel, 4 );
			mainPanel.add( extStateField, 5 );
			
			refreshIntervalLabel = new JLabel( "Refresh Interval:" );
			refreshIntervalLabel.setToolTipText( "Interval of refreshing date/time" );
			refreshIntervalLabel.setFont( font );
			refreshIntervalField = new JTextField( 20 );
			refreshIntervalField.setText( String.valueOf( fileSettings.getRefreshInterval() ) );
			refreshIntervalField.setToolTipText( "Interval of refreshing date/time" );
			refreshIntervalField.setFont( font );
			mainPanel.add( refreshIntervalLabel, 6 );
			mainPanel.add( refreshIntervalField, 7 );
			
			urlForSettingsLabel = new JLabel( "URL Settings File:" );
			urlForSettingsLabel.setToolTipText( "URL to fetch settings from" );
			urlForSettingsLabel.setFont( font );
			urlForSettingsField = new JTextField();
			urlForSettingsField.setText( fileSettings.getNetURL() );
			urlForSettingsField.setToolTipText( "URL to fetch settings from" );
			urlForSettingsField.setFont( font );
			mainPanel.add( urlForSettingsLabel, 8 );
			mainPanel.add( urlForSettingsField, 9 );
			
			queryIntervalLabel = new JLabel( "Query Interval:" );
			queryIntervalLabel.setToolTipText( "Interval to update settings from online file" );
			queryIntervalLabel.setFont( font );
			queryIntervalField = new JTextField( 20 );
			queryIntervalField.setText( String.valueOf( fileSettings.getQueryInterval() ) );
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
			useNetCheckbox.setSelected( fileSettings.getUseNetSettings() );
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
					queryIntervalField.setEditable( true );
					urlForSettingsField.setEditable( true );
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
				
				fileSettings.setTitle( titleField.getText() );
				try
				{
					fileSettings.setExtendedState( Integer.parseInt( extStateField.getText() ) );
				} catch ( NumberFormatException pE )
				{
					System.out.println( "Error retrieving integer from extended state field: " +
							pE.getMessage() );
				}
				fileSettings.setUseNetSettings( useNetCheckbox.isSelected() );
				
				try
				{
					fileSettings
							.setQueryInterval( Integer.parseInt( queryIntervalField.getText() ) );
				} catch ( NumberFormatException pE )
				{
					System.out.println( "Error retrieving integer from query interval field: " +
							pE.getMessage() );
				}
				
				try
				{
					fileSettings
							.setRefreshInterval( Long.parseLong( refreshIntervalField.getText() ) );
				} catch ( NumberFormatException pE )
				{
					System.out.println( "Error retrieving long from refresh interval field: " +
							pE.getMessage() );
				}
				
				fileSettings.setNetURL( urlForSettingsField.getText() );
				
				if ( fileSettings.getUseNetSettings() )
					useNetSettings();
				else
					useFileSettings();
				
				// disabling would not allow someone to uncheck using net
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
	
	private class ClockSettings
	{
		
		private String title;
		private String netURL;
		private Color backgroundColor;
		private Color foregroundColor;
		private int queryInterval;
		private long refreshInterval;
		private int extendedState;
		private boolean useNetSettings;
		
		public ClockSettings()
		{
			this.title = DEFAULT_TITLE;
			this.netURL = settingsFileURL;
			this.backgroundColor = new Color( 45, 54, 45 );
			this.foregroundColor = new Color( 155, 172, 134 );
			this.queryInterval = 60;
			this.refreshInterval = 500;
			this.extendedState = 6;
			this.useNetSettings = true;
		}
		
		public void setTitle( String title )
		{
			
			this.title = title;
		}
		
		public void setBackgroundColor( Color bg )
		{
			
			this.backgroundColor = bg;
		}
		
		public void setForegroundColor( Color fg )
		{
			
			this.foregroundColor = fg;
		}
		
		public void setQueryInterval( int qinterval )
		{
			
			if ( qinterval > 0 )
				this.queryInterval = qinterval;
		}
		
		public void setRefreshInterval( long refreshInterval )
		{
			
			if ( refreshInterval > 0 )
				this.refreshInterval = refreshInterval;
		}
		
		public void setExtendedState( int extState )
		{
			
			this.extendedState = extState;
		}
		
		public void setUseNetSettings( boolean useNet )
		{
			
			this.useNetSettings = useNet;
		}
		
		public void setNetURL( String url )
		{
			
			this.netURL = url;
		}
		
		public String getTitle()
		{
			
			return this.title;
		}
		
		public Color getBackgroundColor()
		{
			
			return this.backgroundColor;
		}
		
		public Color getForegroundColor()
		{
			
			return this.foregroundColor;
		}
		
		public int getQueryInterval()
		{
			
			return this.queryInterval;
		}
		
		public long getRefreshInterval()
		{
			
			return this.refreshInterval;
		}
		
		public boolean getUseNetSettings()
		{
			
			return this.useNetSettings;
		}
		
		public String getNetURL()
		{
			
			return this.netURL;
		}
		
		public int getExtendedState()
		{
			
			return this.extendedState;
		}
		
		public String toString()
		{
			
			return "background-color" + BigClock.DELIMITER + "(" + this.backgroundColor.getRed() +
					"," + this.backgroundColor.getGreen() + "," + this.backgroundColor.getBlue() +
					")\n" + "font-color" + BigClock.DELIMITER + "(" +
					this.foregroundColor.getRed() + "," + this.foregroundColor.getGreen() + "," +
					this.foregroundColor.getBlue() + ")\n" + "title" + BigClock.DELIMITER +
					despace( this.title ) + "\nextended-state" + BigClock.DELIMITER +
					this.extendedState + "\nquery-interval" + BigClock.DELIMITER +
					this.queryInterval + "\nrefresh-interval" + BigClock.DELIMITER +
					this.refreshInterval + "\nsettings-url" + BigClock.DELIMITER + settingsFileURL +
					"\nuse-net-settings" + BigClock.DELIMITER + this.useNetSettings;
		}
	}
}
