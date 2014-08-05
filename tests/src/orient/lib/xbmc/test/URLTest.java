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
}
