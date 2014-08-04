package orient.lib.xbmc.utils.test;

import junit.framework.TestCase;
import orient.lib.xbmc.utils.ScraperUrl;
import orient.lib.xbmc.utils.ScraperUrlGet;
import orient.lib.xbmc.utils.UrlEntry;


public class ScraperUrlGetTest extends TestCase {

	private ScraperUrlGet urlGetter;
	private ScraperUrl url;
	private String getResult;
	
	public ScraperUrlGetTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		url = new ScraperUrl();
		UrlEntry entry = new UrlEntry();
		
		entry.m_url = "http://api.tmdb.org/3/search/movie?api_key=57983e31fb435df4df77afb854740ea9&query=Mission%20Impossible%20Ghost%20Protocol&year=2011&language=en";
		entry.m_cache = "cachfile";
		url.m_url.add(entry);

		urlGetter = new ScraperUrlGet(entry, "cacheContext");
		getResult = urlGetter.Get();
	}

	public void testFoo() {
		boolean result = (getResult.indexOf("{\"page\":1,\"results\":[{\"adult\":false") == 0) ? true : false;
		
		assertTrue(result);
	}
}
