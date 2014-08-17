package com.orient.lib.xbmc.video;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import com.orient.lib.xbmc.CONTENT_TYPE;
import com.orient.lib.xbmc.FileItem;
import com.orient.lib.xbmc.FileItemList;
import com.orient.lib.xbmc.NfoFile;
import com.orient.lib.xbmc.Settings;
import com.orient.lib.xbmc.Util;
import com.orient.lib.xbmc.NfoFile.NFOResult;
import com.orient.lib.xbmc.addons.AddonManager;
import com.orient.lib.xbmc.addons.Scraper;
import com.orient.lib.xbmc.addons.ScraperError;
import com.orient.lib.xbmc.filesystem.StackDirectory;
import com.orient.lib.xbmc.utils.ScraperUrl;
import com.orient.lib.xbmc.utils.URIUtils;

public class VideoInfoScanner {

	private Set pathsToScan;
	
	/**
	 * return values from the information lookup functions
	 */
	public enum INFO_RET {
		INFO_CANCELLED, INFO_ERROR, INFO_NOT_NEEDED, INFO_HAVE_ALREADY, INFO_NOT_FOUND, INFO_ADDED
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

	private NfoFile nfoReader;

	private FileItem lastProcessedFileItem;

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
	
	public FileItem getLastProcessedFileItem() {
		return lastProcessedFileItem;
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

	
//	public boolean retrieveVideoInfo(FileItemList items, boolean bDirNames, CONTENT_TYPE content, boolean useLocal, ScraperUrl pURL, boolean fetchEpisodes)
//	{
//	    // TODO DB
////	    m_database.Open();
//
//	    boolean FoundSomeInfo = false;
//	    
//	    ArrayList<Integer> seenPaths = new ArrayList<Integer>();
//	    
//	    for (int i = 0; i < (int)items.size(); ++i)
//	    {
//	      nfoReader.close();
//	      FileItem pItem = items.get(i);
//
//	      // we do this since we may have a override per dir
//	      Scraper info2 = m_database.GetScraperForPath(pItem.isFolder() ? pItem.getPath() : items.getPath());
//	      
//	      if (info2 == null) // skip
//	        continue;
//
//	      Settings settings = Settings.getInstance();
//	      
//	      // Discard all exclude files defined by regExExclude
//	      if (Util.excludeFileOrFolder(pItem.getPath(), (content == CONTENT_TYPE.CONTENT_TVSHOWS) ? settings.getTvshowExcludeFromScanRegExps()
//	                                                                                    : settings.getMoviesExcludeFromScanRegExps())
//	        continue;
//
//
//	      // clear our scraper cache
////	      info2.ClearCache();
//
//	      INFO_RET ret = INFO_RET.INFO_CANCELLED;
//	      
//	      if (info2.content() == CONTENT_TYPE.CONTENT_TVSHOWS)
//	        ret = retrieveInfoForTvShow(pItem, bDirNames, info2, useLocal, fetchEpisodes);
//	      else if (info2.content() == CONTENT_TYPE.CONTENT_MOVIES)
//	        ret = retrieveInfoForMovie(pItem, bDirNames, info2, useLocal);
//	      else if (info2.content() == CONTENT_TYPE.CONTENT_MUSICVIDEOS)
//	        ret = retrieveInfoForMusicVideo(pItem, bDirNames, info2, useLocal);
//	      else
//	      {
////	        CLog::Log(LOGERROR, "VideoInfoScanner: Unknown content type %d (%s)", info2->Content(), CURL::GetRedacted(pItem->GetPath()).c_str());
//	        FoundSomeInfo = false;
//	        break;
//	      }
//	      if (ret == INFO_RET.INFO_CANCELLED || ret == INFO_RET.INFO_ERROR)
//	      {
//	        FoundSomeInfo = false;
//	        break;
//	      }
//	      if (ret == INFO_RET.INFO_ADDED || ret == INFO_RET.INFO_HAVE_ALREADY)
//	        FoundSomeInfo = true;
//	      else if (ret == INFO_RET.INFO_NOT_FOUND)
//	      {
////	        CLog::Log(LOGWARNING, "No information found for item '%s', it won't be added to the library.", CURL::GetRedacted(pItem->GetPath()).c_str());
//	      }
//
//	      pURL = null;
//
//	      // Keep track of directories we've seen
//	      if (m_bClean && pItem.isFolder())
//	        seenPaths.add(m_database.GetPathId(pItem.getPath()));
//	    }
//
//	    if (content == CONTENT_TVSHOWS && ! seenPaths.empty())
//	    {
//	      vector< pair<int,string> > libPaths;
//	      m_database.GetSubPaths(items.GetPath(), libPaths);
//	      for (vector< pair<int,string> >::iterator i = libPaths.begin(); i < libPaths.end(); ++i)
//	      {
//	        if (find(seenPaths.begin(), seenPaths.end(), i->first) == seenPaths.end())
//	          m_pathsToClean.insert(i->first);
//	      }
//	    }
//	    if(pDlgProgress)
//	      pDlgProgress->ShowProgressBar(false);
//
//	    g_infoManager.ResetLibraryBools();
//	    m_database.Close();
//	    return FoundSomeInfo;
//	  }
//	
	/**
	 * 
	 * @param item
	 * @param dirNames
	 *            seems to be "grabAny" for getting NFO
	 * @param scraper
	 * @param useLocal Use local data (nfo files)?
	 * @return
	 */
	public INFO_RET retrieveInfoForMovie(FileItem item, boolean dirNames,
			Scraper scraper, boolean useLocal) {

		if(lastProcessedFileItem != null)
			lastProcessedFileItem.reset();
		
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
		// Processing NFO Result
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

	/**
	 * 
	 * @param item
	 * @param dirNames
	 *            seems to be "grabAny" for getting NFO
	 * @param scraper
	 * @param useLocal Use local data (nfo files)?
	 * @return
	 */
	public INFO_RET retrieveInfoForMusicVideo(FileItem item, boolean dirNames,
			Scraper scraper, boolean useLocal) {
		
		if(lastProcessedFileItem != null)
			lastProcessedFileItem.reset();
		
		lastProcessedFileItem = item;
		
		if (item.isFolder() || !item.isVideo() || item.isNFO() ||
				(item.isPlayList() && !URIUtils.hasExtension(item.getPath(), ".strm")))
			
			return INFO_RET.INFO_NOT_NEEDED;

//		TODO db
//	    if (m_database.HasMusicVideoInfo(pItem->GetPath()))
//	      return INFO_HAVE_ALREADY;

	    
	    // handle .nfo files
	    NFOResult result = NFOResult.NO_NFO;

	    if (useLocal)
	      result = checkForNFOFile(item, dirNames, scraper);
	    
	    // Processing NFO Result
	    if (result == NFOResult.FULL_NFO)
	    {
	    	lastProcessedFileItem.getVideoInfoTag().reset();
	    	lastProcessedFileItem.setFromVideoInfoTag(nfoReader.getVideoInfoTag());

	    	// TODO DB
//	    	if (addVideo(pItem, info2.content(), bDirNames, true) < 0)
//	    		return INFO_RET.INFO_ERROR;
	      
	      return INFO_RET.INFO_ADDED;
	    }
	    
//	    if (result == NFOResult.URL_NFO || result == NFOResult.COMBINED_NFO)
//	      pURL = scrUrl;


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
	
	
	public INFO_RET retrieveInfoForTvShow(FileItem item, boolean dirNames, Scraper scraper, boolean useLocal, boolean fetchEpisodes)
	{
		if(lastProcessedFileItem != null)
			lastProcessedFileItem.reset();

		lastProcessedFileItem = item;
		

		long idTvShow = -1;

		//		TODO DB
		//		if (pItem.isFolder()) {
		//	      idTvShow = m_database.GetTvShowId(pItem->GetPath());
		//		}
		//	    else
		//	    {
		//	      String strPath = URIUtils.getDirectory(pItem.getPath());
		//	      idTvShow = m_database.GetTvShowId(strPath);
		//	    }


		if (idTvShow > -1 && (fetchEpisodes || !item.isFolder()))
		{
			INFO_RET ret = retrieveInfoForEpisodes(item, idTvShow, scraper, useLocal);

			//		TODO DB
			//	      if (ret == INFO_RET.INFO_ADDED)
			//	        m_database.SetPathHash(pItem->GetPath(), pItem->GetProperty("hash").asString());

			return ret;
		}


		// handle .nfo files
		NFOResult result = NFOResult.NO_NFO;

		if (useLocal)
			result = checkForNFOFile(item, dirNames, scraper);
	    
	    
		if (result == NFOResult.FULL_NFO)
		{
			lastProcessedFileItem.getVideoInfoTag().reset();
			lastProcessedFileItem.setFromVideoInfoTag(nfoReader.getVideoInfoTag());

			// TODO DB
			// long lResult = addVideo(item, scraper.content(), dirNames, useLocal);
			//	      
			//	      if (lResult < 0)
			//	        return INFO_RET.INFO_ERROR;

			if (fetchEpisodes)
			{
				// TODO DB
				//	        INFO_RET ret = retrieveInfoForEpisodes(item, lResult, scraper, useLocal);

				//	        if (ret == INFO_RET.INFO_ADDED)
				//	          m_database.SetPathHash(pItem->GetPath(), pItem->GetProperty("hash").asString());

				//	        return ret;
			}
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
//						long lResult=-1;
			//			if ((lResult = AddVideo(item, scraper.content(), false, useLocal)) < 0)
			//				return INFO_RET.INFO_ERROR;

			return INFO_RET.INFO_ADDED;
		}
		
		// TODO DB
//	    if (fetchEpisodes)
//	    {
//	      INFO_RET ret = RetrieveInfoForEpisodes(item, lResult, scraper, useLocal);
//	      
//	      if (ret == INFO_RET.INFO_ADDED)
//	        m_database.SetPathHash(pItem.getPath(), pItem->GetProperty("hash").asString());
//	    }
	
		return INFO_RET.INFO_ADDED;
	  }
	
	
	public INFO_RET retrieveInfoForEpisodes(FileItem item, long showID, Scraper scraper, boolean useLocal)
	  {
	    // enumerate episodes
	    ArrayList<Episode> files = null;
	    
	    if (!enumerateSeriesFolder(item, files))
	      return INFO_RET.INFO_HAVE_ALREADY;
	    
	    if (files.isEmpty()) // no update or no files
	      return INFO_RET.INFO_NOT_NEEDED;

//	    if (m_bStop || (progress && progress->IsCanceled()))
//	      return INFO_CANCELLED;

	    VideoInfoTag showInfo = null;
	    
//	    TODO DB
//	    m_database.GetTvShowInfo("", showInfo, showID);
	    return onProcessSeriesFolder(files, scraper, useLocal, showInfo);
	  }
	
	
	private INFO_RET onProcessSeriesFolder(ArrayList<Episode> files,
			Scraper scraper, boolean useLocal, VideoInfoTag showInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean enumerateSeriesFolder(FileItem item, ArrayList<Episode> episodeList)
	{
		return false;
//		FileItemList items = new FileItemList();
//
//		Settings settings = Settings.getInstance();
//	    ArrayList<String> regexps = settings.getTvshowExcludeFromScanRegExps();
//
//	    boolean bSkip = false;
//
//	    if (item.isFolder())
//	    {
//	      /*
//	       * Note: DoScan() will not remove this path as it's not recursing for tvshows.
//	       * Remove this path from the list we're processing in order to avoid hitting
//	       * it twice in the main loop.
//	       */
//	      Set.iterator it = pathsToScan.find(item.getPath());
//	      if (it != m_pathsToScan.end())
//	        m_pathsToScan.erase(it);
//
//	      
////	      Iterator<String> iterator = pathsToScan.iterator();
////	      while(iterator.hasNext()) {
////	          Integer setElement = iterator.next();
////	          if(setElement==2) {
////	              iterator.remove();
////	          }
////	      }
//	      
//	      String hash, dbHash;
//	      hash = GetRecursiveFastHash(item->GetPath(), regexps);
//	      if (m_database.GetPathHash(item->GetPath(), dbHash) && !hash.empty() && dbHash == hash)
//	      {
//	        // fast hashes match - no need to process anything
//	        bSkip = true;
//	      }
//
//	      // fast hash cannot be computed or we need to rescan. fetch the listing.
//	      if (!bSkip)
//	      {
//	        int flags = DIR_FLAG_DEFAULTS;
//	        if (!hash.empty())
//	          flags |= DIR_FLAG_NO_FILE_INFO;
//
//	        Util.getRecursiveListing(item->GetPath(), items, g_advancedSettings.m_videoExtensions, flags);
//
//	        // fast hash failed - compute slow one
//	        if (hash.empty())
//	        {
//	          GetPathHash(items, hash);
//	          if (dbHash == hash)
//	          {
//	            // slow hashes match - no need to process anything
//	            bSkip = true;
//	          }
//	        }
//	      }
//
//	      if (bSkip)
//	      {
//	        CLog::Log(LOGDEBUG, "VideoInfoScanner: Skipping dir '%s' due to no change", CURL::GetRedacted(item->GetPath()).c_str());
//	        // update our dialog with our progress
//	        if (m_handle)
//	          OnDirectoryScanned(item->GetPath());
//	        return false;
//	      }
//
//	      if (dbHash.empty())
//	        CLog::Log(LOGDEBUG, "VideoInfoScanner: Scanning dir '%s' as not in the database", CURL::GetRedacted(item->GetPath()).c_str());
//	      else
//	        CLog::Log(LOGDEBUG, "VideoInfoScanner: Rescanning dir '%s' due to change (%s != %s)", CURL::GetRedacted(item->GetPath()).c_str(), dbHash.c_str(), hash.c_str());
//
//	      if (m_bClean)
//	      {
//	        m_pathsToClean.insert(m_database.GetPathId(item->GetPath()));
//	        m_database.GetPathsForTvShow(m_database.GetTvShowId(item->GetPath()), m_pathsToClean);
//	      }
//	      item->SetProperty("hash", hash);
//	    }
//	    else
//	    {
//	      CFileItemPtr newItem(new CFileItem(*item));
//	      items.Add(newItem);
//	    }
//
//	    /*
//	    stack down any dvd folders
//	    need to sort using the full path since this is a collapsed recursive listing of all subdirs
//	    video_ts.ifo files should sort at the top of a dvd folder in ascending order
//
//	    /foo/bar/video_ts.ifo
//	    /foo/bar/vts_x_y.ifo
//	    /foo/bar/vts_x_y.vob
//	    */
//
//	    // since we're doing this now anyway, should other items be stacked?
//	    items.Sort(SortByPath, SortOrderAscending);
//	    int x = 0;
//	    while (x < items.Size())
//	    {
//	      if (items[x]->m_bIsFolder)
//	        continue;
//
//
//	      CStdString strPathX, strFileX;
//	      URIUtils::Split(items[x]->GetPath(), strPathX, strFileX);
//	      //CLog::Log(LOGDEBUG,"%i:%s:%s", x, strPathX.c_str(), strFileX.c_str());
//
//	      int y = x + 1;
//	      if (strFileX.Equals("VIDEO_TS.IFO"))
//	      {
//	        while (y < items.Size())
//	        {
//	          CStdString strPathY, strFileY;
//	          URIUtils::Split(items[y]->GetPath(), strPathY, strFileY);
//	          //CLog::Log(LOGDEBUG," %i:%s:%s", y, strPathY.c_str(), strFileY.c_str());
//
//	          if (strPathY.Equals(strPathX))
//	            /*
//	            remove everything sorted below the video_ts.ifo file in the same path.
//	            understandbly this wont stack correctly if there are other files in the the dvd folder.
//	            this should be unlikely and thus is being ignored for now but we can monitor the
//	            where the path changes and potentially remove the items above the video_ts.ifo file.
//	            */
//	            items.Remove(y);
//	          else
//	            break;
//	        }
//	      }
//	      x = y;
//	    }
//
//	    // enumerate
//	    for (int i=0;i<items.Size();++i)
//	    {
//	      if (items[i]->m_bIsFolder)
//	        continue;
//	      CStdString strPath = URIUtils::GetDirectory(items[i]->GetPath());
//	      URIUtils::RemoveSlashAtEnd(strPath); // want no slash for the test that follows
//
//	      if (URIUtils::GetFileName(strPath).Equals("sample"))
//	        continue;
//
//	      // Discard all exclude files defined by regExExcludes
//	      if (CUtil::ExcludeFileOrFolder(items[i]->GetPath(), regexps))
//	        continue;
//
//	      /*
//	       * Check if the media source has already set the season and episode or original air date in
//	       * the VideoInfoTag. If it has, do not try to parse any of them from the file path to avoid
//	       * any false positive matches.
//	       */
//	      if (ProcessItemByVideoInfoTag(items[i].get(), episodeList))
//	        continue;
//
////	      if (!EnumerateEpisodeItem(items[i].get(), episodeList))
////	        CLog::Log(LOGDEBUG, "VideoInfoScanner: Could not enumerate file %s", CURL::GetRedacted(CURL::Decode(items[i]->GetPath())).c_str());
//	    }
//	    return true;
	  }
	

	public void setLastProcessedFileItem(FileItem lastProcessedFileItem) {
		this.lastProcessedFileItem = lastProcessedFileItem;
	}
}
