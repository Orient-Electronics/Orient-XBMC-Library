package orient.lib.xbmc.video;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.w3c.dom.Element;

import orient.lib.xbmc.utils.ScraperUrl;
import orient.lib.xbmc.utils.StreamDetail;
import orient.lib.xbmc.utils.StreamDetailAudio;
import orient.lib.xbmc.utils.StreamDetailSubtitle;
import orient.lib.xbmc.utils.StreamDetailVideo;
import orient.lib.xbmc.utils.StreamDetails;
import orient.lib.xbmc.utils.XMLUtils;

public class VideoInfoTag {

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
	public int duration; // /< duration in seconds
	
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


	private Map<String, String> xmlTagMapping = new HashMap<String, String>();

	public VideoInfoTag() {
		super();

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
		
		
		
		reset();
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
		//		  fanart.m_xml.clear();
		streamDetails = null;
		resumePoint = new Bookmark();
		resumePoint.type = Bookmark.Type.RESUME;
		//		  type.clear();

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

	/**
	 * Load information to a videoinfotag from an XML element There are three
	 * types of tags supported:
	 * 
	 * <ol>
	 * <li>Single-value tags, such as &lt;title&gt;. These are set if available,
	 * else are left untouched.</li>
	 * <li>Additive tags, such as &lt;set&gt; or &lt;genre&gt;. These are
	 * appended to or replaced (if available) based on the value of the
	 * prioritise parameter. In addition, a clear attribute is available in the
	 * XML to clear the current value prior to appending.</li>
	 * <li>Image tags such as &lt;thumb&gt; and &lt;fanart&gt;. If the
	 * prioritise value is specified, any additional values are prepended to the
	 * existing values.</li>
	 * </ol>
	 * 
	 * @param element
	 *            the root XML element to parse.
	 * @param append
	 *            whether information should be added to the existing tag, or
	 *            whether it should be reset first.
	 * @param prioritise
	 *            if appending, whether additive tags should be prioritised
	 *            (i.e. replace or prepend) over existing values. Defaults to
	 *            false.
	 * 
	 * @see ParseNative
	 */
	public boolean load(Element element, boolean append, boolean prioritise) {
		if (element == null)
			return false;

		if (!append)
			reset();

		try {
			parseNative(element, prioritise);
		} catch (IllegalAccessException | IllegalArgumentException
				| NoSuchFieldException | NullPointerException | ParseException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Parse our native XML format for video info. See Load for a description of
	 * the available tag types.
	 * 
	 * @param element
	 *            the root XML element to parse.
	 * @param prioritise
	 *            whether additive tags should be replaced (or prepended) by the
	 *            content of the tags, or appended to.
	 * @throws ParseException 
	 * @see Load
	 */
	private void parseNative(Element element, boolean prioritise)
			throws IllegalAccessException, IllegalArgumentException,
			NoSuchFieldException, ParseException, NullPointerException {

		Element child = XMLUtils.getFirstChildElement(element);

		while (child != null) {

			String tag = child.getNodeName();

			String memberName;
			String fieldType;

			if (xmlTagMapping.containsKey(tag)) {

				memberName = xmlTagMapping.get(tag);
				fieldType = getFieldType(memberName);

				if (fieldType.equals("String")) 
				{
					setField(memberName, XMLUtils.getFirstChildValue(child));
				} 
				else if (fieldType.equals("int")) 
				{
					setField(memberName, processParsedInt(child));
				} 
				else if (fieldType.equals("float")) 
				{
					setField(memberName, XMLUtils.getFirstChildValue_float(child));
				} 
				else if (fieldType.equals("Date")) 
				{
					String dateStr = XMLUtils.getFirstChildValue(child);
					Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(dateStr);
					setField(memberName, date);
				} 
				else if (fieldType.equals("ArrayList")) 
				{
					addArrayItem(memberName, child);
				}
				else {
					processParsedOther(child);
				}
			}

			child = XMLUtils.getNextSiblingElement(child);
		}

		/////// Post Processing
		 
		// Special Season
		if (specialAfterSeason > 0)
		{
			specialSortSeason = specialAfterSeason;
			specialSortEpisode = 0x1000; // should be more than any realistic
		}
	}
	
	@SuppressWarnings("unchecked")
	private void addArrayItem(String fieldName, Element valueEl) {

		String genericType = getGenericTypeName(fieldName);
		
		
		if (genericType.equals("String")) {
			
			ArrayList<String> arr; 
			Field f;
			
			try {
				f = getClass().getDeclaredField(fieldName);
				f.setAccessible(true);


				if (f.get(this) == null) {
					arr = new ArrayList<String>();
				}
				else {
					arr = (ArrayList<String>) f.get(this);
				}

				String item = XMLUtils.getFirstChildValue(valueEl);

				arr.add((String) item);
				getClass().getDeclaredField(fieldName).set(this, arr);

			} catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		else if (genericType.equals("ActorInfo")) {
			ArrayList<ActorInfo> arr; 
			Field f;
			
			try {
				f = getClass().getDeclaredField(fieldName);
				f.setAccessible(true);
				
				
				if (f.get(this) == null) {
					arr = new ArrayList<ActorInfo>();
				}
				else {
					arr = (ArrayList<ActorInfo>) f.get(this);
				}
				
				ActorInfo item = new ActorInfo();
				item.parseElement(valueEl);
				
				arr.add((ActorInfo) item);
				getClass().getDeclaredField(fieldName).set(this, arr);
				
			} catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		
	}

	public String getGenericTypeName(String fieldName){

		Field field;
		try {
			field = getClass().getDeclaredField(fieldName);
			Type type = field.getGenericType();
			
			if (type instanceof ParameterizedType) {
				
				ParameterizedType pType = (ParameterizedType)type;
				Type[] arr = pType.getActualTypeArguments();
				
				for (Type tp: arr) {
					Class<?> clzz = (Class<?>)tp;
					System.out.println(clzz.getName());
					
					return clzz.getSimpleName();
				}
				
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Used by the ParseNative method. Performs any further processing on the 
	 * value if required.
	 * 
	 * @param el The element to extract the value from
	 */
 	private int processParsedInt(Element el) {

		String tag = el.getNodeName();
		int value = XMLUtils.getFirstChildValue_int(el);


		// Runtime and Duration
		if (tag == "runtime") {
			value = value * 60;
		}
		// Rating
		else if (tag == "rating") {

			try {
				int max_value = Integer.parseInt(XMLUtils.getAttribute(el, "max"));

				if (max_value >= 1)
					value = value / max_value * 10; // Normalise the Movie Rating to between 1 and 10
			} catch (NumberFormatException e) {
			}
		}

		return value;
	}

	/**
	 * Used by the ParseNative method. Performs any further processing on the 
	 * value if required.
	 * 
	 * @param el The element to extract the value from
	 */
	private void processParsedOther(Element el) {

		String tag = el.getNodeName();

		// Thumbs
		if (tag == "thumb") {

			if (pictureUrl == null)
				pictureUrl = new ScraperUrl();

			pictureUrl.ParseElement(el);
		}
		if (tag == "fanart") {
			
			if (fanartUrl == null)
				fanartUrl = new ScraperUrl();
			
			fanartUrl.ParseElement(el);
		}
		else if (tag == "fileinfo") {
			Element streamEl = XMLUtils.getFirstChildElement(el, "streamdetails");
			
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
		}
		else if (tag == "resume") {
			Element position = XMLUtils.getFirstChildElement(el, "position");

			if (position != null){
				resumePoint.timeInSeconds = Double.parseDouble(XMLUtils.getFirstChildValue(el));
			}
			
			Element total = XMLUtils.getFirstChildElement(el, "total");
			
			if (total != null){
				resumePoint.totalTimeInSeconds = Double.parseDouble(XMLUtils.getFirstChildValue(el));
			}
		}
		
		return;
	}
	
	
	
	
	
	/**
	 * Returns the variable type of a given member of the class.
	 * 
	 * @param fieldName The field to check
	 * @return Field Type
	 */
 	private String getFieldType(String fieldName) throws NoSuchFieldException {
		Field f = getClass().getDeclaredField(fieldName);

		return f.getType().getSimpleName();
	}
 	
 	
 	
 	
	/**
	 * Dynamically set a member of this class.
	 *
	 * @param fieldName The field to set
	 * @param value The value to set
	 */
	private void setField(String fieldName, String value)
			throws IllegalAccessException, IllegalArgumentException,
			NoSuchFieldException {
		getClass().getDeclaredField(fieldName).set(this, value);
	}
	
	/**
	 * Dynamically set a member of this class.
	 *
	 * @param fieldName The field to set
	 * @param value The value to set
	 */
	private void setField(String fieldName, int value)
			throws IllegalAccessException, IllegalArgumentException,
			NoSuchFieldException {
		getClass().getDeclaredField(fieldName).setInt(this, value);
	}
	
	/**
	 * Dynamically set a member of this class.
	 *
	 * @param fieldName The field to set
	 * @param value The value to set
	 */
	private void setField(String fieldName, float value)
			throws IllegalAccessException, IllegalArgumentException,
			NoSuchFieldException {
		getClass().getDeclaredField(fieldName).setFloat(this, value);
	}

	/**
	 * Dynamically set a member of this class.
	 *
	 * @param fieldName The field to set
	 * @param value The value to set
	 */
	private void setField(String fieldName, Date value)
			throws IllegalAccessException, IllegalArgumentException,
			NoSuchFieldException {
		getClass().getDeclaredField(fieldName).set(this, value);
	}
}
