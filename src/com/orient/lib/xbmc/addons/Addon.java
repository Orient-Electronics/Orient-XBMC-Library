package com.orient.lib.xbmc.addons;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.orient.lib.xbmc.Settings;
import com.orient.lib.xbmc.utils.FileUtils;
import com.orient.lib.xbmc.utils.XMLUtils;

public class Addon {

	public class TypeMapping {
		public String name;
		public ADDON_TYPE type;
		public int pretty;
		public String icon;

		public TypeMapping(String name, ADDON_TYPE type, int pretty, String icon) {
			super();
			this.name = name;
			this.type = type;
			this.pretty = pretty;
			this.icon = icon;
		}

	}

	/**
	 * Checks if an addon exists.
	 * 
	 * @param id
	 * @return
	 */
	public static boolean exists(String id) {
		String path = Addon.getAddonXmlPath(id);
		
		if (path == null || !FileUtils.exists(path))
			return false;

		return true;
	}

	/**
	 * Returns the path of the addon.xml file of the given addon
	 * 
	 * @param id
	 * @return path | null (if addon directory doesnt exist)
	 */
	private static String getAddonXmlPath(String id) {
		return FilenameUtils.separatorsToSystem(Settings.getInstance()
				.getAddonDirPath() + "\\" + id + "\\addon.xml");
	}

	@SuppressWarnings("unused")
	private boolean hasSettings;
	private boolean requiressettings;
	private boolean settingsLoaded;

	private boolean userSettingsLoaded;
	private boolean hasStrings;
	private boolean checkedStrings;
	protected AddonProps props = new AddonProps();
	private Document document;
	private Document addonXmlDoc;

	private Map<Integer, String> strings = new HashMap<Integer, String>();

	private Map<String, String> settings = new HashMap<String, String>();

	public static ArrayList<TypeMapping> types = new ArrayList<TypeMapping>();

	public Addon() {
		initTypes();
	}

	public Addon(AddonProps props) {
		initTypes();

		this.props = props;
	}

	public Addon(String id) {
		initTypes();
		loadPropsByAddonId(id);
		loadSettings(false);
		loadStrings();
	}

	public void clearStrings() {
		// Unload temporary language strings
		strings.clear();
		hasStrings = false;
	}

	public String getAuthor() {
		return props.author;
	}

	public String getChangeLog() {
		return props.changelog;
	}

	public ArrayList<String> getDeps() {
		return props.dependencies;
	}

	public String getDescription() {
		return props.description;
	}

	public String getDisclaimer() {
		return props.disclaimer;
	}

	public Map<String, String> getExtraInfo() {
		return props.extrainfo;
	}

	public String getFanArt() {
		return props.fanart;
	}

	public String getId() {
		return props.id;
	}

	public String getLibPath() {
		return props.path + "\\" + props.libname;
	}

	public String getMinVersion() {
		return props.minversion;
	}

	public String getName() {
		return props.name;
	}

	public String getPath() {
		return props.path;
	}

	public AddonProps getProps() {
		return props;
	}

	public String getSetting(String key) {
		if (!loadSettings(false))
			return null; // no settings available

		return settings.get(key);
	}

	public int getStars() {
		return props.stars;
	}

	public String getString(int id) {
		if (!hasStrings && !checkedStrings && !loadStrings())
			return "";

		return strings.get(id);
	}

	public String getSummary() {
		return props.summary;
	}

	public ADDON_TYPE getTranslateType(String str) {
		if (str == null)
			return null;

		for (int index = 0; index < types.size(); ++index) {
			TypeMapping map = types.get(index);
			if (str.equals(map.name))

				return map.type;
		}

		return ADDON_TYPE.ADDON_UNKNOWN;
	}

	// properties
	public ADDON_TYPE getType() {
		return props.type;
	}

	public String getVersion() {
		return props.version;
	}

	public boolean hasSettings() {
		return loadSettings(false);
	}

