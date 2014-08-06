package orient.lib.xbmc.test;

import static org.junit.Assert.assertEquals;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import orient.lib.xbmc.URL;

@RunWith(JUnitParamsRunner.class)
public class URLTest {

	
	@Test
	@Parameters({
		"special://masterprofile/",
		"C:\\Users\\Abdul Rehman\\AppData\\Roaming\\XBMC\\",
		"special://home/addons",
		"C:\\Users\\Abdul Rehman\\AppData\\Roaming\\XBMC\\addons",
		})
	public void Get(String path) {
		URL url = new URL(path);
		
		assertEquals(path, url.Get());
	}
	
	@Test
	@Parameters({
		"http://example.com/räksmörgås, http%3a%2f%2fexample.com%2fr%e4ksm%f6rg%e5s"
	})
	public void Encode(String test, String result) {
		assertEquals(result, URL.Encode(test));
	}
	
	@Test
	@Parameters({
		"http%3a%2f%2fexample.com%2fr%e4ksm%f6rg%e5s, http://example.com/räksmörgås"
	})
	public void Decode(String test, String result) {
		assertEquals(result, URL.Decode(test));
	}
}
