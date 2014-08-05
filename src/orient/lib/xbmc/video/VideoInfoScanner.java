package orient.lib.xbmc.video;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import orient.lib.xbmc.CONTENT_TYPE;
import orient.lib.xbmc.FileItem;
import orient.lib.xbmc.NfoFile;
import orient.lib.xbmc.NfoFile.NFOResult;
import orient.lib.xbmc.addons.Scraper;
import orient.lib.xbmc.addons.ScraperError;
import orient.lib.xbmc.filesystem.StackDirectory;
import orient.lib.xbmc.utils.ScraperUrl;
import orient.lib.xbmc.utils.URIUtils;

public class VideoInfoScanner {

	private NfoFile nfoReader;
	private FileItem lastProcessedFileItem;

	/**
	 * return values from the information lookup functions
	 */
	public enum INFO_RET {
		INFO_CANCELLED, INFO_ERROR, INFO_NOT_NEEDED, INFO_HAVE_ALREADY, INFO_NOT_FOUND, INFO_ADDED
	}

	public VideoInfoScanner() {
		nfoReader = new NfoFile();
	}

	/**
	 * Add an item to the database.
	 * 
	 * @param pItem
	 *            item to add to the database.
	 * @param content
	 *            content type of the item.
	 * @param videoFolder
	 *            whether the video is represented by a folder (single movie per
	 *            folder). Defaults to false.
	 * @param useLocal
	 *            whether to use local information for artwork etc.
	 * @param showInfo
	 *            pointer to CVideoInfoTag details for the show if this is an
	 *            episode. Defaults to NULL.
	 * @param libraryImport
	 *            Whether this call belongs to a full library import or not.
	 *            Defaults to false.
	 * @return database id of the added item, or -1 on failure.
	 */
	public int addVideo(FileItem pItem, CONTENT_TYPE content,
			boolean videoFolder /* = false */, boolean useLocal /* = true */,
			VideoInfoTag showInfo /* = NULL */, boolean libraryImport /* = false */) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * <ol>
	 * <li>Fetches the appropriate nfo file path.</li>
	 * <li>"Creates" the file otherwise Returns NFOResult.NO_NFO if no nfo file
	 * found.</li>
	 * </ol>
	 * 
	 * @param item
	 * @param grabAny
	 * @param scraper
	 * @param scrUrl
	 *            ->>> not needed
	 * @return
	 * 
	 *         TODO consider moving this to NfoFile class
	 */
	private NFOResult checkForNFOFile(FileItem item, boolean grabAny,
			Scraper scraper) {

		String strNfoFile = null;

		// Try to find local NFO file
		if (scraper.content() == CONTENT_TYPE.CONTENT_MOVIES
				|| scraper.content() == CONTENT_TYPE.CONTENT_MUSICVIDEOS
				|| (scraper.content() == CONTENT_TYPE.CONTENT_TVSHOWS && !item
						.isFolder()))
			strNfoFile = getNFOFile(item, grabAny);

		if (scraper.content() == CONTENT_TYPE.CONTENT_TVSHOWS
				&& !item.isFolder()) {
			strNfoFile = URIUtils.addFileToFolder(item.getPath(), "tvshow.nfo");

			File file = new File(strNfoFile);

			if (!file.exists())
				strNfoFile = null;
		}

		// If no NFO found
		if (strNfoFile == null || strNfoFile.isEmpty())
			return NFOResult.NO_NFO;

		// Processing the result
		NfoFile.NFOResult result = NFOResult.NO_NFO;

		if (scraper.content() == CONTENT_TYPE.CONTENT_TVSHOWS
				&& !item.isFolder())
			result = nfoReader.create(strNfoFile, scraper,
					item.getVideoInfoTag().episode);
		else
			result = nfoReader.create(strNfoFile, scraper);

		// Setting type
		// String type;
		//
		// if(result == NFOResult.COMBINED_NFO)
		// type = "Mixed";
		//
		// else if(result == NFOResult.FULL_NFO)
		// type = "Full";
		//
		// else if(result == NFOResult.URL_NFO)
		// type = "URL";
		//
		// else if(result == NFOResult.NO_NFO)
		// type = "";
		//
		// else
		// type = "malformed";
		//
		// if (result != NFOResult.NO_NFO)
		// CLog::Log(LOGDEBUG,
		// "VideoInfoScanner: Found matching %s NFO file: %s", type.c_str(),
		// CURL::GetRedacted(strNfoFile).c_str());

		return result;
	}

