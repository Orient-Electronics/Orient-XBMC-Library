package com.orient.lib.xbmc;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

public class Settings {
	private static Settings instance = null;
	
	public static Settings getInstance() {
		if (instance == null) {
			instance = new Settings();
		}
		return instance;
	}
	
	private File cacheDir;
	private File addonDir;
	
	private String assetsDirPath;
	private String addonDirPath;
	
	private String testAssetsDirPath;
	private ArrayList<String> videoCleanStringRegExps;
	private ArrayList<String> folderStackRegExps;
	private ArrayList<String> videoStackRegExps;
	private ArrayList<String> moviesExcludeFromScanRegExps;
	private ArrayList<String> tvshowExcludeFromScanRegExps;

	private final String videoCleanDateTimeRegExp = "(.*[^ _\\,\\.\\(\\)\\[\\]\\-])[ _\\.\\(\\)\\[\\]\\-]+(19[0-9][0-9]|20[0-1][0-9])([ _\\,\\.\\(\\)\\[\\]\\-]|[^0-9]$)";
	private final String pictureExtensions = ".png|.jpg|.jpeg|.bmp|.gif|.ico|.tif|.tiff|.tga|.pcx|.cbz|.zip|.cbr|.rar|.dng|.nef|.cr2|.crw|.orf|.arw|.erf|.3fr|.dcr|.x3f|.mef|.raf|.mrw|.pef|.sr2|.rss";

	private final String musicExtensions = ".nsv|.m4a|.flac|.aac|.strm|.pls|.rm|.rma|.mpa|.wav|.wma|.ogg|.mp3|.mp2|.m3u|.mod|.amf|.669|.dmf|.dsm|.far|.gdm|.imf|.it|.m15|.med|.okt|.s3m|.stm|.sfx|.ult|.uni|.xm|.sid|.ac3|.dts|.cue|.aif|.aiff|.wpl|.ape|.mac|.mpc|.mp+|.mpp|.shn|.zip|.rar|.wv|.nsf|.spc|.gym|.adx|.dsp|.adp|.ymf|.ast|.afc|.hps|.xsp|.xwav|.waa|.wvs|.wam|.gcm|.idsp|.mpdsp|.mss|.spt|.rsd|.mid|.kar|.sap|.cmc|.cmr|.dmc|.mpt|.mpd|.rmt|.tmc|.tm8|.tm2|.oga|.url|.pxml|.tta|.rss|.cm3|.cms|.dlt|.brstm|.wtv|.mka|.tak";

	private final String videoExtensions = ".m4v|.3g2|.3gp|.nsv|.tp|.ts|.ty|.strm|.pls|.rm|.rmvb|.m3u|.m3u8|.ifo|.mov|.qt|.divx|.xvid|.bivx|.vob|.nrg|.img|.iso|.pva|.wmv|.asf|.asx|.ogm|.m2v|.avi|.bin|.dat|.mpg|.mpeg|.mp4|.mkv|.avc|.vp3|.svq3|.nuv|.viv|.dv|.fli|.flv|.rar|.001|.wpl|.zip|.vdr|.dvr-ms|.xsp|.mts|.m2t|.m2ts|.evo|.ogv|.sdp|.avs|.rec|.url|.pxml|.vc1|.h264|.rcv|.rss|.mpls|.webm|.bdmv|.wtv";
	private final String subtitlesExtensions = ".utf|.utf8|.utf-8|.sub|.srt|.smi|.rt|.txt|.ssa|.text|.ssa|.aqt|.jss|.ass|.idx|.ifo|.rar|.zip";
	private Map<String, String> data;
	protected Settings() {
		// Exists only to defeat instantiation.
		
		// TODO only temp
		setCacheDir("\\assets\\xbmc-addons\\cache");
		setAddonDir("\\assets\\xbmc-addons\\addons");	

		setTestAssetsDirPath("tests\\assets\\");	
	
		
		///////////
		/// Paths
		String assetsDirStr = FilenameUtils.separatorsToSystem("\\assets");
		String addonDirStr = FilenameUtils.separatorsToSystem("xbmc-addons\\addons");
		
		if (XBMC.getInstance().isAndroid()) {
			setAssetsDirPath("");
			setAddonDirPath(addonDirStr);
		}
		else {
			setAssetsDirPath(getAppDir() + assetsDirStr);
			setAddonDirPath(FilenameUtils.separatorsToSystem(getAssetsDirPath() + "\\" + addonDirStr));
		}
		/// End
		///////
		
		videoCleanStringRegExps = new ArrayList<String>();
		videoCleanStringRegExps.add("[ _\\,\\.\\(\\)\\[\\]\\-](ac3|dts|custom|dc|remastered|divx|divx5|dsr|dsrip|dutch|dvd|dvd5|dvd9|dvdrip|dvdscr|dvdscreener|screener|dvdivx|cam|fragment|fs|hdtv|hdrip|hdtvrip|internal|limited|multisubs|ntsc|ogg|ogm|pal|pdtv|proper|repack|rerip|retail|r3|r5|bd5|se|svcd|swedish|german|read.nfo|nfofix|unrated|extended|ws|telesync|ts|telecine|tc|brrip|bdrip|480p|480i|576p|576i|720p|720i|1080p|1080i|3d|hrhd|hrhdtv|hddvd|bluray|x264|h264|xvid|xvidvd|xxx|www.www|cd[1-9]|\\[.*\\])([ _\\,\\.\\(\\)\\[\\]\\-]|$)");
		videoCleanStringRegExps.add("(\\[.*\\])");
		
		videoStackRegExps = new ArrayList<String>();
		videoStackRegExps.add("(.*?)([ _.-]*(?:cd|dvd|p(?:(?:ar)?t)|dis[ck]|d)[ _.-]*[0-9]+)(.*?)(\\.[^.]+)$");
		videoStackRegExps.add("(.*?)([ _.-]*(?:cd|dvd|p(?:(?:ar)?t)|dis[ck]|d)[ _.-]*[a-d])(.*?)(\\.[^.]+)$");
		videoStackRegExps.add("(.*?)([ ._-]*[a-d])(.*?)(\\.[^.]+)$");
		
		folderStackRegExps = new ArrayList<String>();
		folderStackRegExps.add("((cd|dvd|dis[ck])[0-9]+)$");
		
		moviesExcludeFromScanRegExps = new ArrayList<String>();
		moviesExcludeFromScanRegExps.add("-trailer");
		moviesExcludeFromScanRegExps.add("[!-._ \\\\/]sample[-._ \\\\/]");
		moviesExcludeFromScanRegExps.add("[\\/](proof|subs)[\\/]");
		
		tvshowExcludeFromScanRegExps = new ArrayList<String>();
		tvshowExcludeFromScanRegExps.add("[!-._ \\\\/]sample[-._ \\\\/]");		
		
		data = new HashMap<String, String>();
		data.put("musiclibrary.albumsscraper", null);
		data.put("musiclibrary.artistsscraper", null);
		data.put("scrapers.epgdefault", "metadata.epg.indian-television-guide");
		data.put("scrapers.moviesdefault", "metadata.themoviedb.org");
		data.put("scrapers.musicvideosdefault", null);
		data.put("scrapers.tvshowsdefault", "metadata.tvdb.com");
	}
	public String get(String key) {
		return data.get(key);
	}
	
