package orient.lib.xbmc.utils.test;

import static org.junit.Assert.*;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import orient.lib.xbmc.Settings;
import orient.lib.xbmc.utils.ScraperParser;

@RunWith(JUnitParamsRunner.class)
public class ScraperParserTest {

	private ScraperParser parser;
	private boolean loadResult;

	private String assetsPath;

	public ScraperParserTest() {
		Settings settings = Settings.getInstance();
		assetsPath = settings.getAssetsDirPath();
	}

	@Before
	public void setUp() throws Exception {
		parser = new ScraperParser();
		
		String filePath = assetsPath + "xbmc-addons\\addons\\metadata.themoviedb.org\\tmdb.xml";
	
		loadResult = parser.Load(filePath);
	}
	
	@Test
	public void load(){
		assertEquals(true, loadResult);
	}

	@Test
	@Parameters({
		"this !!!CLEAN!!!is!!!CLEAN!!! a string !!!CLEAN!!!<i>with</i>!!!CLEAN!!! a tag, this is a string with a tag", 
		"!!!TRIM!!!   foo bar  !!!TRIM!!!, foo bar",
		"!!!FIXCHARS!!!   ?   !!!FIXCHARS!!!, ?",
		"!!!ENCODE!!!http://example.com/räksmörgås!!!ENCODE!!!, http%3A%2F%2Fexample.com%2Fr%E4ksm%F6rg%E5s",
		})
	public void clean(String test, String result){
		assertEquals(result, parser.Clean(test));
	}
	
	@Test
	public void parse(){
		parser.m_param[0] = "Mission%20Impossible%20Ghost%20Protocol";
		parser.m_param[1] = "2011";
		
		String actualResult = parser.Parse("CreateSearchUrl", null);
		
		// Language will be empty because settings not loaded. 
		// Settings will load if this parser is created by an addon
		String expectedResult = "<url>http://api.tmdb.org/3/search/movie?api_key=57983e31fb435df4df77afb854740ea9&amp;query=Mission%20Impossible%20Ghost%20Protocol&amp;year=2011&amp;language=</url>";
		
		assertEquals(expectedResult, actualResult);
	}
	
	
	@Test
	public void insertToken(){
		String str = "foo\\5bar";
		String result = parser.InsertToken(str, 5, "$$$");
		
		assertEquals("foo$$$\\5$$$bar", result);
	}

}