package orient.lib.xbmc.addons;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import orient.lib.xbmc.CONTENT_TYPE;
import orient.lib.xbmc.Util;
import orient.lib.xbmc.utils.CppUtils;
import orient.lib.xbmc.utils.ScraperParser;
import orient.lib.xbmc.utils.ScraperUrl;
import orient.lib.xbmc.utils.XMLUtils;
import orient.lib.xbmc.video.Episode;
import orient.lib.xbmc.video.VideoInfoTag;

public class Scraper extends Addon {
	public class ContentMapping {
		public String name;
		public CONTENT_TYPE type;
		public int pretty;

		public ContentMapping(String name, CONTENT_TYPE type, int pretty) {
			super();
			this.name = name;
			this.type = type;
			this.pretty = pretty;
		}
	}
	public class RelevanceSortComparator implements Comparator<ScraperUrl> {
	    @Override
	    public int compare(ScraperUrl o1, ScraperUrl o2) {
	    	if (o1.relevance < o2.relevance) return 1;
	        if (o1.relevance > o2.relevance) return -1;
	        return 0;
	    }
	}
	
	boolean m_fLoaded;
	String m_language;

	boolean m_requiressettings;

	// CDateTimeSpan m_persistence;
	CONTENT_TYPE m_pathContent;

	ScraperParser m_parser;

	public Scraper(){
		super();
		
		m_parser = new ScraperParser();
		m_parser.setScraper(this);
	}

	// public static ContentMapping content[] = []

	public Scraper(AddonProps props) {
		super(props);
		init();
	}

	public Scraper(String id){
		super(id);
		init();
	}
	
	
	/**
	 * if the XML root is <error>, throw CScraperError with enclosed
	 * <title>/<message> values
	 * 
	 * TODO test
	 * 
	 * @param rootEl
	 * @throws ScraperError
	 */
	public static void checkScraperError(Element rootEl) throws ScraperError
	{
	  if (rootEl == null || rootEl.getNodeValue() == null || rootEl.getNodeValue().equals("error"))
	    return;
	  
	  Element titleEl = XMLUtils.getFirstChildElement(rootEl, "title");
	  Element messageEl = XMLUtils.getFirstChildElement(rootEl, "message");
	  
	  String sTitle = XMLUtils.getFirstChildValue(titleEl);
	  String sMessage = XMLUtils.getFirstChildValue(messageEl);
	  
	  throw new ScraperError(sTitle, sMessage);
	}

	public CONTENT_TYPE content() {
		return m_pathContent;
	};

