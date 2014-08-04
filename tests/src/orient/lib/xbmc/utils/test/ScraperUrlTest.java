package orient.lib.xbmc.utils.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import orient.lib.xbmc.utils.ScraperUrl;
import orient.lib.xbmc.utils.URL_TYPE;
import orient.lib.xbmc.utils.UrlEntry;

public class ScraperUrlTest {

	private ScraperUrl url;
	private String xmlstring;
	private boolean parseResult;
	
	public ScraperUrlTest() {
	}

	@Before
	public void setUp() throws Exception {
		url = new ScraperUrl();

		xmlstring = "<data spoof=\"blah\" gzip=\"yes\">\n" + "  <someurl>\n"
				+ "  </someurl>\n" + "  <someotherurl>\n"
				+ "  </someotherurl>\n" + "</data>\n";

		parseResult = url.ParseString(xmlstring);
	}

	@Test
	public void parseString() {
		assertTrue(parseResult);
	}

	@Test
	public void parseStringResults() {
		UrlEntry url = new UrlEntry();
		url.m_spoof = "blah";
		url.m_url = "someurl";
		url.m_cache = null;
		url.m_aspect = null;
		url.m_type = URL_TYPE.GENERAL;
		url.m_post = false;
		url.m_isgz = true;
		url.m_season = -1;
		
		boolean equal = this.url.GetFirstThumb(null).equals(url);
		
		assertEquals(true, equal);
	}

}
