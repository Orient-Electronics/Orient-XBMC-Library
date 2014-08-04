package orient.lib.xbmc.addons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import orient.lib.xbmc.addons.ADDON_TYPE;

public class AddonProps {
	public String id;
	public ADDON_TYPE type;
	public String version;
	public String minversion;
	public String name;
	public String license;
	public String summary;
	public String description;
	public String path;
	public String libname;
	public String author;
	public String source;
	public String icon;
	public String disclaimer;
	public String changelog;
	public String fanart;
	public String broken;
	public ArrayList<String> dependencies = new ArrayList<String>();
	public Map<String, String> extrainfo = new HashMap<String, String>();
	public Map<String, String> summaryMap = new HashMap<String, String>();
	public Map<String, String> descriptionMap = new HashMap<String, String>();
	public int stars;
}