	public boolean hasUserSettings() {
		if (!loadSettings(false))
			return false;

		return userSettingsLoaded;
	}

	public void initTypes() {
		types.add(new TypeMapping("unknown", ADDON_TYPE.ADDON_UNKNOWN, 0, ""));
		types.add(new TypeMapping("xbmc.metadata.scraper.albums",
				ADDON_TYPE.ADDON_SCRAPER_ALBUMS, 24016,
				"DefaultAddonAlbumInfo.png"));
		types.add(new TypeMapping("xbmc.metadata.scraper.artists",
				ADDON_TYPE.ADDON_SCRAPER_ARTISTS, 24017,
				"DefaultAddonArtistInfo.png"));
		types.add(new TypeMapping("xbmc.metadata.scraper.movies",
				ADDON_TYPE.ADDON_SCRAPER_MOVIES, 24007,
				"DefaultAddonMovieInfo.png"));
		types.add(new TypeMapping("xbmc.metadata.scraper.musicvideos",
				ADDON_TYPE.ADDON_SCRAPER_MUSICVIDEOS, 24015,
				"DefaultAddonMusicVideoInfo.png"));
		types.add(new TypeMapping("xbmc.metadata.scraper.tvshows",
				ADDON_TYPE.ADDON_SCRAPER_TVSHOWS, 24014,
				"DefaultAddonTvInfo.png"));
		types.add(new TypeMapping("xbmc.metadata.scraper.epg",
				ADDON_TYPE.ADDON_SCRAPER_EPG, 24099, ""));
		types.add(new TypeMapping("xbmc.metadata.scraper.library",
				ADDON_TYPE.ADDON_SCRAPER_LIBRARY, 24083, ""));
	}

	public boolean isType(ADDON_TYPE type) {
		return type == props.type;
	}

	/**
	 * Takes the ID of the addon and loads all properties from the corresponding
	 * file structure.
	 * 
	 * @param id
	 * @return
	 */
	private boolean loadPropsByAddonId(String id) {

		if (!Addon.exists(id))
			return false;

		String path = Addon.getAddonXmlPath(id);

		if (path != null && !loadXMLDocument(path))
			return false;

		AddonProps props = new AddonProps();

		// Addon element
//		Settings settings = Settings.getInstance();
//		File addonDir = settings.getAddonDir();

		Element addonEl = document.getDocumentElement();

		if (addonEl == null || !addonEl.getNodeName().equals("addon"))
			return false;

		props.path = FilenameUtils.separatorsToSystem(Settings.getInstance()
				.getAddonDirPath() + "\\" + id);

		props.id = XMLUtils.getAttribute(addonEl, "id");
		props.name = XMLUtils.getAttribute(addonEl, "name");
		props.version = XMLUtils.getAttribute(addonEl, "version");
		props.author = XMLUtils.getAttribute(addonEl, "provider-name");

		// extension element
		Element extensionEl = XMLUtils.getFirstChildElement(addonEl,
				"extension");

		while (extensionEl != null) {
			String point = XMLUtils.getAttribute(extensionEl, "point");

			if (point.equals("xbmc.addon.metadata")) {
				Element licenseEl = XMLUtils.getFirstChildElement(extensionEl,
						"license");

				if (licenseEl != null)
					props.license = licenseEl.getNodeValue();

				// TODO add other meta data
			} else {
				props.libname = XMLUtils.getAttribute(extensionEl, "library");
				props.type = getTranslateType(point);
			}

			extensionEl = XMLUtils.getNextSiblingElement(extensionEl,
					"extension");
		}

		// dependencies element
		Element requiresEl = XMLUtils.getFirstChildElement(addonEl, "requires");
		Element importEl = XMLUtils.getFirstChildElement(requiresEl, "import");

		while (importEl != null) {

			String addon = XMLUtils.getAttribute(importEl, "addon");

			if (addon != null)
				props.dependencies.add(addon);

			importEl = XMLUtils.getNextSiblingElement(importEl, "import");
		}

		this.props = props;

		return true;
	}

