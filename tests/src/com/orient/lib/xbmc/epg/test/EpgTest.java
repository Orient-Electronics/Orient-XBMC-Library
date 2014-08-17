package com.orient.lib.xbmc.epg.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.orient.lib.xbmc.addons.Scraper;
import com.orient.lib.xbmc.addons.ScraperError;
import com.orient.lib.xbmc.epg.Epg;
import com.orient.lib.xbmc.utils.ScraperUrl;

@RunWith(JUnitParamsRunner.class)
public class EpgTest {

	@Test
	public void downloadEpg() throws ScraperError {
		Epg epg = new Epg();
		
		epg.downloadEpg("star-movies");
		
		epg.getChannelList().size();
	}
	
	@Test
	@Parameters({
		"metadata.epg.indian-television-guide, Star Movies, star-movies",
		"metadata.epg.tv.burrp.com, Star Movies, 59",
	})
	public void findEpgChannel(String scraperId, String channel, String expectedChannelId) throws ScraperError {
		Scraper scraper = new Scraper(scraperId);
		
		Epg epg = new Epg();
		ArrayList<ScraperUrl> results = epg.findChannel(scraper, channel);
		
		assertEquals(expectedChannelId, results.get(0).id);
	}
}