	public File getAddonDir() {
		return addonDir;
	}
	
	public String getAddonDirPath() {
		return addonDirPath;
	}
	
	public String getAppDir() {
		
		String path = null;
		
		try {
			URI appDir = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().resolve("../..");
			path = appDir.toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return path;
	}
	
	
	public String getAssetsDirPath() {
		return assetsDirPath;
	}
	
	public File getCacheDir() {
		return cacheDir;
	}

	public ArrayList<String> getFolderStackRegExps() {
		return folderStackRegExps;
	}


	public ArrayList<String> getMoviesExcludeFromScanRegExps() {
		return moviesExcludeFromScanRegExps;
	}


	public String getMusicExtensions() {
		return musicExtensions;
	}


	public String getPictureExtensions() {
		return pictureExtensions;
	}
	
	public String getSubtitlesExtensions() {
		return subtitlesExtensions;
	}
	
	public String getTestAssetsDirPath() {
		return testAssetsDirPath;
	}

	public ArrayList<String> getTvshowExcludeFromScanRegExps() {
		return tvshowExcludeFromScanRegExps;
	}

	public String getVideoCleanDateTimeRegExp() {
		return videoCleanDateTimeRegExp;
	}

	public ArrayList<String> getVideoCleanStringRegExps() {
		return videoCleanStringRegExps;
	}

	public String getVideoExtensions() {
		return videoExtensions;
	}

	public ArrayList<String> getVideoStackRegExps() {
		return videoStackRegExps;
	}

	public void set(String key, String value) {
		data.put(key, value);
	}
	
	public void setAddonDir(File addonDir) {
		this.addonDir = addonDir;
	}

	public void setAddonDir(String addonDir) {
		this.addonDir = new File(addonDir);
	}

	public void setAddonDirPath(String addonDirPath) {
		this.addonDirPath = addonDirPath;
	}

	public void setAssetsDirPath(String assetsDirPath) {
		this.assetsDirPath = assetsDirPath;
	}


	public void setCacheDir(File cacheDir) {
		this.cacheDir = cacheDir;
	}


	public void setCacheDir(String cacheDir) {
		this.cacheDir = new File(cacheDir);
	}


	public void setMoviesExcludeFromScanRegExps(
			ArrayList<String> moviesExcludeFromScanRegExps) {
		this.moviesExcludeFromScanRegExps = moviesExcludeFromScanRegExps;
	}

	public void setTestAssetsDirPath(String testAssetsDirPath) {
		this.testAssetsDirPath = testAssetsDirPath;
	}

	public void setTvshowExcludeFromScanRegExps(
			ArrayList<String> tvshowExcludeFromScanRegExps) {
		this.tvshowExcludeFromScanRegExps = tvshowExcludeFromScanRegExps;
	}
	
	public void setVideoCleanStringRegExps(
			ArrayList<String> videoCleanStringRegExps) {
		this.videoCleanStringRegExps = videoCleanStringRegExps;
	}
}
