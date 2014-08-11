package orient.lib.xbmc.video;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Element;

import orient.lib.xbmc.InfoTag;
import orient.lib.xbmc.utils.ScraperUrl;
import orient.lib.xbmc.utils.StreamDetail;
import orient.lib.xbmc.utils.StreamDetailAudio;
import orient.lib.xbmc.utils.StreamDetailSubtitle;
import orient.lib.xbmc.utils.StreamDetailVideo;
import orient.lib.xbmc.utils.StreamDetails;
import orient.lib.xbmc.utils.XMLUtils;

public class VideoInfoTag extends InfoTag {

	/**
	 * the base path of the video, for folder-based lookups
	 */
	public String basePath;

	/**
	 * the parent path id where the base path of the video lies
	 */
	public int parentPathID;

	public ArrayList<String> artist;
	public ArrayList<String> country;
	public ArrayList<String> director;
	public ArrayList<String> genre;
	public ArrayList<String> showLink;
	public ArrayList<String> studio;
	public ArrayList<String> tags;
	public ArrayList<String> writingCredits;
	public ArrayList<ActorInfo> cast;

	public String tagline;
	public String plotOutline;
	public String trailer;
	public String plot;
	public String title;
	public String sortTitle;
	public String votes;
	public String set;
	public String file;
	public String path;
	public String IMDBNumber;
	public String MPAARating;
	public String fileNameAndPath;
	public String originalTitle;
	public String episodeGuide;
	public String status;
	public String productionCode;
	public String showTitle;
	public String album;
	public String showPath;
	public String uniqueId;

	public int setId;
	public int playCount;
	public int top250;
	public int year;
	public int season;
	public int episode;
	public int dbId;
	public int fileId;
	public int specialSortSeason;
	public int specialSortEpisode;
	private int specialAfterSeason;
	public int track;
	public int bookmarkId;
	public int idShow;
	public int idSeason;
	/** duration in seconds */
	public int duration;

	public float rating;
	public float epBookmark;

	public Date premiered;
	public Date firstAired;
	public Date lastPlayed;
	public Date dateAdded;

	public ScraperUrl pictureUrl;
	public ScraperUrl fanartUrl; // instead of CFanart fanart;
	public StreamDetails streamDetails;
	public Bookmark resumePoint;

	// MediaType type;

	public VideoInfoTag() {
		super();

		initXmlTagMapping();
		reset();
	}

	protected void afterParseXML () {
		super.afterParseXML();
		
		// Special Season
		if (specialAfterSeason > 0) {
			specialSortSeason = specialAfterSeason;
			specialSortEpisode = 0x1000; // should be more than any realistic
		}
	}

