/**
 * 
 */
package com.orient.lib.xbmc.video;

import java.util.Date;

import com.orient.lib.xbmc.utils.ScraperUrl;

/**
 * @author Abdul Rehman
 * 
 */
public class Episode {
	public boolean isFolder = false;
	public int season = -1;
	public int episode = -1;
	public int subEpisode = 0;
	public String path;
	public String title;
	public Date date;
	public ScraperUrl scraperUrl;
}
