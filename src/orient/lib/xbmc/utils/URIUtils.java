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
	 * 
	 * @param basePath
	 * @param fullFilenNmeToAdd
	 * @return
	 */
	public static String addFileToFolder(String basePath,
			String fullFileNameToAdd) {

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
		return FilenameUtils.getBaseName(path) + "."
				+ FilenameUtils.getExtension(path);
	}

	
	public static String getParentPath(String strPath) {
		String strParent = "";

		orient.lib.xbmc.URL url = new orient.lib.xbmc.URL(strPath);
		String strFile = url.GetFileName();

		if ( URIUtils.protocolHasParentInHostname(url.GetProtocol()) && strFile.isEmpty())
		{
			strFile = url.GetHostName();
			return getParentPath(strFile);
		}
		else if (url.GetProtocol() != null && url.GetProtocol().equals("stack"))
		{

			// TODO temp fix
			FileItem fileItem = new FileItem(strPath, false);
			if (fileItem.isStack()) {
				ArrayList<String> paths = StackDirectory.getPaths(strPath);

				if (paths.isEmpty())
					return null;

				return getParentPath(paths.get(0));
			}

			//		   StackDirectory dir = new StackDirectory();
			//		    ArrayList<FileItem> items =  dir.getDirectory(url);
			//		    items.get(0).m_strDVDLabel = GetDirectory(items[0]->GetPath());
			//		    if (StringUtils::StartsWithNoCase(items[0]->m_strDVDLabel, "rar://") || StringUtils::StartsWithNoCase(items[0]->m_strDVDLabel, "zip://"))
			//		      GetParentPath(items[0]->m_strDVDLabel, strParent);
			//		    else
			//		      strParent = items[0]->m_strDVDLabel;
			//		    for( int i=1;i<items.Size();++i)
			//		    {
			//		      items[i]->m_strDVDLabel = GetDirectory(items[i]->GetPath());
			//		      if (StringUtils::StartsWithNoCase(items[0]->m_strDVDLabel, "rar://") || StringUtils::StartsWithNoCase(items[0]->m_strDVDLabel, "zip://"))
			//		        items[i]->SetPath(GetParentPath(items[i]->m_strDVDLabel));
			//		      else
			//		        items[i]->SetPath(items[i]->m_strDVDLabel);
			//
			//		      GetCommonPath(strParent,items[i]->GetPath());
			//		    }
			//		    return true;
		}
		else if (url.GetProtocol() != null && url.GetProtocol().equals("multipath"))
		{
			// get the parent path of the first item
			//		    return getParentPath(CMultiPathDirectory::GetFirstPath(strPath), strParent);
		}
		else if (url.GetProtocol() != null && url.GetProtocol().equals("plugin"))
		{

		}
		else if (url.GetProtocol() != null && url.GetProtocol().equals("special"))
		{
			if (hasSlashAtEnd(strFile))
				strFile.substring(strFile.length() - 1);
			if(strFile.indexOf('/') == -1 && strFile.indexOf('\\') == -1)
				return null;
		}
		else if (strFile == null || strFile.length() == 0)
		{
			if (url.GetHostName().length() > 0)
			{
				// we have an share with only server or workgroup name
				// set hostname to "" and return true to get back to root
				url.SetHostName("");
				return url.Get();
			}
			return null;
		}

		if (hasSlashAtEnd(strFile) )
		{
			strFile = strFile.substring(0, strFile.length() - 1);
		}

		int iPos = -1;
		
		if (strFile != null) {
			iPos = strFile.lastIndexOf('/');

			if (iPos == -1)
			{
				iPos = strFile.lastIndexOf('\\');
			}
		}

		if (iPos == -1)
		{
			url.setFileName("");
			return url.Get();
		}

		strFile = strFile.substring(0, iPos);

		strFile = AddSlashAtEnd(strFile);

		url.setFileName(strFile);

		return url.Get();

	}
	
	public static String AddSlashAtEnd(String strFolder)
	{
		if (isURL(strFolder))
		{
			orient.lib.xbmc.URL url = new orient.lib.xbmc.URL(strFolder);

			String file = url.GetFileName();

			if(file != null && !file.isEmpty() && file != strFolder)
			{
				file =  AddSlashAtEnd(file);
				url.setFileName(file);
			}
			return url.Get();
		}

		if (!hasSlashAtEnd(strFolder))
		{
			if (isDOSPath(strFolder))
				strFolder += '\\';
			else
				strFolder += '/';
		}
		
		return strFolder;
	}

	public static boolean hasExtension(String strFileName, String strExtensions) {
		String[] extensions = StringUtils.split(strExtensions, "|");

		return hasExtension(strFileName, extensions);
	}

	public static boolean hasExtension(String strFileName, String[] extensions) {

		for (int i = 0; i < extensions.length; i++) {
			if (extensions[i].charAt(0) == '.')
				extensions[i] = extensions[i].substring(1);
		}

		return FilenameUtils.isExtension(strFileName, extensions);
	}

	public static boolean hasSlashAtEnd(String strFile) {
		return hasSlashAtEnd(strFile, false);
	}

	
	public static boolean hasSlashAtEnd(String strFile, boolean checkURL) {
		
		if (strFile == null)
			return false;
		
		if (strFile.isEmpty())
			return false;

		if (checkURL && isURL(strFile)) {
			String file = getFileName(strFile);
			return file.isEmpty() || hasSlashAtEnd(file, false);
		}

		char kar = strFile.charAt(strFile.length() - 1);

		if (kar == '/' || kar == '\\')
			return true;

		return false;
	}

	public static boolean isInAPK(String strFile) {
		URI uri;

		try {
			uri = new URI(strFile);
		} catch (URISyntaxException e) {
			return false;
		}

		File file = new File(strFile);

		return uri.getScheme().equals("apk") && !file.getName().isEmpty();
	}

	public static boolean isInArchive(String strFile) {
		return isInZIP(strFile) || isInRAR(strFile) || isInAPK(strFile);
	}

	public static boolean isInRAR(String strFile) {

		if (strFile == null)
			return false;
		
		URI uri;

		try {
			uri = new URI(strFile);
		} catch (URISyntaxException e) {
			return false;
		}

		File file = new File(strFile);

		return uri.getScheme().equals("rar") && !file.getName().isEmpty();
	}

	public static boolean isInZIP(String strFile) {
		URI uri;

		try {
			uri = new URI(strFile);
		} catch (URISyntaxException e) {
			return false;
		}

		File file = new File(strFile);

		return uri.getScheme().equals("zip") && !file.getName().isEmpty();
	}

	public static boolean isStack(String strFile) {
		return StringUtils.startsWithIgnoreCase(strFile, "stack:");
	}

	public static boolean isURL(String strFile) {
		return strFile.indexOf("://") > -1;
	}

	public static boolean protocolHasParentInHostname(String prot) {
		
		if (prot == null)
			return false;
		
		return prot.equals("zip") || prot.equals("rar") || prot.equals("apk")
				|| prot.equals("bluray") || prot.equals("udf");
	}

	public static String removeExtension(String filename) {
		return FilenameUtils.removeExtension(filename);
	}

	public static String removeSlashAtEnd(String strPath) {
		
		if (strPath == null || strPath.isEmpty())
			return strPath;
		
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

	
	// TODO test
	public static boolean isDOSPath(String path) {
		if (path.length() > 1 && path.charAt(1) == ':' && Character.isLetter(path.charAt(0)))
		    return true;

		  // windows network drives
		  if (path.length() > 1 && path.charAt(0) == '\\' && path.charAt(1) == '\\')
		    return true;

		  return false;
	}

	public static boolean protocolHasEncodedHostname(String prot) {
		return protocolHasParentInHostname(prot)
			      || prot.equals("musicsearch")
			      || prot.equals("image");
	}
}