	boolean loadSettings(boolean bForce /* = false */) {
		if (settingsLoaded && !bForce)
			return true;

		// if (!hasSettings)
		// return false;

		String addonFileName = props.path + "/resources/settings.xml";

		if (addonXmlDoc == null)
			addonXmlDoc = XMLUtils.getDocument(addonFileName);

		if (addonXmlDoc == null) {
			hasSettings = false;
			return false;
		}

		// Make sure that the addon XML has the settings element
		Element settings = addonXmlDoc.getDocumentElement();

		if (settings == null || !settings.getNodeName().equals("settings")) {
			return false;
		}

		settingsFromXML(addonXmlDoc, true);
		// LoadUserSettings();

		settingsLoaded = true;
		return true;
	}

	/**
	 * Language File Handling
	 */
	public boolean loadStrings() {
		// Path where the language strings reside
		String chosenPath = props.path + "/resources/language/"
				+ Locale.getDefault().getDisplayLanguage();

		Document stringsXmlDoc = XMLUtils.getDocument(chosenPath
				+ "/strings.xml");

		if (stringsXmlDoc == null) {
			hasStrings = false;
			return false;
		}

		return hasStrings = stringsFromXML(stringsXmlDoc, true);
	}

	public boolean loadUserSettings() {
		userSettingsLoaded = false;

		// TODO look into this
		// CXBMCTinyXML doc;
		// if (doc.LoadFile(m_userSettingsPath))
		// m_userSettingsLoaded = SettingsFromXML(doc);

		return userSettingsLoaded;
	}

	private boolean loadXMLDocument(String path) {

		// Document doc = GetXMLDocument(path);
		Document doc = XMLUtils.getDocument(path);

		if (doc == null)
			return false;

		document = doc;

		return true;
	}

	public boolean reloadSettings() {
		return loadSettings(true);
	}

	public boolean requiresSettings() {
		return requiressettings;
	}

	public void setSetting(String key, String value) {
		loadSettings(false);

		if (key == null)
			return;

		settings.put(key, value);
	}

	private boolean settingsFromXML(Document doc, boolean loadDefaults /*
																		 * =false
																		 */) {
		if (doc.getDocumentElement() == null)
			return false;

		if (loadDefaults)
			settings.clear();

		Element category = XMLUtils.getFirstChildElement(
				doc.getDocumentElement(), "category");
		if (category == null)
			category = doc.getDocumentElement();

		boolean foundSetting = false;
		while (category != null) {
			Element setting = XMLUtils
					.getFirstChildElement(category, "setting");

			while (setting != null) {

				String id = XMLUtils.getAttribute(setting, "id");
				String value = XMLUtils.getAttribute(setting,
						loadDefaults ? "default" : "value");

				if (id != null && value != null) {
					settings.put(id, value);
					foundSetting = true;
				}

				setting = XMLUtils.getNextSiblingElement(setting, "setting");
			}

			category = XMLUtils.getNextSiblingElement(category, "category");
		}
		return foundSetting;
	}

	private boolean stringsFromXML(Document doc, boolean loadDefaults /* =false */) {
		if (doc.getDocumentElement() == null)
			return false;

		if (loadDefaults)
			strings.clear();

		Element stringEl = XMLUtils.getFirstChildElement(
				doc.getDocumentElement(), "string");
		if (stringEl == null)
			return false;

		while (stringEl != null) {
			String id = XMLUtils.getAttribute(stringEl, "id");
			String value = XMLUtils.getFirstChildValue(stringEl);

			if (id != null && value != null) {
				strings.put(Integer.parseInt(id), value);
			}

			stringEl = XMLUtils.getNextSiblingElement(stringEl, "string");
		}
		return true;
	}
}
