package orient.lib.xbmc.addons;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import orient.lib.xbmc.Settings;

public class AddonManager {

	private static AddonManager instance = null;

	protected AddonManager() {
		// Exists only to defeat instantiation.
	}

	public static AddonManager getInstance() {
		if (instance == null) {
			instance = new AddonManager();
		}
		return instance;
	}
	
	public static Addon getAddon(String id) {
		
		if (!Addon.exists(id))
			return null;
		
		Addon addon = new Addon(id);
		
		if (addon.getId() == null)
			return null;
		
		return addon;
	}
	
	/**
	 * Returns ArrayList all Scrapers of a given type
	 * 
	 * @param type
	 * @return
	 */
	public static ArrayList<Scraper> GetScrapers(ADDON_TYPE type) {
		
		// Fetching Addon Directory
		Settings settings = Settings.getInstance();
		File addonDir = settings.getAddonDir();
		
		// Getting a list of all folders from the addon directory
		String[] directories = addonDir.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
		
		if (directories == null)
			return null;
		
		// Building the list of addons
		ArrayList<Scraper> result = new ArrayList<Scraper>();
		
		for (String addonId : directories) {
			if (!Addon.exists(addonId))
				continue;
			
			Scraper addon = new Scraper(addonId);
			
			if (addon.getType() != type)
				continue;
			
			result.add(addon);
		}
		
		return result;
	}

	public static Scraper getDefaultScraper(ADDON_TYPE type) {
		Settings settings = Settings.getInstance();
		String setting = null;

		switch (type) {
		
		case ADDON_SCRAPER_ALBUMS:
			setting = settings.get("musiclibrary.albumsscraper");
			break;
		case ADDON_SCRAPER_ARTISTS:
			setting = settings.get("musiclibrary.artistsscraper");
			break;
		case ADDON_SCRAPER_MOVIES:
			setting = settings.get("scrapers.moviesdefault");
			break;
		case ADDON_SCRAPER_MUSICVIDEOS:
			setting = settings.get("scrapers.musicvideosdefault");
			break;
		case ADDON_SCRAPER_TVSHOWS:
			setting = settings.get("scrapers.tvshowsdefault");
			break;
		default:
			setting = null;
		}

		if (Scraper.exists(setting))
			return new Scraper(setting);
		
		return null;
	}
}
