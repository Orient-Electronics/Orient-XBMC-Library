package com.orient.lib.xbmc.test;

import static org.junit.Assert.assertEquals;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;

import com.orient.lib.xbmc.NfoFile;
import com.orient.lib.xbmc.Settings;
import com.orient.lib.xbmc.addons.Scraper;

public class NfoFileTest {

	private Settings settings;
	private String testAssetsPath;

	@Before
	public void setUp() throws Exception {
		settings = Settings.getInstance();
		testAssetsPath = FilenameUtils.separatorsToSystem(settings.getAppDir()
				+ "tests\\assets\\");
	}

	@Test
	public void create() {

		Scraper scraper = new Scraper("metadata.themoviedb.org");

		String nfoPath = FilenameUtils.separatorsToSystem(testAssetsPath
				+ "Testing Data\\Movies - Flat\\movie.nfo");

		NfoFile nfo = new NfoFile();
		nfo.create(nfoPath, scraper);

		assertEquals(nfo.getScraperUrl().id, "tt1440129");
	}

}