	/**
	 * <p>
	 * Tries to find an NFO file for the given file item. It checks the
	 * following cases:
	 * </p>
	 * 
	 * <h1>Case 1</h1>
	 * <p>
	 * C:\Path\to\folder\filename.avi
	 * </p>
	 * 
	 * @param item
	 * @param grabAny
	 * @return
	 * 
	 *         TODO consider moving this to NfoFile class as "findVideoNfoPath"
	 */
	public String getNFOFile(FileItem item, boolean grabAny) {

		String nfoFile = null;
		File nfo;

		// Find a matching .nfo file
		if (!item.isFolder()) {

			// we have a rarred item - we want to check outside the rars
			if (URIUtils.isInRAR(item.getPath())) {

				// TODO create copy constructor
				FileItem item2 = item;
				URL url;
				try {
					url = new URL(item.getPath());
					String strPath = URIUtils.getDirectory(url.getHost());
					item2.setPath(URIUtils.addFileToFolder(strPath,
							FilenameUtils.getBaseName(item.getPath())));
					return getNFOFile(item2, grabAny);
				} catch (MalformedURLException e) {
					e.printStackTrace();
					return null;
				}
			}

			// grab the folder path
			String strPath = URIUtils.getDirectory(item.getPath());

			// looking up by folder name - movie.nfo takes priority
			// - but not for stacked items (handled below)
			if (grabAny && !item.isStack()) {
				nfoFile = URIUtils.addFileToFolder(strPath, "movie.nfo");
				nfo = new File(nfoFile);

				if (nfo.exists())
					return nfoFile;
			}

			// try looking for .nfo file for a stacked item
			if (item.isStack()) {
				// first try .nfo file matching first file in stack
				String firstFile = StackDirectory.getFirstStackedFile(item
						.getPath());

				FileItem item2 = new FileItem(firstFile, false);
				nfoFile = getNFOFile(item2, grabAny);

				// else try .nfo file matching stacked title
				if (nfoFile.isEmpty()) {
					String stackedTitlePath = StackDirectory
							.getStackedTitlePath(item.getPath());
					item2.setPath(stackedTitlePath);
					nfoFile = getNFOFile(item2, grabAny);
				}
			} else {
				// already an .nfo file?
				if (URIUtils.hasExtension(item.getPath(), ".nfo"))
					nfoFile = item.getPath();

				// no, create .nfo file
				// nfo file name is same as media file name
				else
					nfoFile = URIUtils.replaceExtension(item.getPath(), ".nfo");
			}

			// test file existence
			nfo = new File(nfoFile);
			if (!nfoFile.isEmpty() && nfo.exists())
				return nfoFile;

			nfoFile = "";

			// final attempt - strip off any cd1 folders
			if (nfoFile.isEmpty()) {
				// need no slash for the check that follows
				strPath = URIUtils.removeSlashAtEnd(strPath);

				if (strPath.indexOf("cd1") > -1) {
					strPath = strPath.substring(0, strPath.length() - 3);
					strPath = URIUtils.addFileToFolder(strPath,
							URIUtils.getFileName(item.getPath()));

					FileItem item2 = new FileItem(strPath, false);

					return getNFOFile(item2, grabAny);
				}
			}

			if (nfoFile.isEmpty() && item.isOpticalMediaFile()) {
				FileItem parentDirectory = new FileItem(
						item.getLocalMetadataPath(), true);
				nfoFile = getNFOFile(parentDirectory, true);
			}
		}

		// // folders (or stacked dvds) can take any nfo file if there's a
		// unique one
		// if (item.isFolder() || item.isOpticalMediaFile() || (bGrabAny &&
		// nfoFile.isEmpty()))
		// {
		// // see if there is a unique nfo file in this folder, and if so, use
		// that
		// FileItemList items;
		// Directory dir;
		// String strPath;
		// if (item.isFolder())
		// strPath = item.getPath();
		// else
		// strPath = URIUtils.getDirectory(item.getPath());
		//
		// if (dir.GetDirectory(strPath, items, ".nfo") && items.Size())
		// {
		// int numNFO = -1;
		// for (int i = 0; i < items.Size(); i++)
		// {
		// if (items[i].isNFO())
		// {
		// if (numNFO == -1)
		// numNFO = i;
		// else
		// {
		// numNFO = -1;
		// break;
		// }
		// }
		// }
		// if (numNFO > -1)
		// return items[numNFO].getPath();
		// }
		// }

		return nfoFile;

	}

