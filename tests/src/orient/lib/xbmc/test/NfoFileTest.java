package orient.lib.xbmc.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import orient.lib.xbmc.NfoFile;
import orient.lib.xbmc.Settings;
import orient.lib.xbmc.addons.Scraper;

public class NfoFileTest {


	private Settings settings;
	private String assetsPath;
	
	@Before
	public void setUp() throws Exception {
		settings = Settings.getInstance();
		assetsPath = settings.getTestAssetsDirPath();
	}


	@Test
	public void create() {

		Scraper scraper = new Scraper("metadata.themoviedb.org");
		
		
		String nfoPath = assetsPath + "Testing Data\\Movies - Flat\\movie.nfo";
		
		NfoFile nfo = new NfoFile();
		nfo.create(nfoPath, scraper);
		
		assertEquals(nfo.getScraperUrl().strId, "tt1440129");
	}

}
