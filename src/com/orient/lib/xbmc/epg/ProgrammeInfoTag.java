package com.orient.lib.xbmc.epg;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.orient.lib.xbmc.CONTENT_TYPE;
import com.orient.lib.xbmc.InfoTag;
import com.orient.lib.xbmc.utils.ScraperUrl;
import com.orient.lib.xbmc.utils.XMLUtils;
import com.orient.lib.xbmc.video.VideoInfoTag;

public class ProgrammeInfoTag extends InfoTag {

	/** event start time */
	public Date startTime; 

	/** event end time */
	public Date endTime; 


	public String channelId;
	
	/**
	 * @deprecated use info.type instead
	 */
	public CONTENT_TYPE type;
	
	public boolean hasDetails;
	
	// PVR::CPVRTimerInfoTagPtr m_timer;
	// CEpg * m_epg; /*!< the schedule that this event belongs to */
	//
	// PVR::CPVRChannelPtr m_pvrChannel;
	// CCriticalSection m_critSection;

	protected String xmlDateFormat = "yyyyMMddHHmmss Z";
	
	public VideoInfoTag info;

	@Override
	public void reset() {
		type = CONTENT_TYPE.CONTENT_NONE;
		
		info = new VideoInfoTag();
		info.type = CONTENT_TYPE.CONTENT_NONE;
	
		initXmlTagMapping();
	}

	public ProgrammeInfoTag() {
		super();
	}

	@Override
	protected void initXmlTagMapping() {
		xmlTagMapping.clear();
		
		// Other
		xmlTagMapping.put("type", "type");
		xmlTagMapping.put("icon", "info");
		
		
		////////////////////
		/// VideoInfoTag ///
		
		if (info == null)
			info = new VideoInfoTag();
		
		HashMap<String, String> videoInfoMapping = (HashMap<String, String>) info
				.getXmlTagMapping();
		
		videoInfoMapping.put("desc", "plot");
		videoInfoMapping.put("category", "genre");
//		videoInfoMapping.put("icon", "logoUrl");
		videoInfoMapping.put("release", "premiered");
		videoInfoMapping.put("category", "genre");
		
		videoInfoMapping.remove("credits");
		
		info.setXmlTagMapping(videoInfoMapping);
	}
	
	@Override
	public boolean loadXML(Element element, boolean append, boolean prioritise) {
		return super.loadXML(element, append, prioritise)
				&& info.loadXML(element, true, prioritise);
	}
	