	/**
	 * 
	 * @param item
	 * @param dirNames
	 *            seems to be "grabAny" for getting NFO
	 * @param scraper
	 * @param useLocal
	 * @param scraperUrl
	 *            not required, only used as reference in XBMC
	 * @return
	 */
	public INFO_RET retrieveInfoForMovie(FileItem item, boolean dirNames,
			Scraper scraper, boolean useLocal) {

		lastProcessedFileItem = item;
		
		if (item.isFolder() || !item.isVideo() || item.isNFO())
			return INFO_RET.INFO_NOT_NEEDED;

		// TODO DB
		// if (m_database.HasMovieInfo(pItem->GetPath()))
		// return INFO_HAVE_ALREADY;

		// handle .nfo files
		NFOResult result = NFOResult.NO_NFO;

		if (useLocal)
			result = checkForNFOFile(item, dirNames, scraper);

//		// ////////////////////////////////
//		// / moved from CheckForNFOFile ///
//		// ////////////////////////////////
//
//		ScraperUrl scrUrl = new ScraperUrl();
//
//		if (result == NFOResult.FULL_NFO) {
//			if (scraper.content() == CONTENT_TYPE.CONTENT_TVSHOWS)
//				scraper = nfoReader.getScraper();
//		} else if (result != NFOResult.NO_NFO && result != NFOResult.ERROR_NFO) {
//			scrUrl = nfoReader.getScraperUrl();
//			scraper = nfoReader.getScraper();
//
//			// CLog::Log(LOGDEBUG,
//			// "VideoInfoScanner: Fetching url '%s' using %s scraper (content: '%s')",
//			// scrUrl.m_url[0].m_url.c_str(), info->Name().c_str(),
//			// TranslateContent(info->Content()).c_str());
//
//			if (result == NFOResult.COMBINED_NFO)
//				item.setFromVideoInfoTag(nfoReader.getDetails());
//		}
//
//		// //////////////////////
//
//		if (result == NFOResult.FULL_NFO) {
//			item.getVideoInfoTag().reset();
//			item.setFromVideoInfoTag(nfoReader.getDetails());
//
//			// TODO DB
//			// if (addVideo(item, scraper.content(), dirNames, true) < 0)
//			// return INFO_RET.INFO_ERROR;
//
//			return INFO_RET.INFO_ADDED;
//		}
//
//		// TODO this is useless, has to be returned
//		if (result == NFOResult.URL_NFO || result == NFOResult.COMBINED_NFO)
//			scraperUrl = scrUrl;
//
//		
//		// Next part
////		ScraperUrl url = new ScraperUrl();
////	    int retVal = 0;
////	    
////	    if (scraperUrl != null)
////	      url = scraperUrl;
////	    
	    
		
		// Compensation of above
		if (result == NFOResult.COMBINED_NFO)
			lastProcessedFileItem.setFromVideoInfoTag(nfoReader.getVideoInfoTag());
	    
		if (result == NFOResult.FULL_NFO) {
			lastProcessedFileItem.getVideoInfoTag().reset();
			lastProcessedFileItem.setFromVideoInfoTag(nfoReader.getVideoInfoTag());

			// TODO DB
			// if (addVideo(item, scraper.content(), dirNames, true) < 0)
			// return INFO_RET.INFO_ERROR;

			return INFO_RET.INFO_ADDED;
		}
		
		
		// Data fetch
		ScraperUrl videoUrl = findVideo(lastProcessedFileItem.getMovieName(dirNames), scraper);

		if (videoUrl == null)
			return INFO_RET.INFO_NOT_FOUND;

		NfoFile nfoFile = (result == NFOResult.COMBINED_NFO) ? nfoReader : null;

		lastProcessedFileItem = getDetails(lastProcessedFileItem, videoUrl, scraper, nfoFile);

		if (lastProcessedFileItem.getVideoInfoTag() != null) {
			
			// TODO DB
			// if (AddVideo(pItem, info2->Content(), bDirNames, useLocal) < 0)
			// return INFO_ERROR;
			
			return INFO_RET.INFO_ADDED;
		}

		// TODO: This is not strictly correct as we could fail to download
		// information here or error, or be cancelled
		return INFO_RET.INFO_NOT_FOUND;
	}

	
	
