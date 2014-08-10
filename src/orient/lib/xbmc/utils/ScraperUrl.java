package orient.lib.xbmc.utils;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/*
 * XBMC code downloaded on: 4/7/14
 **/

public class ScraperUrl {

	public String id;
	public String title;
	public String spoof; // for backwards compatibility only!
	public String xml;
	public double relevance;
	
	public ArrayList<UrlEntry> urlList;

	/**
	 * This method from xbmc has been broken up into multiple methods: Get,
	 * ExecuteHttpRequest, ProcessHttpResponse and SaveCache.
	 * 
	 * Also, to save contents to cache, the cache directory has to be set
	 * through SetCacheDir.
	 */
	public static String get(UrlEntry scrURL, String cacheContext) {
		ScraperUrlGet urlGetter = new ScraperUrlGet(scrURL, cacheContext);

		return urlGetter.get();
	}

	public ScraperUrl() {
		urlList = new ArrayList<UrlEntry>();
		relevance = 0;
	}

	public ScraperUrl(String strUrl) {
		urlList = new ArrayList<UrlEntry>();
		relevance = 0;

		parseString(strUrl);
	}

	public void clear() {
		id = null;
		title = null;
		spoof = null;
		xml = null;
		relevance = 0;
		urlList.clear();
	}

	public UrlEntry getFirstThumb(String type) {

		for (UrlEntry temp : urlList) {
			if (temp.type == URL_TYPE.GENERAL
					&& (type == null || type.isEmpty() || type == "thumb" || temp.aspect == type))
				return temp;
		}

		UrlEntry result = new UrlEntry();
		result.type = URL_TYPE.GENERAL;
		result.isPost = false;
		result.isGZip = false;
		result.season = -1;
		return result;
	}

	public int getMaxSeasonThumb() {
		int maxSeason = 0;

		for (UrlEntry temp : urlList) {
			if (temp.type == URL_TYPE.SEASON && temp.season > 0
					&& temp.season > maxSeason)
				maxSeason = temp.season;
		}

		return maxSeason;
	}

	public UrlEntry getSeasonThumb(int season, String type) {

		for (UrlEntry temp : urlList) {
			if (temp.type == URL_TYPE.SEASON
					&& temp.season == season
					&& (type == null || type.length() == 0 || type == "thumb" || temp.aspect == type))
				return temp;
		}

		UrlEntry result = new UrlEntry();
		result.type = URL_TYPE.GENERAL;
		result.isPost = false;
		result.isGZip = false;
		result.season = -1;
		return result;
	}

	@SuppressWarnings("deprecation")
	public String getThumbURL(UrlEntry entry) {
		if (entry.spoof.length() == 0)
			return entry.url;

		return entry.url + "|Referer=" + URLEncoder.encode(entry.spoof);
	}

	public ArrayList<String> getThumbURLs(ArrayList<String> thumbs,
			String type, int season) {
		for (UrlEntry temp : urlList) {
			if (temp.aspect == type || type.length() == 0 || type == "thumb"
					|| temp.aspect.length() == 0) {
				if ((temp.type == URL_TYPE.GENERAL && season == -1)
						|| (temp.type == URL_TYPE.SEASON && temp.season == season))
					thumbs.add(getThumbURL(temp));
			}
		}

		return thumbs;
	}

	public boolean parse() {
		String strToParse = xml;
		xml = null;
		// return OldParseXml(strToParse);
		return parseString(strToParse);
	}

	public boolean parseElement(Element element) {

		if (element == null || element.getFirstChild() == null
				|| element.getFirstChild().getNodeValue() == null)
			return false;

		xml = XMLUtils.nodeToString(element);// pass in the root


		UrlEntry url = new UrlEntry();

		// url
		Node child = element.getFirstChild();// XMLHelper.getFirstChildElement(element);
		
		// to get rid of whitespace nodes i.e. \n
		while (child != null && child.getNodeName() == "#text" && child.getNodeValue().trim().length() == 0)
			child = child.getNextSibling();
		
		if (child != null) {
			
			url.url = child.getNodeValue();

			if (url.url == null || url.url.length() == 0)
				url.url = child.getNodeName();
			
			url.url = StringEscapeUtils.unescapeXml(url.url);
		}
		

		// spoof
		url.spoof = XMLUtils.getAttribute(element, "spoof");

		// cache
		url.cache = XMLUtils.getAttribute(element, "cache");

		// aspect
		url.aspect = XMLUtils.getAttribute(element, "aspect");
		
		// thumb
		url.thumb = XMLUtils.getAttribute(element, "thumb");

		// post
		String szPost = XMLUtils.getAttribute(element, "post");

		if (szPost != null && szPost.equals("yes"))
			url.isPost = true;
		else
			url.isPost = false;

		// post
		String szIsGz = XMLUtils.getAttribute(element, "gzip");

		if (szIsGz != null && szIsGz.equals("yes"))
			url.isGZip = true;
		else
			url.isGZip = false;

		// type & season
		String szType = XMLUtils.getAttribute(element, "type");

		url.type = URL_TYPE.GENERAL;
		url.season = -1;

		if (szType != null && szType.equals("season")) {
			url.type = URL_TYPE.SEASON;

			String szSeason = XMLUtils.getAttribute(element, "season");

			if (szSeason != null)
				url.season = Integer.parseInt(szSeason);
		}

		urlList.add(url);

		return true;
	}

	// XML format is of strUrls is:
	// <TAG><url>...</url>...</TAG> (parsed by ParseElement) or <url>...</url>
	// (ditto)
	public boolean parseEpisodeGuide(String strUrls) {
		if (strUrls.length() == 0)
			return false;

		Document doc;

		try {

			/*
			 * strUrl is coming from internal sources (usually generated by
			 * scraper or from database) so strUrl is always in UTF-8
			 */
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new InputSource(new StringReader(strUrls)));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
			return false;
		}

		Element pElement = doc.getDocumentElement();

		if (pElement == null) {
			return false;
		}

		Element link = XMLUtils.getFirstChildElement(pElement, "episodeguide");

		if (link == null)
			return false;

		if (XMLUtils.getFirstChildElement(link, "url") != null) {
			link = XMLUtils.getFirstChildElement(link, "url");

			while (link != null) {
				parseElement(link);
				link = XMLUtils.getNextSiblingElement(link, "url");
			}

		} else if (link.getFirstChild() != null
				&& link.getFirstChild().getNodeValue() != null) {
			parseElement(link);
		}

		return true;
	}

	// Just to save code breaks
	public boolean parseString(String strUrl) {

		Document doc;

		try {

			/*
			 * strUrl is coming from internal sources (usually generated by
			 * scraper or from database) so strUrl is always in UTF-8
			 */
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new InputSource(new StringReader(strUrl)));
		} catch (SAXException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return false;
		}

		Element pElement = doc.getDocumentElement();

		if (pElement == null) {
			UrlEntry url = new UrlEntry();
			url.url = strUrl;
			urlList.add(url);
			xml = strUrl;
		} else {
			while (pElement != null) {
				parseElement(pElement);
				pElement = XMLUtils.getNextSiblingElement(pElement,
						pElement.getNodeValue());
			}
		}

		return true;
	}

}