	@Override
	protected void onParseXMLItemOther(Element el) {

		super.onParseXMLItemOther(el);
		
		String tag = el.getNodeName();

		// Type
		if (tag.equals("type")) {
			String value = XMLUtils.getFirstChildValue(el);
			value = StringUtils.replace(value, " ", "");
			
			if (value.equalsIgnoreCase("tvshow") || value.equalsIgnoreCase("tvshows")
					 || value.equalsIgnoreCase("tvseries")) {
				this.info.type = CONTENT_TYPE.CONTENT_TVSHOWS;
			}
			else if (value.equalsIgnoreCase("movie") || value.equalsIgnoreCase("movies")) {
				this.info.type = CONTENT_TYPE.CONTENT_MOVIES;
			}
			else if (value.equalsIgnoreCase("musicvideo") || value.equalsIgnoreCase("musicvideos")) {
				this.info.type = CONTENT_TYPE.CONTENT_MUSICVIDEOS;
			}
			else
				this.info.type = CONTENT_TYPE.CONTENT_NONE;
		}
		// Icon
		else if (tag.equals("icon")) {

			String url = XMLUtils.getAttribute(el, "src");

			if (url != null && !url.isEmpty()) {

				if (info.pictureUrl == null)
					info.pictureUrl = new ScraperUrl();

				info.pictureUrl.parseString("<url>" + url + "</url>");
			}
		}

		return;
	}
	
	
	@Override
	protected void afterParseXML (Element element) {
		super.afterParseXML(element);
		
		// Start, Stop times
		try {
			String dateFormatAttr = XMLUtils.getAttribute(element, "dateformat");
			String dateFormat = (dateFormatAttr != null) ? dateFormatAttr : xmlDateFormat;

			String startTime = XMLUtils.getAttribute(element, "start");

			if (startTime != null) 
				this.startTime = new SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(startTime);

			String stopTime = XMLUtils.getAttribute(element, "stop");
			
			if (stopTime != null) 
				this.endTime = new SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(stopTime);
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		// Channel Id
		channelId = XMLUtils.getAttribute(element, "channel");
		
		
		// Episode/Season/Part
		Element ep = XMLUtils.getFirstChildElement(element, "episode-num");
		
		if (ep != null) {
			String val = XMLUtils.getFirstChildValue(ep);

			val = StringUtils.replace(val, ".", " . ");
			String[] parts = StringUtils.split(val, '.');

			if (parts.length == 3) {

				// Series
				String series = parts[0].trim();

				try {
					if (!series.isEmpty())
						info.season = Integer.parseInt(series);
				}
				catch (NumberFormatException e) {
					e.printStackTrace();
				}

				// Episode
				String episode = parts[1].trim();

				try {
					if (!episode.isEmpty())
						info.episode = Integer.parseInt(episode);
				}
				catch (NumberFormatException e) {
					e.printStackTrace();
				}

				// Episode Part
				String[] episodeParts = parts[2].trim().split("/");

				if (episodeParts.length == 2){

					try {
						info.episodePart = Integer.parseInt(episodeParts[0].trim());
						info.episodeTotalParts = Integer.parseInt(episodeParts[1].trim());
					}
					catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		
		
		// Rating
		Element ratingEl = XMLUtils.getFirstChildElement(element, "star-rating");

		if (ratingEl != null) {
			try {
				Element valueEl = XMLUtils.getFirstChildElement(ratingEl, "value");
				String val = XMLUtils.getFirstChildValue(valueEl);

				val = StringUtils.replace(val, "/", " / ");
				String[] parts = StringUtils.split(val, '/');

				if (parts.length != 2)
					return;

				String rating = parts[0].trim();

				info.rating = Float.parseFloat(rating);
			}
			catch (NullPointerException | NumberFormatException e) {
				e.printStackTrace();
			}
		}
		
		
		// Rating
		Element creditsEl = XMLUtils.getFirstChildElement(element, "credits");
		
		if (creditsEl != null) {
			info.loadXML(creditsEl, true, false);
		}
	}
	
	
	
	
	
	
	
	/**
	 * Check if this event is currently active.
	 * If startTime or endTime is not set, returns false;
	 * 
	 * @return True if it's active, false otherwise.
	 */
	public boolean isActive() {
	  Date now = new Date();
	  
	  if (startTime == null || endTime == null)
		  return false;
	  
	  return (startTime.before(now) && endTime.after(now));
	}
	
	/**
     * @return True when this event has already passed, false otherwise.
     */
	public boolean wasActive() {
		Date now = new Date();
		
		if (endTime == null)
			return false;
		
		return (endTime.before(now));
	}
	
	/**
	 * @return True when this event is in the future, false otherwise.
	 */
	public boolean inTheFuture() {
		Date now = new Date();
		
		if (startTime == null)
			return false;
		
		return (startTime.after(now));
	}

	/**
	 * @return The current progress of this tag.
	 */
	public float progressPercentage() {
		float fReturn = (float) 0.0;

		long duration = endTime.getTime() - startTime.getTime() > 0 ? endTime
				.getTime() - startTime.getTime() : 3600;

		Date now = new Date();
		long currentTime = now.getTime();

		if (currentTime >= startTime.getTime()
				&& currentTime <= endTime.getTime())
			fReturn = ((float) currentTime - startTime.getTime()) / duration
					* 100;
		else if (currentTime > endTime.getTime())
			fReturn = 100;

		return fReturn;
	}

	/**
	 * @return The current progress of this tag in seconds.
	 */
	public long progress() {
		if (startTime == null)
			return -1;

		Date now = new Date();
		long duration = now.getTime() - startTime.getTime();

		if (duration <= 0)
			return 0;

		return duration / 1000 % 60;
	}
	
	/**
     * Get the duration of this event in seconds.
     * @return The duration in seconds.
     */
	public long getDuration() {
		
		if (startTime == null || endTime == null)
			return 0;
		
		long duration  = endTime.getTime() - startTime.getTime();
		
		return duration / 1000;
	}

}