	public FileItem getLastProcessedFileItem() {
		return lastProcessedFileItem;
	}

	public void setLastProcessedFileItem(FileItem lastProcessedFileItem) {
		this.lastProcessedFileItem = lastProcessedFileItem;
	}

	/**
	 * Loads the given file item with details of the given video. These details
	 * are collected from both scraper and nfo file. Scraper data gets the
	 * priority.
	 * 
	 * Logic different from XBMC: XBMC doesnt return anything if online scraper
	 * fails. Our logic atleast sends the nfo data, if available.
	 * 
	 * TODO Recheck above logic, as nfo files may already be loaded at an earlier stage
	 * TODO see if this can be renamed as "getVideoDetails"
	 * 
	 * @param item
	 * @param url
	 * @param scraper
	 * @param nfoFile
	 * @return
	 */
	public static FileItem getDetails(FileItem item, ScraperUrl url, Scraper scraper,
			NfoFile nfoFile) {
		
		VideoInfoTag movieDetails = null;
		
		try {
			movieDetails = scraper.getVideoDetails(url, true/*fMovie*/);
		} catch (ScraperError e) {
			e.printStackTrace();
		}
		
		if (item.getVideoInfoTag() != null)
			// TODO change this to merge
			item.getVideoInfoTag().overwrite(movieDetails);
		
//		VideoInfoDownloader imdb = new VideoInfoDownloader(scraper);
//		VideoInfoTag movieDetails = imdb.getDetails(url);
		
//		if (nfoFile != null)
//			movieDetails = nfoFile.getDetails(movieDetails, true);
		
		item.setFromVideoInfoTag(movieDetails);

		return item;
	}

	/**
	 * Finds a movie by a given name from the given scraper.
	 * 
	 * TODO check if its only finding movies or working with other videos as
	 * well.
	 * 
	 * @param videoName
	 * @param scraper
	 * @return
	 */
	public static ScraperUrl findVideo(String videoName, Scraper scraper) {

		ArrayList<ScraperUrl> movieList;

		try {
			movieList = scraper.findMovie(videoName, true);

			// no results. try without cleaning chars like '.' and '_'
			if (movieList == null || movieList.isEmpty())
				movieList = scraper.findMovie(videoName, false);

			if (movieList != null && !movieList.isEmpty())
				return movieList.get(0);

		} catch (ScraperError e) {
			e.printStackTrace();
		}

		return null;
	}
}
