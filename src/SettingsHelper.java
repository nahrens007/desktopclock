import java.awt.Color;

public abstract class SettingsHelper
{
	public final static float VERSION_NUMBER = 2.6f;
	public final static String DEFAULT_TITLE = "CLOCK v" + VERSION_NUMBER;
	public final static String DEFAULT_FILEPATH = "clock_settings";
	public final static String DELIMITER = "::";
	/**
	 * This method will replace all '%' symbols in a string with a single blank
	 * space.
	 * 
	 * @param deblankedString
	 *            The string to be re-spaced.
	 * @return A string void of any '%' symbols, which have been replaced by a
	 *         space.
	 */
	public static String respace( String deblankedString )
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
	
	public static String despace( String spacedString )
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
	public static Color verifyColor( String color )
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
}
