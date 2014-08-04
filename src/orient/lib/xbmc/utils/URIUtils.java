package orient.lib.xbmc.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import orient.lib.xbmc.filesystem.StackDirectory;
import orient.lib.xbmc.FileItem;


public class URIUtils {


	/**
	 * Appends a path/uri with a given scheme
	 * @param basePath
	 * @param fullFilenNmeToAdd
	 * @return
	 */
	public static String addFileToFolder(String basePath, String fullFileNameToAdd) {

		try {

			URI uri = new URI(basePath);
			URI uri2 = uri.resolve(fullFileNameToAdd);
			return uri2.toString();
		} catch (URISyntaxException e) {
			return FilenameUtils.concat(basePath, fullFileNameToAdd);
		}

	}

	/**
	 * Will from a full filename return the directory the file resides in. Keeps
	 * the final slash at end and possible |option=foo options.
	 * 
	 * @param strFilePath
	 * @return
	 */
	public static String getDirectory(String strFilePath) {

		String path = FilenameUtils.getFullPath(strFilePath);

		int options = StringUtils.lastIndexOf(strFilePath, "|");

		if (options > -1)
			path += strFilePath.substring(options);

		return path;
	}

	public static String getFileName(String path) {
		return FilenameUtils.getBaseName(path) + "." + FilenameUtils.getExtension(path);
	}


	/**
	 * Takes a path and return it's parent path.
	 * Different from the original, may have lots of loop holes.
	 * 
	 * @param strPath
	 * @return
	 */
	public static String getParentPath(String strPath) {

		
		
		URL url;

		try {
			url = new URL(strPath);
			String strFile = url.getFile();


			if (URIUtils.protocolHasParentInHostname(url.getProtocol()) && strFile.isEmpty()) {
				strFile = url.getHost();
				return getParentPath(strFile);
			}

			
		} catch (MalformedURLException e1) {
			
			// Stack
			FileItem fileItem = new FileItem(strPath, false);
			if (fileItem.isStack()) {
				ArrayList<String> paths = StackDirectory.getPaths(strPath);
				
				if (paths.isEmpty())
					return null;
				
				return getDirectory(paths.get(0));			
			}			
		}
		
			
			
		// Custom
		URI uri;
		
		try {
			uri = new URI(strPath);
		} catch (URISyntaxException e) {
			return "";
		}
		
//		String strParent = "";
		
		String path = uri.getPath();
		
		if ((path == null) || path.equals("") || path.equals("/"))
		{
			return uri.getScheme() + "://";
		}
		
		uri = uri.resolve("..");
		return uri.toString();
	}


	public static boolean hasSlashAtEnd(String strFile, boolean checkURL /* = false */)
	{
		if (strFile.isEmpty()) 
			return false;

		if (checkURL && isURL(strFile))
		{
			String file = getFileName(strFile);
			return file.isEmpty() || hasSlashAtEnd(file, false);
		}

		char kar =  strFile.charAt(strFile.length() - 1);

		if (kar == '/' || kar == '\\')
			return true;

		return false;
	}

	public static boolean isURL(String strFile)
	{
		return strFile.indexOf("://") > -1;
	}

	public static boolean protocolHasParentInHostname(String prot)
	{
		return prot.equals("zip")
				|| prot.equals("rar")
				|| prot.equals("apk")
				|| prot.equals("bluray")
				|| prot.equals("udf");
	}

	public static boolean hasExtension(String strFileName, String strExtensions) {
		String[] extensions = StringUtils.split(strExtensions, "|");

		return hasExtension(strFileName, extensions);
	}

	public static boolean hasExtension(String strFileName, String[] extensions) {

		for (int i=0; i < extensions.length; i++) {
			if (extensions[i].charAt(0) == '.')
				extensions[i] = extensions[i].substring(1);
		}

		return FilenameUtils.isExtension(strFileName, extensions);
	}

	public static boolean isInRAR(String strFile) {
		URI uri;

		try {
			uri = new URI(strFile);
		} catch (URISyntaxException e) {
			return false;
		}

		File file = new File(strFile);

		return uri.getScheme().equals("rar") && !file.getName().isEmpty();
	}

	public static boolean isStack(String strFile)
	{
		return StringUtils.startsWithIgnoreCase(strFile, "stack:");
	}

	public static String removeExtension(String filename) {
		return FilenameUtils.removeExtension(filename);
	}

	public static String removeSlashAtEnd(String strPath) {
		char tail = strPath.charAt(strPath.length() - 1);
		
		if (tail == '/' || tail == '\\')
			return FilenameUtils.getFullPathNoEndSeparator(strPath);
		
		return strPath;
	}

	public static String replaceExtension(String filename, String extension) {
		filename = FilenameUtils.removeExtension(filename);

		if (extension.charAt(0) == '.')
			extension = extension.substring(1);
		
		return filename + "." + extension;
	}

	/**
	 * Splits a path into folder path and file name.
	 * 
	 * @param strFileNameAndPath
	 * @return A string array. Index 0 contains folder path and index 1 contains
	 *         file name.
	 */
	public static String[] split(String path) {
		return splitFolderAndFilePath(path);
	}

	/**
	 * Splits a path into folder path and file name.
	 * 
	 * @param strFileNameAndPath
	 * @return A string array. Index 0 contains folder path and index 1 contains
	 *         file name.
	 */
	private static String[] splitFolderAndFilePath(String path) {

		int splitIndex = path.lastIndexOf('/') > -1 ? path.lastIndexOf('/')
				: path.lastIndexOf('\\');

		if (splitIndex == -1 && path.lastIndexOf(':') == 1)
			splitIndex = 1;

		if (splitIndex == 0)
			splitIndex--;

		String[] result = new String[2];

		result[0] = path.substring(0, splitIndex + 1);
		result[1] = path.substring(splitIndex + 1);

		return result;
	}
}
