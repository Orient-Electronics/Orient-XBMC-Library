package orient.lib.xbmc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import orient.lib.xbmc.addons.ADDON_TYPE;
import orient.lib.xbmc.addons.AddonManager;
import orient.lib.xbmc.addons.Scraper;
import orient.lib.xbmc.addons.ScraperError;
import orient.lib.xbmc.utils.ScraperUrl;
import orient.lib.xbmc.utils.XMLUtils;
import orient.lib.xbmc.video.VideoInfoTag;

public class NfoFile {

	public enum NFOResult {
		NO_NFO, FULL_NFO, URL_NFO, COMBINED_NFO, ERROR_NFO
	}
	/**
	 * XBMC calls it "info"
	 */
	private int headPos = 0;

	private String document;
	
	private ADDON_TYPE type;
	private Scraper scraper;
	private ScraperUrl scraperUrl;
	private VideoInfoTag videoInfoTag;
	
	public NfoFile() {
		// TODO Auto-generated constructor stub
	}

	public NFOResult create(String strPath, Scraper info) {
		return create(strPath, info, -1);
	}
	
	/**
	 * 
	 * 
	 * @param strPath
	 * @param info
	 * @param episode
	 * @return
	 */
	public NFOResult create(String strPath, Scraper info, int episode) {

		// assume we can use these settings
		this.scraper = info;
		this.type = info.scraperTypeFromContent(info.content());
		
		VideoInfoTag details = null;

		// load nfo file contents in object
		if (!load(strPath))
			return NFOResult.NO_NFO;


		// Load contents of NFO file in the info tag object
		boolean nfo = false;

		if (type == ADDON_TYPE.ADDON_SCRAPER_ALBUMS) {
			// CAlbum album;
			// bNfo = GetDetails(album);
		
		} else if (type == ADDON_TYPE.ADDON_SCRAPER_ARTISTS) {
			// CArtist artist;
			// bNfo = GetDetails(artist);
		
		} else if (type == ADDON_TYPE.ADDON_SCRAPER_TVSHOWS
				|| type == ADDON_TYPE.ADDON_SCRAPER_MOVIES
				|| type == ADDON_TYPE.ADDON_SCRAPER_MUSICVIDEOS) {
			
			// first check if it's an XML file with the info we need
			details = getDetails();

			// TODO should we check null here?
			
			
			// Processing TV Show Episodes
			if (episode > -1 && details != null
					&& type == ADDON_TYPE.ADDON_SCRAPER_TVSHOWS) {
				
				int infos = 0;
				headPos = document.indexOf("<episodedetails", headPos);
				details = getDetails();

				while (headPos != -1 && details.episode != episode) {
					headPos = document.indexOf("<episodedetails", headPos + 1);
					details = getDetails();
					infos++;
				}
				
				if (details.episode != episode) {
					nfo = false;
					details.reset();
					headPos = 0;
					
					// still allow differing nfo/file numbers for single ep nfo's
					if (infos == 1) 
						details = getDetails();
				}
			}
			
			nfo = (details == null) ? false : true;
		}

		ArrayList<Scraper> vecScrapers = new ArrayList<Scraper>();

		//// Building scraper list ////
		
		Scraper defaultScraper = (Scraper) AddonManager.getDefaultScraper(type);
		
		// 1. Add selected scraper - first priority
		if (info != null)
			vecScrapers.add(info);


		// 2. Add all other scrapers except default
		ArrayList<Scraper> addons = AddonManager.GetScrapers(type);


		for (Scraper scraper : addons) {

			// skip if scraper requires settings and there's nothing set yet
			if (scraper.requiresSettings() && !scraper.hasUserSettings())
				continue;

			String sId = scraper.getId();
			if (info != null && !info.getId().equals(sId)
					&& defaultScraper != null
					&& !defaultScraper.getId().equals(sId))
				vecScrapers.add((Scraper) scraper);
		}

		// 3. add default scraper - not user selectable so it's last priority
		if (defaultScraper != null
				&& info != null
				&& !info.getId().equals(defaultScraper.getId())
				&& (!defaultScraper.requiresSettings() || defaultScraper.hasUserSettings() ) )
			vecScrapers.add(defaultScraper);

		// search..
		int scrapeResult = -1;
		for (Scraper scraper : vecScrapers) {

			scrapeResult = extractScraperUrl(scraper);

			if ((scrapeResult == 0 || scrapeResult == 2))
				break;
		}

		// Result evaluation
		this.videoInfoTag = details;
		
		
		if (scrapeResult == 2)
			return NFOResult.ERROR_NFO;

		if (nfo)
			return scraperUrl.m_url.isEmpty() ? NFOResult.FULL_NFO : NFOResult.COMBINED_NFO;

		return scraperUrl.m_url.isEmpty() ? NFOResult.NO_NFO : NFOResult.URL_NFO;
	}

