package orient.lib.xbmc.test;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import junit.framework.TestCase;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import orient.lib.xbmc.Util;

@RunWith(JUnitParamsRunner.class)
public class UtilTest extends TestCase {

	@Test
	@Parameters({
		"Mission Impossible Ghost Protocol [2011] 720p BRRip [Dual Audio] [English + Hindi] x264 BUZZccd [WBRG].mkv, "
		+ "Mission Impossible Ghost Protocol, "
		+ "2011, "
		+ "Mission Impossible Ghost Protocol (2011)",
	})
	public void CleanString(String path, String title, String year, String titleAndYear) {
		Map<String, String> result = Util.CleanString(path, true, true);
		
		boolean check = false;
		
		if (result.get("title").equals(title) &&
				result.get("year").equals(year) &&
				result.get("titleAndYear").equals(titleAndYear))
			check = true;
		
		assertEquals(true, check);
	}
	
	@Test
	@Parameters({
		"special://masterprofile/, special://masterprofile/",
		"C:\\Users\\Abdul Rehman\\AppData\\Roaming\\XBMC\\ , C:\\Users\\Abdul Rehman\\AppData\\Roaming\\XBMC\\"
	})
	public void validatePath(String path, String result) {
		assertEquals(result, Util.validatePath(path));
	}
	
}
