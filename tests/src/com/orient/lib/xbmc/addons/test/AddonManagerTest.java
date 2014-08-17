package com.orient.lib.xbmc.addons.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.orient.lib.xbmc.addons.ADDON_TYPE;
import com.orient.lib.xbmc.addons.AddonManager;
import com.orient.lib.xbmc.addons.Scraper;

public class AddonManagerTest {

	public AddonManagerTest() {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		ArrayList<Scraper> result = AddonManager.GetScrapers(ADDON_TYPE.ADDON_SCRAPER_MOVIES);
		
		assertEquals(2, result.size());
	}

}
