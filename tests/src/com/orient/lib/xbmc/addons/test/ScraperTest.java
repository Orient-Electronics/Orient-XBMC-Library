package com.orient.lib.xbmc.addons.test;

import static junitparams.JUnitParamsRunner.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.orient.lib.xbmc.NfoFile;
import com.orient.lib.xbmc.Settings;
import com.orient.lib.xbmc.addons.Scraper;
import com.orient.lib.xbmc.addons.ScraperError;
import com.orient.lib.xbmc.utils.ScraperUrl;

@RunWith(JUnitParamsRunner.class)
public class ScraperTest {

	private Settings settings;
	
	private String testAssetsPath;
	
	public ScraperTest() {
		settings = Settings.getInstance();
		testAssetsPath = settings.getTestAssetsDirPath();
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	@Parameters({
		"metadata.themoviedb.org, true",
		"fake.scraper.doesnt.exist, false",
	})
	public void load(String id, boolean result) {
		Scraper scraper = new Scraper(id);
		
		boolean loadResult = false;
		
		if (scraper != null)
			loadResult = scraper.load();
		
		assertEquals(result, loadResult);
	}
	
	@Test
	@Parameters({
		"metadata.themoviedb.org, 82992, Fast & Furious 6 (2013)",
		"metadata.themoviedb.org, 187339, Mission Impossible 720p BRRip [Dual Audio] [English + Hindi] x264 BUZZccd [WBRG].mkv",
		"metadata.themoviedb.org, 56292, Mission Impossible Ghost Protocol [2011] 720p BRRip [Dual Audio] [English + Hindi] x264 BUZZccd [WBRG].mkv",
	})
	public void findMovie(String scraperId, String movieIdResult, String filename) {
		Scraper scraper = new Scraper(scraperId);
		ArrayList<ScraperUrl> movieResults;
		
		try {
			movieResults = scraper.findMovie(filename, true);
			assertEquals(movieResults.get(0).id , movieIdResult);
		} catch (ScraperError e) {
			e.printStackTrace();
			fail();
		}
		
	}
	
	@Test
	@Parameters({
		"metadata.epg.indian-television-guide, Star Movies, star-movies",
		"metadata.epg.tv.burrp.com, Star Movies, 59",
	})
	public void findEpgChannel(String scraperId, String channel, String expectedChannelId) throws ScraperError {
		Scraper scraper = new Scraper(scraperId);
		ArrayList<ScraperUrl> movieResults = scraper.findEpgChannel(channel, null);
		
		assertEquals(expectedChannelId, movieResults.get(0).id);
	}

	@SuppressWarnings("unused")
	private Object[] nfoData() {
		return $(
				$( "metadata.themoviedb.org", "44214", "http://api.tmdb.org/3/movie/44214?api_key=57983e31fb435df4df77afb854740ea9&amp;language=en", testAssetsPath + "Testing Data\\Movies - Flat\\Black Swan (2010) [720p] [R] [voted 0.0] [Thriller].nfo" )
				);
	}
	
	
	@Test
	@Parameters(method = "nfoData")
	public void nfoUrl(String scraperId, String idResult, String url, String filePath) {
		NfoFile nfoFile = new NfoFile();
		
		nfoFile.load(filePath);
		
		// Get field value to check
		Field field;
		try {
			field = nfoFile.getClass().getDeclaredField("document");
			field.setAccessible(true);
			String nfoContent = (String) field.get(nfoFile);
			
			Scraper scraper = new Scraper(scraperId);
			ScraperUrl scraperUrl = scraper.nfoUrl(nfoContent);
			
			boolean result = false;
			
			if (scraperUrl.id.equals(idResult))
				result = true;
			
			assertEquals(true, result);
			
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (ScraperError e) {
			e.printStackTrace();
		}
	}
}
