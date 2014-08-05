package orient.lib.xbmc;

import java.io.File;
import java.net.URLDecoder;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import orient.lib.xbmc.MediaSource.SourceType;
import orient.lib.xbmc.filesystem.StackDirectory;
import orient.lib.xbmc.utils.MimeTypes;
import orient.lib.xbmc.utils.URIUtils;
import orient.lib.xbmc.video.VideoInfoTag;
import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class FileItem {

	/**
	 * complete path to item
	 */
	private String path;

	// SortSpecial specialSort;
	private boolean isParentFolder;
	private boolean canQueue;
	private boolean labelPreformated;
	private String mimetype;
	private String extrainfo;
	// private MusicInfoTag musicInfoTag;
	private VideoInfoTag videoInfoTag;
	private boolean isAlbum;

	/**
	 * is this a root share/drive
	 */
	private boolean isShareOrDrive;

	/**
	 * If isShareOrDrive is true, use to get the share type. Types see:
	 * MediaSource.driveType
	 */
	private SourceType driveType;

	/**
	 * file creation date & time
	 */
	private Date dateTime;

	/**
	 * file size (0 for folders)
	 */
	private int dwSize;
	private String DVDLabel;
	private String title;
	private int programCount;
	private int depth;
	private int startOffset;
	private int startPartNumber;
	private int endOffset;

	private boolean isFolder;
	
	/**
	 * @param path
	 * @param isFolder
	 */
	public FileItem(String path, boolean isFolder) {
		initialize();
		this.path = path;
		this.isFolder = isFolder;

		fillInMimeType();
	}

	public FileItem(VideoInfoTag movie) {
		initialize();
		setFromVideoInfoTag(movie);
	}

	/**
	 * Analysis the path and sets the corresponding mimetype.
	 */
	private void fillInMimeType() {
		String ext = FilenameUtils.getExtension(path);
		mimetype = MimeTypes.getMimeType(ext);
		
		if (mimetype.isEmpty())
			mimetype = "application/octet-stream";
	}

	public String getLocalMetadataPath() {
		if (isFolder && !isFileFolder())
			return path;

		String parent = URIUtils.getParentPath(path);

		String parentFolder = URIUtils.removeSlashAtEnd(parent);
		parentFolder = URIUtils.getFileName(parentFolder);

		if (StringUtils.equalsIgnoreCase(parentFolder, "VIDEO_TS") || StringUtils.equalsIgnoreCase(parentFolder, "BDMV"))
		{ // go back up another one
			parent = URIUtils.getParentPath(parent);
		}
		return parent;
	}

	public String getPath() {
		return path;
	}

	private boolean hasMusicInfoTag() {
		// return musicInfoTag != null;
		return false;
	}

	private boolean hasVideoInfoTag() {
		return videoInfoTag != null;
	}

	public void initialize() {
		// musicInfoTag = null;
		videoInfoTag = null;
		labelPreformated = false;
		isAlbum = false;
		dwSize = 0;
		isParentFolder = false;
		isShareOrDrive = false;
		driveType = MediaSource.SourceType.SOURCE_TYPE_UNKNOWN;
		startOffset = 0;
		startPartNumber = 1;
		endOffset = 0;
		programCount = 0;
		depth = 1;
		canQueue = true;
	}

	/**
	 * If file name is "index.bdmv" returns true, else returns false
	 * @return
	 */
	public boolean isBDFile()
	{
	  String strFileName = URIUtils.getFileName(path);
	  return (strFileName.equals("index.bdmv"));
	}

	/**
	 * Checks for .ifo and .vob files.
	 * 
	 * @param bVobs
	 * @param bIfos
	 * @return
	 */
	boolean isDVDFile(boolean bVobs /*= true*/, boolean bIfos /*= true*/) 
	{
	  String strFileName = URIUtils.getFileName(path);
	  if (bIfos)
	  {
	    if (strFileName.equals("video_ts.ifo")) return true;
	    if (StringUtils.startsWithIgnoreCase(strFileName, "vts_") && StringUtils.endsWithIgnoreCase(strFileName, "_0.ifo") && strFileName.length() == 12) return true;
	  }
	  if (bVobs)
	  {
	    if (strFileName.equals("video_ts.vob")) return true;
	    if (StringUtils.startsWithIgnoreCase(strFileName, "vts_") && StringUtils.endsWithIgnoreCase(strFileName, ".vob")) return true;
	  }

	  return false;
	}

	/**
	 * Returns true if the path has any of the the following
	 * extensions: .img, .iso, .nrg
	 * @return
	 */
	public boolean isDVDImage()
	{
	  return URIUtils.hasExtension(path, ".img|.iso|.nrg");
	}

	private boolean isFileFolder() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Returns true if the set path is a folder.
	 * 
	 * @return
	 */
	public boolean isFolder() {
		return isFolder;
	}
	
	/**
	 * Returns true is the path has .nfo extension.
	 * 
	 * @return
	 */
	public boolean isNFO() {
		return FilenameUtils.isExtension(path, "nfo");
	}

	/**
	 * Checks if a given file is a DVD or a BD file.
	 * 
	 * @return
	 */
	public boolean isOpticalMediaFile()
	{
	  boolean found = isDVDFile(false, true);
	  
	  if (found)
	    return true;
	  
	  return isBDFile();
	}

	public boolean isStack() {
		return URIUtils.isStack(path);
	}

	/**
	 * Checks file mimetypes and extensions to see if path is a
	 * known video format.
	 * 
	 * @return
	 */
	public boolean isVideo() {

		if (mimetype.toLowerCase().indexOf("video/") == 0)
			return true;

		if (hasVideoInfoTag())
			return true;
		if (hasMusicInfoTag())
			return false;

		/*
		 * check for some standard types
		 */
		String extension;
		if (mimetype.toLowerCase().indexOf("application/") == 0) {
			extension = mimetype.substring(12);
			if (extension.equals("ogg") || extension.equals("x-ogg") || extension.equals("mp4")
					|| extension.equals("mxf"))
				return true;
		}
		
		Settings settings = Settings.getInstance();

		return URIUtils.hasExtension(path, settings.getVideoExtensions());
	}

	public void setFromVideoInfoTag(VideoInfoTag video) {
		// if (!video.title.isEmpty())
		// setLabel(video.title);

		// TODO what to do when video.fileNameAndPath and video.path are null?
		if (video == null)
			video = new VideoInfoTag();
		
		if (video.fileNameAndPath == null || video.fileNameAndPath.isEmpty()) {
			
			if (video.path != null) {
				path = video.path + File.separator;
				isFolder = true;
			}
		} else {
			path = video.fileNameAndPath;
			isFolder = false;
		}

		videoInfoTag = video;

		// FillInDefaultIcon();
		fillInMimeType();
	}

	public void setPath(String path) {
		this.path = path;
	}

	public VideoInfoTag getVideoInfoTag() {
		return videoInfoTag;
	}

	/**
	 * TODO test
	 * @param useFolderNames
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public String getMovieName(boolean useFolderNames) {
		String strMovieName = getBaseMoviePath(useFolderNames);

		if (URIUtils.isStack(strMovieName))
			strMovieName = StackDirectory.getStackedTitlePath(strMovieName);

		strMovieName = URIUtils.removeSlashAtEnd(strMovieName);

		return URLDecoder.decode(URIUtils.getFileName(strMovieName));
	}

	private String getBaseMoviePath(boolean useFolderNames) {
		String strMovieName = path;

		// if (IsMultiPath())
		// strMovieName = CMultiPathDirectory::GetFirstPath(m_strPath);

		if (isOpticalMediaFile())
			return getLocalMetadataPath();

		if ((!isFolder || URIUtils.isInArchive(path)) && useFolderNames) {
			strMovieName = URIUtils.getParentPath(strMovieName);

			if (URIUtils.isInArchive(path)) {
				String strArchivePath = URIUtils.getParentPath(strMovieName);
				strMovieName = strArchivePath;
			}
		}

		return strMovieName;
	}

}
