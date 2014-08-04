package orient.lib.xbmc.addons.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import orient.lib.xbmc.addons.Addon;

public class AddonTest {

	public AddonTest() {
	}

	@Test
	public void test() {
		Addon addon = new Addon("metadata.themoviedb.org");
		
		assertEquals(addon.getSetting("language"), "en");
	}
}