	@Override
	protected void initXmlTagMapping() {
		// Strings
		xmlTagMapping.put("id", "IMDBNumber");
		xmlTagMapping.put("title", "title");
		xmlTagMapping.put("originaltitle", "originalTitle");
		xmlTagMapping.put("showtitle", "showTitle");
		xmlTagMapping.put("sorttitle", "sortTitle");
		xmlTagMapping.put("uniqueid", "uniqueId");
		xmlTagMapping.put("votes", "votes");
		xmlTagMapping.put("outline", "plotOutline");
		xmlTagMapping.put("plot", "plot");
		xmlTagMapping.put("tagline", "tagline");
		xmlTagMapping.put("file", "file");
		xmlTagMapping.put("path", "path");
		xmlTagMapping.put("filenameandpath", "fileNameAndPath");
		xmlTagMapping.put("status", "status");
		xmlTagMapping.put("code", "productionCode");
		xmlTagMapping.put("album", "album");
		xmlTagMapping.put("artist", "artist");
		xmlTagMapping.put("trailer", "trailer");
		xmlTagMapping.put("basepath", "basePath");
		xmlTagMapping.put("mpaa", "MPAARating");
		xmlTagMapping.put("set", "set");

		// Integers
		xmlTagMapping.put("year", "year");
		xmlTagMapping.put("top250", "top250");
		xmlTagMapping.put("season", "season");
		xmlTagMapping.put("episode", "episode");
		xmlTagMapping.put("track", "track");
		xmlTagMapping.put("displayseason", "specialSortSeason");
		xmlTagMapping.put("displayepisode", "specialSortEpisode");
		xmlTagMapping.put("specialAfterSeason", "specialAfterSeason");
		xmlTagMapping.put("playcount", "playCount");
		xmlTagMapping.put("runtime", "duration");

		// Floats
		xmlTagMapping.put("rating", "rating");
		xmlTagMapping.put("epbookmark", "epBookmark");

		// Date
		xmlTagMapping.put("lastplayed", "lastPlayed");
		xmlTagMapping.put("premiered", "premiered");
		xmlTagMapping.put("aired", "firstAired");
		xmlTagMapping.put("dateadded", "dateAdded");

		// ArrayLists
		xmlTagMapping.put("genre", "genre");
		xmlTagMapping.put("country", "country");
		xmlTagMapping.put("credits", "writingCredits");
		xmlTagMapping.put("director", "director");
		xmlTagMapping.put("showlink", "showLink");
		xmlTagMapping.put("tag", "tags");
		xmlTagMapping.put("studio", "studio");
		xmlTagMapping.put("actor", "cast");

		// Other
		xmlTagMapping.put("thumb", "pictureUrl");
		xmlTagMapping.put("fanart", "fanartUrl");
		xmlTagMapping.put("fileinfo", "streamDetails");
		xmlTagMapping.put("resume", "resumePoint");
	}

	public void merge(VideoInfoTag that) {
		// TODO implement this
	}