	/**
	 * Extract the ScraperUrl from NFO file using the given scraper.
	 * 
	 * @param scraper
	 * @return 0 - success; 1 - no result; skip; 2 - error
	 */
	private int extractScraperUrl(Scraper scraper) {
		if (scraper.isNoop()) {
			scraperUrl = new ScraperUrl();
			return 0;
		}
		if (scraper.getType() != type)
			return 1;

		// scraper.ClearCache();

		try {
			scraperUrl = scraper.nfoUrl(document);
		} catch (ScraperError e) {
			return 2;
		}

		if (!scraperUrl.m_url.isEmpty())
			setScraper(scraper);

		return scraperUrl.m_url.isEmpty() ? 1 : 0;
	}

	
	
	public VideoInfoTag getDetails() {
		return getDetails(null, false);
	}
	
	public VideoInfoTag getDetails(VideoInfoTag details) {
		return getDetails(details, false);
	}

	/**
	 * Loads NFO XML data to add build a VideoInfoTag. If a VideoInfoTag, it
	 * is appended, otherwise a new tag is created.
	 * 
	 * @param details
	 * @param prioritise
	 * @return
	 */
	public VideoInfoTag getDetails (VideoInfoTag details, boolean prioritise)
	{
		String strDoc = this.document.substring(headPos);

		// removing xml version string
		strDoc = strDoc.replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim();

		// Wrapping with root tags to cater any loose elements
		strDoc = "<root>"+strDoc+"</root>";
		
		// Document processing
		// TODO may be there's a better way to do this?
		Document doc = XMLUtils.getDocumentFromString(strDoc);
		Element rootEl = XMLUtils.getFirstChildElement(doc.getDocumentElement());
		doc = XMLUtils.getDocumentFromString(XMLUtils.nodeToString(rootEl));

		if (details == null)
			details = new VideoInfoTag();
		
		details.load(doc.getDocumentElement(), true, prioritise);
		
		return details; 
	}

	public Scraper getScraper() {
		return scraper;
	}

	
	
	public ScraperUrl getScraperUrl() {
		return scraperUrl;
	}

	public VideoInfoTag getVideoInfoTag() {
		return videoInfoTag;
	}

	/**
	 * Loads the contents of the given .nfo file into the object (as a string)
	 * 
	 * @param fileName
	 *            Path to the nfo file
	 * @return
	 */
	public boolean load(String fileName) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(fileName));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			document = sb.toString();
			br.close();
			return true;
		} catch (IOException e) {
			document = null;
			return false;
		}
	}
	
	/**
	 * @deprecated Use extractScraperUrl instead. Only exists to XBMC backwards
	 *             compatibility.
	 * @param scraper
	 * @return
	 * @see extractScraperUrl
	 */
	@SuppressWarnings("unused")
	private int scrape(Scraper scraper) {
		return extractScraperUrl(scraper);
	}

	public void setScraper(Scraper scraper) {
		this.scraper = scraper;
	}

	public void setScraperUrl(ScraperUrl scraperUrl) {
		this.scraperUrl = scraperUrl;
	}

	public void setVideoInfoTag(VideoInfoTag videoInfoTag) {
		this.videoInfoTag = videoInfoTag;
	}
}
