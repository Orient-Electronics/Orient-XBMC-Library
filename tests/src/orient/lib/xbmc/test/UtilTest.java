package orient.lib.xbmc.test;

import java.util.Map;

import junit.framework.TestCase;
import orient.lib.xbmc.Util;

public class UtilTest extends TestCase {

	Map<String, String> result;
	
	public UtilTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		Util util = Util.getInstance();
		
		String str = "Mission Impossible Ghost Protocol [2011] 720p BRRip [Dual Audio] [English + Hindi] x264 BUZZccd [WBRG].mkv";
		result = util.CleanString(str, true, true);
	}

	public void testTitle() {
		assertEquals("Mission Impossible Ghost Protocol", result.get("title"));
	}
	
	public void testYear() {
		assertEquals("2011", result.get("year"));
	}
	
	public void testTitleAndYear() {
		assertEquals("Mission Impossible Ghost Protocol (2011)", result.get("titleAndYear"));
	}
	
}
