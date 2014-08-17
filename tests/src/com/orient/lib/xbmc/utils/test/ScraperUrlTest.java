package com.orient.lib.xbmc.utils.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.orient.lib.xbmc.utils.ScraperUrl;
import com.orient.lib.xbmc.utils.URL_TYPE;
import com.orient.lib.xbmc.utils.UrlEntry;

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

		parseResult = url.parseString(xmlstring);
	}

	@Test
	public void parseString() {
		assertTrue(parseResult);
	}

	@Test
	public void parseStringResults() {
		UrlEntry url = new UrlEntry();
		url.spoof = "blah";
		url.url = "someurl";
		url.cache = null;
		url.aspect = null;
		url.type = URL_TYPE.GENERAL;
		url.isPost = false;
		url.isGZip = true;
		url.season = -1;
		
		boolean equal = this.url.getFirstThumb(null).equals(url);
		
		assertEquals(true, equal);
	}

}
