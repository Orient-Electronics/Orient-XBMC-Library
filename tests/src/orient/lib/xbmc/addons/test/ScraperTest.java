package orient.lib.xbmc.addons.test;

import static junitparams.JUnitParamsRunner.$;
import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

import orient.lib.xbmc.NfoFile;
import orient.lib.xbmc.Settings;
import orient.lib.xbmc.addons.Scraper;
import orient.lib.xbmc.addons.ScraperError;
import orient.lib.xbmc.utils.ScraperUrl;
import orient.lib.xbmc.utils.XMLUtils;

@RunWith(JUnitParamsRunner.class)
public class ScraperTest {

	private Settings settings;
	
	private String assetsPath;
	
	public ScraperTest() {
	}

	@Before
	public void setUp() throws Exception {
		settings = Settings.getInstance();
		assetsPath = settings.getAssetsDirPath();
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
		"metadata.themoviedb.org, 187339, Mission Impossible 720p BRRip [Dual Audio] [English + Hindi] x264 BUZZccd [WBRG].mkv",
		"metadata.themoviedb.org, 56292, Mission Impossible Ghost Protocol [2011] 720p BRRip [Dual Audio] [English + Hindi] x264 BUZZccd [WBRG].mkv",
	})
	public void findMvoie(String scraperId, String movieIdResult, String filename) {
		Scraper scraper = new Scraper(scraperId);
		ArrayList<ScraperUrl> movieResults;
		
		try {
			movieResults = scraper.FindMovie(filename, true);
			assertEquals(movieResults.get(0).strId , movieIdResult);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			fail();
		} catch (ScraperError e) {
			e.printStackTrace();
			fail();
		}
		
	}

	@SuppressWarnings("unused")
	private Object[] nfoData() {
		return $(
				$( "metadata.themoviedb.org", "44214", "http://api.tmdb.org/3/movie/44214?api_key=57983e31fb435df4df77afb854740ea9&amp;language=en", assetsPath + "Testing Data\\Movies - Flat\\Black Swan (2010) [720p] [R] [voted 0.0] [Thriller].nfo" )
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
			
			if (scraperUrl.strId.equals(idResult) && scraperUrl.m_url.get(0).m_url.equals(url))
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
