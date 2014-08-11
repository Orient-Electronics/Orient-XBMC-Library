package orient.lib.xbmc.epg;

import java.util.ArrayList;
import java.util.Date;

import orient.lib.xbmc.InfoTag;

public class EpgInfoTag extends InfoTag {

	/** notify on start */
	protected boolean notify;

	/** keep track of changes to this entry */
	protected boolean changed;

	/** database ID */
	protected int broadcastId;

	/** genre type */
	protected int genreType;

	/** genre subtype */
	protected int genreSubType;

	/** parental rating */
	protected int parentalRating;

	/** star rating */
	protected int starRating;

	/** series number */
	protected int seriesNumber;

	/** episode number */
	protected int episodeNumber;

	/** episode part number */
	protected int episodePart;

	/** unique broadcast ID */
	protected int uniqueBroadcastID;

	/** title */
	protected String title;

	/** plot outline */
	protected String plotOutline; 

	/** plot */
	protected String plot; 

	/** genre */
	protected ArrayList<String> genre; 

	/** episode name */
	protected String episodeName; 

	/** the path to the icon */
	protected String iconPath; 

	/** the filename and path */
	protected String fileNameAndPath; 

	/** event start time */
	protected Date startTime; 

	/** event end time */
	protected Date endTime; 

	/** first airdate */
	protected Date firstAired;

	
	// PVR::CPVRTimerInfoTagPtr m_timer;
	// CEpg * m_epg; /*!< the schedule that this event belongs to */
	//
	// PVR::CPVRChannelPtr m_pvrChannel;
	// CCriticalSection m_critSection;


	@Override
	public void reset() {
		notify = false;
		changed = false;
		broadcastId = -1;
		genreType = 0;
		genreSubType = 0;
		parentalRating = 0;
		starRating = 0;
		seriesNumber = 0;
		episodeNumber = 0;
		episodePart = 0;
		uniqueBroadcastID = -1;
	}

	@Override
	protected void initXmlTagMapping() {
		// Strings
		xmlTagMapping.put("episodename", "episodeName");
		xmlTagMapping.put("filenameandpath", "fileNameAndPath");
		xmlTagMapping.put("icon", "iconPath");
		xmlTagMapping.put("plot", "plot");
		xmlTagMapping.put("outline", "plotOutline");
		xmlTagMapping.put("title", "title");

		// Integers
		xmlTagMapping.put("broadcastid", "broadcastID");
		xmlTagMapping.put("episodenumber", "episodeNumber");
		xmlTagMapping.put("episodepart", "episodePart");
		xmlTagMapping.put("uniquebroadcastid", "uniqueBroadcastID");
	}
}
