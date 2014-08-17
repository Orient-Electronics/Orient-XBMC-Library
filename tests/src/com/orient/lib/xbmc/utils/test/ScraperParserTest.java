package com.orient.lib.xbmc.utils.test;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.orient.lib.xbmc.Settings;
import com.orient.lib.xbmc.utils.ScraperParser;

@RunWith(JUnitParamsRunner.class)
public class ScraperParserTest {

	private ScraperParser parser;
	private boolean loadResult;

	private String assetsPath;

	public ScraperParserTest() {
		Settings settings = Settings.getInstance();
		assetsPath = settings.getAssetsDirPath();
		parser = new ScraperParser();
	}

	@Before
	public void setUp() throws Exception {		
		String filePath = assetsPath + "xbmc-addons\\addons\\metadata.themoviedb.org\\tmdb.xml";
		loadResult = parser.load(filePath);
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
		"!!!ENCODE!!!http://example.com/räksmörgås!!!ENCODE!!!, http%3a%2f%2fexample.com%2fr%e4ksm%f6rg%e5s",
		})
	public void clean(String test, String expectedResult) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		Method method = parser.getClass().getDeclaredMethod("clean", String.class);
		method.setAccessible(true);
		String result = (String) method.invoke(parser, test);
		
		
		assertEquals(expectedResult, result);
	}
	
	@Test
	public void parse(){
		parser.params[0] = "Mission%20Impossible%20Ghost%20Protocol";
		parser.params[1] = "2011";
		
		String actualResult = parser.parse("CreateSearchUrl", null);
		
		// Language will be empty because settings not loaded. 
		// Settings will load if this parser is created by an addon
		String expectedResult = "<url>http://api.tmdb.org/3/search/movie?api_key=57983e31fb435df4df77afb854740ea9&amp;query=Mission%20Impossible%20Ghost%20Protocol&amp;year=2011&amp;language=</url>";
		
		assertEquals(expectedResult, actualResult);
	}
	
	
	@Test
	public void insertToken() throws NoSuchMethodException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		String str = "foo\\5bar";

		Method method = parser.getClass().getDeclaredMethod("insertToken",
				String.class, int.class, String.class);
		method.setAccessible(true);
		String result = (String) method.invoke(parser, str, 5, "$$$");

		assertEquals("foo$$$\\5$$$bar", result);
	}

}