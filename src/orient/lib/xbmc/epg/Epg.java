package orient.lib.xbmc.epg;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import orient.lib.xbmc.addons.ADDON_TYPE;
import orient.lib.xbmc.addons.AddonManager;
import orient.lib.xbmc.addons.Scraper;
import orient.lib.xbmc.addons.ScraperError;
import orient.lib.xbmc.utils.ScraperUrl;
import orient.lib.xbmc.utils.XMLUtils;

public class Epg {
	
	private ArrayList<ChannelInfoTag> channelList;
	private ArrayList<ProgrammeInfoTag> programmeList;
	
	public Epg() {
		super();
	}
	
	/**
	 * Sets the endtime of each programme with the start time of next programme,
	 * if the endtime already not set.
	 * 
	 * Before calculating end timings, the programme list is sorted based on
	 * start times.
	 */
	public void autoFillEndTimes() {
		sortProgrammes();

		ArrayList<ProgrammeInfoTag> tempProgrammeList = new ArrayList<ProgrammeInfoTag>();

		for(int i=0; i < programmeList.size() - 1; i++) {

			ProgrammeInfoTag p = programmeList.get(i);

			if (p.endTime == null) {

				for(int j=i+1; j < programmeList.size(); j++) 

					if (p.channelId.equals(programmeList.get(j).channelId)) {
						p.endTime = programmeList.get(j).startTime;
						break;
					}
			}
			
			tempProgrammeList.add(p);
		}
		
		programmeList = tempProgrammeList;
	}	
	
	
	public ArrayList<ScraperUrl> findChannel(String channel) throws ScraperError {
		return findChannel(channel);
	}

	public ArrayList<ScraperUrl> findChannel(Scraper scraper, String channel) throws ScraperError {
		
		if (scraper == null)
			scraper = AddonManager.getDefaultScraper(ADDON_TYPE.ADDON_SCRAPER_EPG);
		
		return scraper.findEpgChannel(channel, null);
	}
	
	public void clear() {
		if (channelList != null)
			channelList.clear();

		if (programmeList != null)
			programmeList.clear();
	}	
	
	@SuppressLint("SimpleDateFormat") 
	public void downloadEpg(Scraper scraper, String channelId, Date date) throws ScraperError {
//		Scraper scraper = new Scraper("metadata.epg.indian-television-guide");
		
		if (scraper == null)
			scraper = AddonManager.getDefaultScraper(ADDON_TYPE.ADDON_SCRAPER_EPG);
		
		ArrayList<String> params = new ArrayList<String>();
		
		// Channel URL
		params.add(null);
		
		// Channel id
		params.add(channelId);
		
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss Z");

		// Date to get timeline
		if (date == null)
			date = new Date();
		
		params.add(dateFormat.format(date));
		
		// Today's Date
		Date today = new Date();
		params.add(dateFormat.format(today));
		
		ArrayList<String> resultList = scraper.run("GetDetails", null, params);
		
		if (resultList.isEmpty())
			return;
		
		for (String result : resultList) {
			Document doc = XMLUtils.getDocumentFromString(result);

			if (doc == null || !doc.getDocumentElement().getNodeName().equals("tv") 
					||doc.getDocumentElement() == null) {
				continue;  // might have more valid results later
			}
			
			// Channels
			Element channelItem = XMLUtils.getFirstChildElement(doc.getDocumentElement(), "channel");
			
			while (channelItem != null) {

				if (channelList == null)
					channelList = new ArrayList<ChannelInfoTag>();

				ChannelInfoTag cTag = new ChannelInfoTag();
				cTag.loadXML(channelItem, false, false);
				
				channelList.add(cTag);
				
				
				channelItem = XMLUtils.getNextSiblingElement(channelItem, "channel");
			}
			
			// Programmes
			Element programmeItem = XMLUtils.getFirstChildElement(doc.getDocumentElement(), "programme");
			
			while (programmeItem != null) {
				
				if (programmeList == null)
					programmeList = new ArrayList<ProgrammeInfoTag>();

				ProgrammeInfoTag pTag = new ProgrammeInfoTag();
				pTag.loadXML(programmeItem, false, false);
				
				programmeList.add(pTag);
				
				programmeItem = XMLUtils.getNextSiblingElement(programmeItem, "programme");
			}
		}
		
		if (scraper.getSetting("autoendtime").equals("true")) {
			autoFillEndTimes();
		}
	}
	
	public void downloadEpg(String channelId) throws ScraperError {
		downloadEpg(null, channelId, null);
	}
	
	public void downloadEpg(String channelId, Date date) throws ScraperError {
		downloadEpg(null, channelId, date);
	}
	
	/**
	 * Returns the currently active programme on a given channel.
	 * @return
	 */
	public ProgrammeInfoTag getActiveProgramme(String channelId) {
		for (ProgrammeInfoTag p : programmeList) {
			if (p.channelId.equals(channelId) && p.isActive())
				return p;
		}
		
		return null;
	}
	
	
	/**
	 * Returns the list of all currently active programmes.
	 * @return
	 */
	public ArrayList<ProgrammeInfoTag> getActiveProgrammeList(String channelId) {
		
		ArrayList<ProgrammeInfoTag> list = new ArrayList<ProgrammeInfoTag>();
		
		for (ProgrammeInfoTag p : programmeList) {
			if (p.isActive())
				list.add(p);
		}
		
		return list;
	}
	
	public ArrayList<ChannelInfoTag> getChannelList() {
		return channelList;
	}
	
	public ArrayList<ProgrammeInfoTag> getProgrammeList() {
		return programmeList;
	}
	
	public void setChannelList(ArrayList<ChannelInfoTag> channelList) {
		this.channelList = channelList;
	}
	
	public void setProgrammeList(ArrayList<ProgrammeInfoTag> programmeList) {
		this.programmeList = programmeList;
	}
	
	/**
	 * Sorts all programmes based on the start times.
	 */
	public void sortProgrammes() {
		Collections.sort(programmeList, new Comparator<ProgrammeInfoTag>() {
	        @Override 
	        public int compare(ProgrammeInfoTag p1, ProgrammeInfoTag p2) {
	        	if (p1.startTime.after(p2.startTime)) return 1;
	            if (p1.startTime.before(p2.startTime)) return -1;
		        return 0;
	        }
	    });
	}
}
