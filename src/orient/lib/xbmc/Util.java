package orient.lib.xbmc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class Util {

	private static Util instance = null;

	protected Util() {
		// Exists only to defeat instantiation.
	}

	public static Util getInstance() {
		if (instance == null) {
			instance = new Util();
		}
		return instance;
	}
	


	public Map<String, String> CleanString (String fileName, boolean bRemoveExtension, boolean bCleanChars) {

		String strTitle = "";
		String strTitleAndYear = "";
		String strYear = "";
		
		Map<String, String> result = new HashMap<String, String>();
		
		strTitleAndYear = fileName;

		if (fileName.equals(".."))
			return result;

		Settings settings = Settings.getInstance();

		// videoCleanDateTimeRegExp
		
		String videoCleanDateTimeRegExp = settings.getVideoCleanDateTimeRegExp();
		
		Pattern pattern = Pattern.compile(videoCleanDateTimeRegExp);
		Matcher matcher = pattern.matcher(strTitleAndYear);

		if (matcher.find()) {
			strTitleAndYear = matcher.group(1);
			strYear =  matcher.group(2);
		}
		
		strTitleAndYear = FilenameUtils.removeExtension(strTitleAndYear);
		
		// videoCleanStringRegExps

		ArrayList<String> videoCleanStringRegExps = settings.getVideoCleanStringRegExps();

		for (String regex : videoCleanStringRegExps) {
			pattern = Pattern.compile(regex);
			matcher = pattern.matcher(strTitleAndYear);
			
			if (matcher.find()) {
				strTitleAndYear = strTitleAndYear.substring(0, matcher.start());
			}
		}
		
		
		
		// final cleanup - special characters used instead of spaces:
		// all '_' tokens should be replaced by spaces
		// if the file contains no spaces, all '.' tokens should be replaced by
		// spaces - one possibility of a mistake here could be something like:
		// "Dr..StrangeLove" - hopefully no one would have anything like this.
		if (bCleanChars)
		{
			boolean initialDots = true;
			boolean alreadyContainsSpace = strTitleAndYear.contains(" ");

			for (int i = 0; i < strTitleAndYear.length(); i++)
			{
				char c = strTitleAndYear.charAt(i);

				if (c != '.')
					initialDots = false;

				if ((c == '_') || ((!alreadyContainsSpace) && !initialDots && (c == '.')))
				{	
					strTitleAndYear = strTitleAndYear.substring(0,i)+' '+strTitleAndYear.substring(i+1);
				}
			}
		}
		
		
		strTitleAndYear = strTitleAndYear.trim();
		strTitle = strTitleAndYear;

		// append year
		if (strYear.length() != 0)
			strTitleAndYear = strTitle + " (" + strYear + ")";

		// restore extension if needed
		if (!bRemoveExtension)
			strTitleAndYear += "." + FilenameUtils.getExtension(fileName);
		
		result.put("title", strTitle);
		result.put("year", strYear);
		result.put("titleAndYear", strTitleAndYear);
		
		return result;
	}
	
	
	
}