	// find album by artist, using fcurl for web fetches
	// returns a list of albums (empty if no match or failure)
	public void FindAlbum(String sAlbum, String sArtist) throws ScraperError
	{
//		std::vector<CMusicAlbumInfo> vcali;
//		  if (IsNoop())
//		    return vcali;


		ArrayList<String> extras = new ArrayList<String>();

		try {
			extras.add(URLEncoder.encode(sAlbum, "UTF-8").replace("+", "%20"));
			extras.add(URLEncoder.encode(sArtist, "UTF-8").replace("+", "%20"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}

		ScraperUrl scurl = new ScraperUrl();

		ArrayList<String> vcsOut = run("CreateAlbumSearchUrl", scurl, extras);

		if (vcsOut.isEmpty() || vcsOut.get(0).length() == 0)
			return;

		scurl.ParseString(vcsOut.get(0));


		// the next function is passed the contents of the returned URL, and returns
		// an empty string on failure; on success, returns XML matches in the form:
		// <results>
		//  <entity>
		//   <title>...</title>
		//   <url>...</url> (with the usual CScraperUrl decorations like post or spoof)
		//   <artist>...</artist>
		//   <year>...</year>
		//   <relevance [scale="..."]>...</relevance> (scale defaults to 1; score is divided by it)
		//  </entity>
		//  ...
		// </results>
		vcsOut = run("GetAlbumSearchResults", scurl, null);
		
		for (String i : vcsOut) {
			Document doc = XMLUtils.getDocumentFromString(i);

			if (doc.getDocumentElement() == null) {
				continue;  // might have more valid results later
			}

			checkScraperError(doc.getDocumentElement());

			NodeList resultsList = doc.getElementsByTagName("results");

			if (resultsList.getLength() < 1)
				continue;

			Element xhResults = (Element) resultsList.item(0);

			
			for (Element pxeAlbum = XMLUtils.getFirstChildElement(xhResults, "entity"); 
					pxeAlbum != null; 
					pxeAlbum = XMLUtils.getNextSiblingElement(pxeAlbum, "entity")) {
				
				
				String title = XMLUtils.getFirstChildValue(pxeAlbum, "title");
				
				// Title
				if (title.length() == 0)
					continue;
				

				String artist = XMLUtils.getFirstChildValue(pxeAlbum, "artist");
				String albumName;
				
				if (artist.length() > 0)
					albumName = String.format("%s - %s", artist, title);
				else
					albumName = title;
					
				
				String year = XMLUtils.getFirstChildValue(pxeAlbum, "year");

				if (artist.length() > 0)
					albumName = String.format("%s (%s)", albumName, year);


				// if no URL is provided, use the URL we got back from CreateAlbumSearchUrl
		        // (e.g., in case we only got one result back and were sent to the detail page)
				Element pxeLink = XMLUtils.getFirstChildElement(pxeAlbum, "url");

				ScraperUrl sUrlAlbum = new ScraperUrl();
				if (pxeLink == null) 
					sUrlAlbum.ParseString(scurl.m_xml);


				for (; pxeLink != null && pxeLink.getFirstChild() != null; pxeLink = XMLUtils
						.getNextSiblingElement(pxeLink, "url"))
					sUrlAlbum.ParseElement(pxeLink);
				
				
				if (sUrlAlbum.m_url.isEmpty())
					continue;
				
//				CMusicAlbumInfo ali(sTitle, sArtist, sAlbumName, scurlAlbum);
				
				Element pxeRel = XMLUtils.getFirstChildElement(pxeAlbum, "relevance");
		        if (pxeRel != null && XMLUtils.getFirstChildElement(pxeRel) != null)
		        {
		        	String szScale = XMLUtils.getAttribute(pxeRel, "scale");
		          float flScale = szScale != null ? Float.parseFloat(szScale) : 1;
//		          ali.SetRelevance(float(atof(pxeRel->FirstChild()->Value())) / flScale);
		        }
		        
		        
//		        vcali.push_back(ali);
			}
		}
		
		
//		return vcali;
	}

	public ArrayList<ScraperUrl> FindMovie(String movie, boolean fFirst)
			throws ScraperError, UnsupportedEncodingException {

		Util util = Util.getInstance();

		Map<String, String> result = util.CleanString(movie, true, fFirst);

		String sTitle = result.get("title");
		String sYear = result.get("year");
//		String sTitleYear = result.get("titleAndYear");

		ArrayList<ScraperUrl> vcscurl = new ArrayList<ScraperUrl>();

		//		if (IsNoop())
		//			return vcscurl;

		if (!fFirst)
			sTitle.replace('-',' ');

		ArrayList<String> vcsIn = new ArrayList<String>();
		//		  g_charsetConverter.utf8To(SearchStringEncoding(), sTitle, vcsIn[0]);


		vcsIn.add(URLEncoder.encode(sTitle, "UTF-8").replace("+", "%20"));

		if (sYear != null && sYear.length() != 0)
			vcsIn.add(sYear);


		// request a search URL from the title/filename/etc.
		ScraperUrl scurl = new ScraperUrl();
		ArrayList<String> vcsOut = run("CreateSearchUrl", scurl, vcsIn);

		if (vcsOut.isEmpty())
		{
			//		    CLog::Log(LOGDEBUG, "%s: CreateSearchUrl failed", __FUNCTION__);
					    throw new ScraperError();
		}


		scurl.ParseString(vcsOut.get(0));
		//		  scurl.ParseString("<url>http://api.tmdb.org/3/search/movie?api_key=57983e31fb435df4df77afb854740ea9&amp;query=Mission%20Impossible%20Ghost%20Protocol&amp;year=2011&amp;language=en</url>");


		// do the search, and parse the result into a list
		vcsIn.clear();
		vcsIn.add(scurl.m_url.get(0).m_url);
		vcsOut = run("GetSearchResults", scurl, vcsIn);

		if (vcsOut == null)
			return null;
		
		boolean fSort = true;
		boolean fResults = false;
		ArrayList<String> stsDupeCheck = new ArrayList<String>();

		for (String i : vcsOut) {
			Document doc = XMLUtils.getDocumentFromString(i);

			if (doc.getDocumentElement() == null) {
				continue;  // might have more valid results later
			}

			checkScraperError(doc.getDocumentElement());

			NodeList resultsList = doc.getElementsByTagName("results");

			if (resultsList.getLength() < 1)
				continue;

			Element xhResults = (Element) resultsList.item(0);

			fResults = true;  // even if empty

			// we need to sort if returned results don't specify 'sorted="yes"'
			if (fSort)
			{
				String sorted = XMLUtils.getAttribute(xhResults, "sorted");
				if (sorted != null)
					fSort = !sorted.equalsIgnoreCase("yes");
			}

			for (Element pxeMovie = XMLUtils.getFirstChildElement(xhResults, "entity"); 
					pxeMovie != null; 
					pxeMovie = XMLUtils.getNextSiblingElement(pxeMovie, "entity")) {


				ScraperUrl scurlMovie = new ScraperUrl();

				// ID
				Element pxnId = XMLUtils.getFirstChildElement(pxeMovie, "id");

				if (pxnId == null || pxnId.getFirstChild() == null)
					continue;

				scurlMovie.strId = pxnId.getFirstChild().getNodeValue();
				
				Element pxnTitle = XMLUtils.getFirstChildElement(pxeMovie, "title");

				// Title
				if (pxnTitle == null || pxnTitle.getFirstChild() == null)
					continue;

				scurlMovie.strTitle = pxnTitle.getFirstChild().getNodeValue();

				// Link
				Element pxeLink = XMLUtils.getFirstChildElement(pxeMovie, "url");

				if (pxeLink == null || pxeLink.getFirstChild() == null) 
					continue;


				for (; pxeLink != null && pxeLink.getFirstChild() != null; pxeLink = XMLUtils
						.getNextSiblingElement(pxeLink, "url"))

					scurlMovie.ParseElement(pxeLink);


				// calculate the relevance of this hit
				String sCompareTitle = StringUtils.lowerCase(scurlMovie.strTitle);
				String sMatchTitle = StringUtils.lowerCase(sTitle);
				
				
				/*
				 * Identify the best match by performing a fuzzy string compare on the search term and
				 * the result. Additionally, use the year (if available) to further refine the best match.
				 * An exact match scores 1, a match off by a year scores 0.5 (release dates can vary between
				 * countries), otherwise it scores 0.
				 */
				Element pxnYear = XMLUtils.getFirstChildElement(pxeMovie, "year");

				String sCompareYear = "";
				double yearScore = 0;

				if (pxnYear != null && pxnYear.getFirstChild() != null) {
					sCompareYear = pxnYear.getFirstChild().getNodeValue();

					if (sYear.length() != 0 && sCompareYear.length() != 0)
						yearScore = Math.max(0.0, 1-0.5* Math.abs(Integer.parseInt(sYear)-Integer.parseInt(sCompareYear)));

				}
				
				scurlMovie.relevance = CppUtils.fstrcmp(sMatchTitle, sCompareTitle, 0.0) + yearScore;


				// reconstruct a title for the user

				if (sCompareYear.length() != 0)
					scurlMovie.strTitle = String.format("%s (%s)",
							scurlMovie.strTitle, sCompareYear);


				if (!stsDupeCheck.contains(scurlMovie.m_url.get(0).m_url + " " + scurlMovie.strTitle)) {
					stsDupeCheck.add(scurlMovie.m_url.get(0).m_url + " " + scurlMovie.strTitle);
					vcscurl.add(scurlMovie);
				}	
			}

		}

		if (!fResults)
			throw new ScraperError();

		Collections.sort(vcscurl, new RelevanceSortComparator());
		
		return vcscurl;
	}

	// fetch list of episodes from URL (from video database)
	public ArrayList<Episode> getEpisodeList(ScraperUrl scurl)
	{
		ArrayList<Episode> vcep = new ArrayList<Episode>();
		
	  if (scurl == null || scurl.m_url.isEmpty())
	    return vcep;


	  ArrayList<String> vcsIn;
	  vcsIn.add(scurl.m_url.get(0).m_url);
	  
	  ArrayList<String> vcsOut = run("GetEpisodeList", scurl, vcsIn);

	  // parse the XML response
	  for (String i : vcsOut)
	  {
	    Document doc = XMLUtils.getDocumentFromString(i);
	    if (doc == null || doc.getDocumentElement() == null)
	    {
	      continue;
	    }

	    Element root = doc.getDocumentElement();
	    Element epGuide = XMLUtils.getFirstChildElement(root, "episodeguide");
	    
	    if (epGuide == null)
	    	continue;

	    for (Element pxeMovie = XMLUtils.getFirstChildElement(epGuide, "episode")
	      ; pxeMovie != null; pxeMovie = XMLUtils.getNextSiblingElement(pxeMovie, "episode"))
	    {
	      
	      Element pxeLink = XMLUtils.getFirstChildElement(pxeMovie, "url");
	      Element seasonEl = XMLUtils.getFirstChildElement(pxeMovie, "season");
	      Element epEl = XMLUtils.getFirstChildElement(pxeMovie, "epnum");
	      Element titleEl = XMLUtils.getFirstChildElement(pxeMovie, "title");

	      Episode ep;
	      ep.season = XMLUtils.getFirstChildValue_int(seasonEl);
	      String strEpNum = XMLUtils.getFirstChildValue(seasonEl);
	      

	      if (pxeLink != null && ep.season != null && strEpNum != null && !strEpNum.isEmpty())
	      {
	        ScraperUrl scurlEp = ep.scraperUrl;
	        
	        int dot = strEpNum.indexOf(".");
	        
	        ep.episode = Integer.parseInt(strEpNum);
	        ep.subEpisode = (dot > -1) ? Integer.parseInt(strEpNum.substring(dot + 1)) : 0;
	        
//	        if (XMLHelper.getFirstChildValue(titleEl) == null || scurlEp.strTitle.isEmpty() )
//	            scurlEp.strTitle = g_localizeStrings.Get(416);
	        
	        scurlEp.strId = XMLUtils.getFirstChildValue(pxeMovie, "id");

	        for ( ; pxeLink != null && XMLUtils.getFirstChildElement(pxeLink) != null; 
	        		pxeLink = XMLUtils.getNextSiblingElement(pxeLink, "url"))
	          scurlEp.ParseElement(pxeLink);

	        // date must be the format of yyyy-mm-dd
//	        ep.date.SetValid(FALSE);
//	        String Date;
//	        if (XMLUtils::GetString(pxeMovie, "aired", sDate) && sDate.length() == 10)
//	        {
//	          tm tm;
//	          if (strptime(sDate, "%Y-%m-%d", &tm))
//	            ep.cDate.SetDate(1900+tm.tm_year, tm.tm_mon + 1, tm.tm_mday);
//	        }
	        vcep.add(ep);
	      }
	    }
	  }

	  return vcep;
	}

	/**
	 * Takes a list of XML strings and returns the first valid ScraperUrl
	 * object. Format:  <url>...</url> or <url>...</url><id>...</id>.
	 * 
	 * TODO consider moving this to ScraperUrl class.
	 * TODO check resemblance with ParseEpisodeGuide method of ScraperUrl
	 * 
	 * @param xmlList
	 * @return
	 * @throws ScraperError 
	 */
	private ScraperUrl getUrlFromXML(ArrayList<String> xmlList) throws ScraperError {
		
		if (xmlList == null || xmlList.isEmpty() || xmlList.get(0).isEmpty())
			return null;

		ScraperUrl scurlRet = null;

		// parse returned XML: either <error> element on error, blank on failure,
		// or <url>...</url> or <url>...</url><id>...</id> on success
		for (String i : xmlList) {
			Document doc;

			doc = XMLUtils.getDocumentFromString("<root>" + i + "</root>");


			if (doc.getDocumentElement() == null) {
				continue;  // might have more valid results later
			}

			Element rootEl = doc.getDocumentElement();

			if (XMLUtils.getFirstChildElement(rootEl).getNodeName().equals("details"))
				rootEl = XMLUtils.getFirstChildElement(rootEl);

			checkScraperError(doc.getDocumentElement());

			/*
		       NOTE: Scrapers might return invalid xml with some loose
		       elements (eg. '<url>http://some.url</url><id>123</id>').
		       Since XMLUtils::GetString() is assuming well formed xml
		       with start and end-tags we're not able to use it.
		       Check for the desired Elements instead.
			 */


			Element pxeUrl = null;
			Element pId = null;

			pId = XMLUtils.getFirstChildElement(rootEl, "id");
			pxeUrl = XMLUtils.getFirstChildElement(rootEl, "url");

			scurlRet = new ScraperUrl();

			if (pId != null && pId.getFirstChild() != null)
				scurlRet.strId = pId.getFirstChild().getNodeValue();

			if (pxeUrl != null && XMLUtils.getAttribute(pxeUrl, "function") != null)
				continue;

			if (pxeUrl != null)
				scurlRet.ParseElement(pxeUrl);
			else
				continue;

			break;
		}

		return scurlRet;
	}
	
	/**
	 * takes URL; returns video details
	 * 
	 * @param scurl
	 * @param fMovie true for movie, false for episode
	 * @return
	 * @throws ScraperError
	 */
	public VideoInfoTag getVideoDetails(ScraperUrl scurl, boolean fMovie)
			throws ScraperError {

		VideoInfoTag video = new VideoInfoTag();
		String sFunc = fMovie ? "GetDetails" : "GetEpisodeDetails";
		
		ArrayList<String> vcsIn = new ArrayList<String>();
		vcsIn.add(scurl.strId);
		vcsIn.add(scurl.m_url.get(0).m_url);
		
		ArrayList<String> vcsOut = run(sFunc, scurl, vcsIn);
		
		for (String i : vcsOut) {
			Document doc = XMLUtils.getDocumentFromString(i);

			if (doc.getDocumentElement() == null) {
				continue;  // might have more valid results later
			}
			
			
			NodeList resultsList = doc.getElementsByTagName("details");

			if (resultsList.getLength() < 1)
				continue;

			Element pxeDetails = (Element) resultsList.item(0);
			
			video.load(pxeDetails, true/*fChain*/, true);
		}
		
		return video;
	}


	private void init() {
		m_parser = new ScraperParser();
		m_parser.setScraper(this);

		if (getType() == null)
			return;
		
		switch (getType()) {
		case ADDON_SCRAPER_ALBUMS:
			m_pathContent = CONTENT_TYPE.CONTENT_ALBUMS;
			break;
		case ADDON_SCRAPER_ARTISTS:
			m_pathContent = CONTENT_TYPE.CONTENT_ARTISTS;
			break;
		case ADDON_SCRAPER_MOVIES:
			m_pathContent = CONTENT_TYPE.CONTENT_MOVIES;
			break;
		case ADDON_SCRAPER_MUSICVIDEOS:
			m_pathContent = CONTENT_TYPE.CONTENT_MUSICVIDEOS;
			break;
		case ADDON_SCRAPER_TVSHOWS:
			m_pathContent = CONTENT_TYPE.CONTENT_TVSHOWS;
			break;
		default:
			m_pathContent = CONTENT_TYPE.CONTENT_NONE;
			break;
		}
	}



	/**
	 * Consider merging this with run
	 * 
	 * @param function
	 * @param scrURL 
	 * @param extras Params
	 * @return
	 */
	public String internalRun(String function, ScraperUrl scrURL, ArrayList<String> extras) {

		// walk the list of input URLs and fetch each into parser parameters
		int i;
		for (i=0;i<scrURL.m_url.size();++i)
		{
			m_parser.m_param[i] = ScraperUrl.Get(scrURL.m_url.get(i),getId());

			if (m_parser.m_param[i] == null || m_parser.m_param[i].length() == 0)
				return "";
		}

		// put the 'extra' parameters into the parser parameter list too
		if (!extras.isEmpty())
		{
			for (int j=0;j<extras.size();++j)
				m_parser.m_param[j+i] = extras.get(j);
		}

		return m_parser.Parse(function,this);
	}

	public boolean isNoop() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	public boolean load() {
		if (m_fLoaded)
			return true;

		boolean result = m_parser.Load(getLibPath());

		if (!result)
			return false;

		ArrayList<String> deps = getDeps();

		for (String temp : deps) {

			if (temp.equals("xbmc.metadata")) {
				continue;
			}

			Addon addon = AddonManager.getAddon(temp);

			if (addon == null) {
				continue;
			}

			Document doc = XMLUtils.getDocument(addon.getLibPath());

			if (addon.getType() == ADDON_TYPE.ADDON_SCRAPER_LIBRARY && doc != null)
				m_parser.AppendDocument(doc);
		}

		m_fLoaded = true;
		return true;
	}
	
	
	
	
	/** 
	 * pass in contents of .nfo file; returns URL (null if none found)
	 * and may populate strId
	 * 
	 * @param nfoContent
	 * @return
	 * @throws ScraperError 
	 */
	
	public ScraperUrl nfoUrl(String nfoContent) throws ScraperError
	{

		if (isNoop())
			return null;


		// scraper function takes contents of .nfo file, returns XML (see below)
		ArrayList<String> paramList = new ArrayList<String>();
		paramList.add(nfoContent);

		ScraperUrl scurl = new ScraperUrl();

		ArrayList<String> outputList = run("NfoUrl", scurl, paramList);
		
		return getUrlFromXML(outputList);
	}
	
	/**
	 * This function is exactly same as NfoUrl function. Only difference is the
	 * RUN call.
	 * 
	 * TODO test run and merge codes.
	 * 
	 * @param externalID
	 * @return
	 * @throws ScraperError 
	 */
	public ScraperUrl resolveIDToUrl(String externalID) throws ScraperError
	{
		if (isNoop())
			return null;

		// scraper function takes an external ID, returns XML (see below)
		ArrayList<String> paramList = new ArrayList<String>();
		paramList.add(externalID);

		ScraperUrl scurl = new ScraperUrl();

		ArrayList<String> outputList = run("NfoUrl", scurl, paramList);
		
		return getUrlFromXML(outputList);
	}

	
	/**
	 * returns a ArrayList of strings: the first is the XML output by the
	 * function; the rest is XML output by chained functions, possibly
	 * recursively
	 * 
	 * @param function
	 * @param scrURL
	 * @param extras
	 * @return
	 * @throws ScraperError 
	 */
	public ArrayList<String> run(String function, ScraperUrl scrURL,
			ArrayList<String> extras) throws ScraperError {

		if (!load())
			return null;

		String strXML = internalRun(function, scrURL, extras);

		if (strXML == null || strXML.isEmpty())
		{
			//			if (function.equals("NfoUrl") && function.equals("ResolveIDToUrl"))
			//				Log.v("Scrapper", "Run: Unable to parse web site");

						throw new ScraperError("Run: Unable to parse web site");
		}

		Document doc = XMLUtils.getDocumentFromString(strXML);

		if(!doc.hasChildNodes())
			return null;

		ArrayList<String> result = new ArrayList<String>();
		result.add(strXML);

		Element xchain = XMLUtils.getFirstChildElement(doc.getDocumentElement());

		while (xchain != null) {

			if (!xchain.getNodeName().equals("url") && !xchain.getNodeName().equals("chain")) {
				xchain = XMLUtils.getNextSiblingElement(xchain);
				continue;
			}


			// <chain|url function="...">param</>
			String szFunction = XMLUtils.getAttribute(xchain, "function");

			if (szFunction != null)
			{
				ScraperUrl scrURL2 = new ScraperUrl();
				ArrayList<String> extras2 = new ArrayList<String>();

				// for <chain>, pass the contained text as a parameter; for <url>, as URL content
				if (xchain.getNodeName().equals("chain"))
				{
					Node paramNode = xchain.getFirstChild();

					if (paramNode != null)
						extras2.add(paramNode.getNodeValue());
				}
				else
					scrURL2.ParseElement(xchain);


				// Fix for empty chains. $$1 would still contain the
				// previous value as there is no child of the xml node. 
				// since $$1 will always either contain the data from an 
				// url or the parameters to a chain, we can safely clear it here
				// to fix this issue
				m_parser.m_param[0] = null;
				ArrayList<String> result2 = run(szFunction,scrURL2, extras2);
				
				if(result2 != null)
					result.addAll(result2);
			}

			xchain = XMLUtils.getNextSiblingElement(xchain);
		}

		return result;
	}
	
	
	
	public ADDON_TYPE scraperTypeFromContent(CONTENT_TYPE content) {
		switch (content) {
		case CONTENT_ALBUMS:
			return ADDON_TYPE.ADDON_SCRAPER_ALBUMS;
		case CONTENT_ARTISTS:
			return ADDON_TYPE.ADDON_SCRAPER_ARTISTS;
		case CONTENT_MOVIES:
			return ADDON_TYPE.ADDON_SCRAPER_MOVIES;
		case CONTENT_MUSICVIDEOS:
			return ADDON_TYPE.ADDON_SCRAPER_MUSICVIDEOS;
		case CONTENT_TVSHOWS:
			return ADDON_TYPE.ADDON_SCRAPER_TVSHOWS;
		default:
			return ADDON_TYPE.ADDON_UNKNOWN;
		}
	}
	
	String searchStringEncoding() { 
		return m_parser.GetSearchStringEncoding(); 
	}

	
	public String translateContent(CONTENT_TYPE type, boolean pretty) {
		return null;
	}

	public CONTENT_TYPE translateContent(String str) {
		return null;
	}
}
