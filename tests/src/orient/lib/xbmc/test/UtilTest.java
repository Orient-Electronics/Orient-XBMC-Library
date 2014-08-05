package orient.lib.xbmc.test;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import junit.framework.TestCase;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import orient.lib.xbmc.Util;

@RunWith(JUnitParamsRunner.class)
public class UtilTest extends TestCase {

	Map<String, String> result;
	
	public UtilTest(String name) {
		super(name);
	}
	
	@Before
	protected void setUp() throws Exception {
		super.setUp();
		
		Util util = Util.getInstance();
		
		String str = "Mission Impossible Ghost Protocol [2011] 720p BRRip [Dual Audio] [English + Hindi] x264 BUZZccd [WBRG].mkv";
		result = util.CleanString(str, true, true);
	}

	@Test
	public void testTitle() {
		assertEquals("Mission Impossible Ghost Protocol", result.get("title"));
	}
	
	@Test
	public void testYear() {
		assertEquals("2011", result.get("year"));
	}
	
	@Test
	public void testTitleAndYear() {
		assertEquals("Mission Impossible Ghost Protocol (2011)", result.get("titleAndYear"));
	}

	@Test
	@Parameters({
		"special://masterprofile/, special://masterprofile/",
		"C:\\Users\\Abdul Rehman\\AppData\\Roaming\\XBMC\\, C:\\Users\\Abdul Rehman\\AppData\\Roaming\\XBMC\\"
	})
	public void validatePath(String path, String result) {
		assertEquals(result, Util.validatePath(path));
	}
	
}
