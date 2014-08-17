package com.orient.lib.xbmc.filesystem;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.orient.lib.xbmc.FileItem;
import com.orient.lib.xbmc.FileItemList;
import com.orient.lib.xbmc.Settings;
import com.orient.lib.xbmc.URL;
import com.orient.lib.xbmc.utils.URIUtils;


public class StackDirectory {

	/**
	 * Takes a stack string in the format
	 * "stack://file1 , file2 , file3 , file4" and returns path to file1. It is
	 * assumed that the files are already sorted in volume order, and hence only
	 * returns the first file. Filenames with commas should be double escaped
	 * (ie replaced with ,,), as " , " is used a separator.
	 * 
	 * @param strPath
	 * @return
	 */
	public static String getFirstStackedFile(String strPath) {
		ArrayList<String> paths = getPaths(strPath);

		if (paths.isEmpty())
			return "";

		return paths.get(0);
	}

	/**
	 * Extracts individual paths from a stack path.
	 * 
	 * @param stackPath
	 * @return
	 * 
	 * @see getFileItems
	 */
	public static ArrayList<String> getPaths(String stackPath) {

		// remove "stack://" from the folder
		if (stackPath.indexOf("stack://") == 0)
			stackPath = stackPath.substring(8);

		String[] files = stackPath.split(" , ");

		ArrayList<String> paths = new ArrayList<String>();
		for (String file : files) {
			file = StringUtils.replace(file, ",,", ",");
			paths.add(file);
		}

		return paths;
	}

	/**
	 * Create a Stack path with the format:
	 * "stack://file1 , file2 , file3 , file4" Takes FilesItemList and array of
	 * indexes that point to files. Filenames with commas are double escaped (ie
	 * replaced with ,,), thus the " , " separator used.
	 * 
	 * @param items
	 *            FileItemList of all the files in a directory
	 * @param stackIndexes
	 *            index of the relevant files.
	 * @return
	 */
	public static String constructStackPath(FileItemList items,
			int[] stackIndexes) {

		// no checks on the range of stack here.
		// we replace all instances of comma's with double comma's, then
		// separate the files using " , ".
		String stackedPath = "stack://";

		for (int i = 0; i < stackIndexes.length; ++i) {
			if (i != 0)
				stackedPath += " , ";

			String path = items.get(stackIndexes[i]).getPath();

			// double escape any occurrence of commas
			path = StringUtils.replace(path, ",", ",,");
			stackedPath += path;
		}
		return stackedPath;
	}

	public static String getStackedTitlePath(String stackPath) {

		// Load up our Regular Expressions
	    ArrayList<Pattern> patterns = new ArrayList<Pattern>();
	    
	    Settings settings = Settings.getInstance();
	    ArrayList<String> expressions = settings.getVideoStackRegExps();

	    for(String expression : expressions) {
	    	Pattern pattern = Pattern.compile(expression);
	    	
	    	try {
	    		patterns.add(pattern);
	    	}
	    	catch (PatternSyntaxException e) {
	    		//
	    	}
	    }
	    
	    // Set up variables
	    String strStackTitlePath = null;
	    String strCommonDir = URIUtils.getParentPath(stackPath);

	    ArrayList<String> files = getPaths(stackPath);
		
		if (files.size() <= 1)
			return null;
		
		
		
		String strStackTitle = null;

	    String File1 = URIUtils.getFileName(files.get(0));
	    String File2 = URIUtils.getFileName(files.get(1));

//	    // Check if source path uses URL encoding
//	    if (URIUtils::ProtocolHasEncodedFilename(CURL(strCommonDir).GetProtocol()))
//	    {
//	    	File1 = CURL::Decode(File1);
//	    	File2 = CURL::Decode(File2);
//	    }

	      int offset = 0;
	      
	      for (int i=0; i < expressions.size(); i++) {
//	      for (String expression : expressions) {
	    	  String expression = expressions.get(i);
	    	  
	    	  // Treating file 1
	    	  Pattern pattern = Pattern.compile(expression);
	    	  Matcher matcher = pattern.matcher(File1);
	    	  
	    	  if(matcher.find(offset)) {
	    		  String Title1 = matcher.group(1);
	    		  String Volume1 = matcher.group(2);
	    		  String Ignore1  = matcher.group(3);
	    		  String Extension1  = matcher.group(4);
	    		  
	    		  if (offset > 0)
	    			  Title1 = File1.substring(0, matcher.start(2));
	    		  
	    		  // Treating file 2
	    		  Pattern pattern2 = Pattern.compile(expression);
	    		  Matcher matcher2 = pattern2.matcher(File2);
//	    		  matcher.find(offset);
	    		  
	    		  if(matcher2.find(offset)) {
	    			  String Title2 = matcher2.group(1);
	    			  String Volume2 = matcher2.group(2);
	    			  String Ignore2  = matcher2.group(3);
	    			  String Extension2  = matcher2.group(4);
	    			  
	    			  if (offset > 0)
	    				  Title2 = File2.substring(0, matcher2.start(2));
	    			  
	    			  
	    			  // Compare findings
	    			  if (Title1.equals(Title2))
	    			  {
	    				  if (!Volume1.equals(Volume2))
	    				  {
	    					  if (Ignore1.equals(Ignore2) && Extension1.equals(Extension2))
	    					  {
	    						  // got it
	    						  strStackTitle = Title1 + Ignore1 + Extension1;
	    						  
//	    						  // Check if source path uses URL encoding
//	    						  if (URIUtils::ProtocolHasEncodedFilename(CURL(strCommonDir).GetProtocol()))
//	    							  strStackTitle = CURL::Encode(strStackTitle);

	    						  break;
	    					  }
	    					  else // Invalid stack
	    						  break;
	    				  }
	    				  else // Early match, retry with offset
	    				  {
	    					  offset = matcher.start(3);
	    					  i--;
	    					  continue;
	    				  }
	    			  }
	    		  }
	    	  }
	    	  
	    	  offset = 0;
	      }
	      
	      if (!strCommonDir.isEmpty() && !strStackTitle.isEmpty())
	          strStackTitlePath = strCommonDir + strStackTitle;
	      
	      return strStackTitlePath;
	}

	/**
	 * Same as getFileItems. Only exists to avoid XBMC breakages.
	 * Use other function instead.
	 * 
	 * @param stackPath
	 * @return
	 */
	public static ArrayList<FileItem> getDirectory(String stackPath) {
		return getFileItems(stackPath);
	}
	
	
	public static ArrayList<FileItem> getDirectory(URL url)
	  { 
		
		// TODO implement
		return getFileItems(url.Get());
//	    ArrayList<String> files;
//	    String pathToUrl = url.Get();
//	    if (!getPaths(pathToUrl, files))
//	      return false;   // error in path
//
//	    for (vector<std::string>::const_iterator i = files.begin(); i != files.end(); ++i)
//	    {
//	      CFileItem item(new CFileItem(*i));
//	      item->SetPath(*i);
//	      item->m_bIsFolder = false;
//	      items.Add(item);
//	    }
//	    return true;
	  }
	
	/**
	 * Takes a Stack path, breaks it into individual file paths and 
	 * returns corresponding FileItem objects.
	 * 
	 * @param stackPath
	 * @return
	 * 
	 * @see getFileItems, getPaths
	 */
	public static ArrayList<FileItem> getFileItems(String stackPath) {
		ArrayList<String> paths = getPaths(stackPath);
		ArrayList<FileItem> items = new ArrayList<FileItem>();
		
		for(String path : paths) {
			items.add(new FileItem(path, false));
		}
		
		return items;
	}
}
