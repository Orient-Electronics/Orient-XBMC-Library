package orient.lib.xbmc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import orient.lib.xbmc.utils.URIUtils;

public class Util {
	public static Map<String, String> CleanString (String fileName, boolean bRemoveExtension, boolean bCleanChars) {

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

	public static String validatePath(String path) {
		return validatePath(path, false);
	}
	
	// TODO test
	public static String validatePath(String path, boolean bFixDoubleSlashes) {
		String result = path;

		// Don't do any stuff on URLs containing %-characters or protocols that embed
		// filenames. NOTE: Don't use IsInZip or IsInRar here since it will infinitely
		// recurse and crash XBMC
		if (URIUtils.isURL(path) && 
				(path.indexOf('%') > -1 ||
						StringUtils.startsWithIgnoreCase(path, "apk:") ||
						StringUtils.startsWithIgnoreCase(path, "zip:") ||
						StringUtils.startsWithIgnoreCase(path, "rar:") ||
						StringUtils.startsWithIgnoreCase(path, "stack:") ||
						StringUtils.startsWithIgnoreCase(path, "bluray:") ||
						StringUtils.startsWithIgnoreCase(path, "multipath:") ))
			return result;

		// check the path for incorrect slashes
		//		if (SystemUtils.IS_OS_WINDOWS) {
		if (URIUtils.isDOSPath(path))
		{
			result = StringUtils.replace(result, "/", "\\");

			/* The double slash correction should only be used when *absolutely*
		       necessary! This applies to certain DLLs or use from Python DLLs/scripts
		       that incorrectly generate double (back) slashes.
			 */

			if (bFixDoubleSlashes && !result.isEmpty())
			{
				// Fixup for double back slashes (but ignore the \\ of unc-paths)
				for (int x = 1; x < result.length() - 1; x++)
				{
					if (result.charAt(x) == '\\' && result.charAt(x+1) == '\\')
						result.substring(0, x-1);
				}
			}
		}
		else if (path.indexOf("://") > -1 || path.indexOf(":\\\\") > -1)

		{
			result = StringUtils.replace(result, "\\", "/");

			/* The double slash correction should only be used when *absolutely*
		       necessary! This applies to certain DLLs or use from Python DLLs/scripts
		       that incorrectly generate double (back) slashes.
			 */

			if (bFixDoubleSlashes && !result.isEmpty())
			{
				// Fixup for double forward slashes(/) but don't touch the :// of URLs
				for (int x = 1; x < result.length() - 1; x++)
				{
					if ( result.charAt(x) == '/' && result.charAt(x+1) == '/' && !(result.charAt(x-1) == ':' || (result.charAt(x-1) == '/' && result.charAt(x-2) == ':')) )
						result.substring(0, x-1);
				}
			}
		}
		return result;
	}

	public static boolean excludeFileOrFolder(String strFileOrFolder,
			ArrayList<String> regexps) {
		
		if (strFileOrFolder.isEmpty())
		    return false;
		
		for (String regex : regexps) {
			Pattern pattern = Pattern.compile(regex);			
			Matcher matcher = pattern.matcher(strFileOrFolder);
			
			if (matcher.find())
				return true;
		}
		
		return false;
	}
	
	
	
}
