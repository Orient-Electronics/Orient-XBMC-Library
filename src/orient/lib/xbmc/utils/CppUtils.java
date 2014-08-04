package orient.lib.xbmc.utils;

import org.apache.commons.lang3.StringUtils;

public class CppUtils {
	/**
	 * <h1>fuzzy string compare</h1>
	 * 
	 * <p>The fstrcmp function may be used to compare two string for
	 * similarity. It is very useful in reducing "cascade" or "secondary" errors
	 * in compilers or other situations where symbol tables occur.</p>
	 * 
	 * @return double; 0 if the strings are entirly dissimilar, 1 if the strings
	 * are identical, and a number in between if they are similar.
	 */

	public static double 
	fstrcmp (String string1, String string2, double minimum)
	{
	  int len1, len2, score;

	  len1 = string1.length();
	  len2 = string2.length();

	  /* short-circuit obvious comparisons */
	  if (len1 == 0 && len2 == 0)
	    return 1.0;
	  if (len1 == 0 || len2 == 0)
	    return 0.0;

		score = Math.max(string1.length(), string2.length())
				- StringUtils.getLevenshteinDistance(string1, string2);

	  /* The result is
	  ((number of chars in common) / (average length of the strings)).
	     This is admittedly biased towards finding that the strings are
	     similar, however it does produce meaningful results.  */
	  return ((double)score * 2.0 / (len1 + len2));
	}
}