	@SuppressWarnings("unchecked")
	protected void onParseXMLArrayItem(String fieldName, Element valueEl) {
		super.onParseXMLArrayItem(fieldName, valueEl);
		
		String genericType = getGenericTypeName(fieldName);

		if (genericType.equals("ActorInfo")) {
			ArrayList<ActorInfo> arr;
			Field f;

			try {
				f = getClass().getDeclaredField(fieldName);
				f.setAccessible(true);

				if (f.get(this) == null) {
					arr = new ArrayList<ActorInfo>();
				} else {
					arr = (ArrayList<ActorInfo>) f.get(this);
				}

				ActorInfo item = new ActorInfo();
				item.parseElement(valueEl);

				arr.add((ActorInfo) item);
				getClass().getDeclaredField(fieldName).set(this, arr);

			} catch (NoSuchFieldException | IllegalAccessException
					| IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

	}
	
	/**
	 * Used by the parseXML method. Performs any further pre-processing on the
	 * incoming value if required.
	 * 
	 * @param el
	 *            The element to extract the value from
	 * @return The processed value
	 */
	protected int onParseXMLItemInt(Element el) {

		super.onParseXMLItemInt(el);
		
		String tag = el.getNodeName();
		int value = XMLUtils.getFirstChildValue_int(el);

		// Runtime and Duration
		if (tag == "runtime") {
			value = value * 60;
		}
		// Rating
		else if (tag == "rating") {

			try {
				int max_value = Integer.parseInt(XMLUtils.getAttribute(el,
						"max"));

				if (max_value >= 1)
					value = value / max_value * 10; // Normalise the Movie
													// Rating to between 1 and
													// 10
			} catch (NumberFormatException e) {
			}
		}

		return value;
	}

	/**
	 * Used by the parseXML method. Performs processing on any unrecognized 
	 * types i.e. Any type other than String, int, float, date, ArrayList
	 * 
	 * @param el
	 *            The element to extract the value from
	 */
	protected void onParseXMLItemOther(Element el) {

		super.onParseXMLItemOther(el);
		
		String tag = el.getNodeName();

		// Thumbs
		if (tag == "thumb") {

			if (pictureUrl == null)
				pictureUrl = new ScraperUrl();

			pictureUrl.parseElement(el);
		}
		if (tag == "fanart") {

			if (fanartUrl == null)
				fanartUrl = new ScraperUrl();

			fanartUrl.parseElement(el);
		} else if (tag == "fileinfo") {
			Element streamEl = XMLUtils.getFirstChildElement(el,
					"streamdetails");

			if (streamEl == null)
				return;

			Element typeEl = XMLUtils.getFirstChildElement(streamEl);

			while (typeEl != null) {

				StreamDetail p;
				switch (typeEl.getNodeName()) {
				case "video":
					p = new StreamDetailVideo();
					break;

				case "audio":
					p = new StreamDetailAudio();
					break;

				case "subtitle":
					p = new StreamDetailSubtitle();
					break;

				default:
					return;
				}

				p.parseElement(typeEl);

				if (streamDetails == null)
					streamDetails = new StreamDetails();

				streamDetails.AddStream(p);

				typeEl = XMLUtils.getNextSiblingElement(typeEl);
			}
		} else if (tag == "resume") {
			Element position = XMLUtils.getFirstChildElement(el, "position");

			if (position != null) {
				String value = XMLUtils.getFirstChildValue(position);

				if (value != null && !value.trim().isEmpty())
					resumePoint.timeInSeconds = Double
							.parseDouble(value.trim());
			}

			Element total = XMLUtils.getFirstChildElement(el, "total");

			if (total != null) {
				String value = XMLUtils.getFirstChildValue(total);

				if (value != null && !value.trim().isEmpty())
					resumePoint.totalTimeInSeconds = Double.parseDouble(value);
			}
		}

		return;
	}

	/**
	 * Overwrites the current info tag with the given Info Tag
	 * 
	 * @param that
	 */
	public void overwrite(VideoInfoTag that) {

		Iterator<Entry<String, String>> it = xmlTagMapping.entrySet()
				.iterator();

		while (it.hasNext()) {

			Map.Entry<String, String> pair = (Entry<String, String>) it.next();

			String fieldName = (String) pair.getValue();
			String fieldType;

			try {
				fieldType = getFieldType(fieldName);

				Field thisField = this.getClass().getDeclaredField(fieldName);
				Field thatField = that.getClass().getDeclaredField(fieldName);

				switch (fieldType) {

				case "int":

					// if (thatField.getInt(this) > 0)
					thisField.set(this, thatField.getInt(that));

					break;
				case "float":

					// if (thatField.getFloat(this) > 0)
					thisField.set(this, thatField.getFloat(that));

					break;
				case "String":
				case "Date":
				case "ArrayList":
				default:

					// if (thatField.get(that) != null)
					thisField.set(this, thatField.get(that));

					break;
				}
			} catch (NoSuchFieldException | IllegalAccessException
					| IllegalArgumentException e) {
				e.printStackTrace();
				continue;
			}

			it.remove(); // avoids a ConcurrentModificationException
		}
	}

	public void reset() {
		// Array Lists
		director = null;
		writingCredits = null;
		genre = null;
		country = null;
		cast = null;
		tags = null;
		studio = null;
		artist = null;
		showLink = null;

		// Other
		// fanart.m_xml.clear();
		streamDetails = null;
		resumePoint = new Bookmark();
		resumePoint.type = Bookmark.Type.RESUME;
		// type.clear();

		// Strings
		tagline = null;
		plotOutline = null;
		plot = null;
		pictureUrl = null;
		title = null;
		showTitle = null;
		originalTitle = null;
		sortTitle = null;
		votes = null;
		set = null;
		file = null;
		path = null;
		IMDBNumber = null;
		MPAARating = null;
		fileNameAndPath = null;
		status = null;
		productionCode = null;
		album = null;
		trailer = null;
		uniqueId = null;
		basePath = null;
		showPath = null;

		// Integers
		setId = -1;
		top250 = 0;
		year = 0;
		season = -1;
		episode = -1;
		specialSortSeason = -1;
		specialSortEpisode = -1;
		specialAfterSeason = 0;
		dbId = -1;
		fileId = -1;
		bookmarkId = -1;
		track = -1;
		duration = 0;
		playCount = 0;
		epBookmark = 0;
		parentPathID = -1;
		idShow = -1;
		idSeason = -1;

		// Float
		rating = 0.0f;

		// Date
		premiered = null;
		firstAired = null;
		lastPlayed = null;
		dateAdded = null;

	}
}
