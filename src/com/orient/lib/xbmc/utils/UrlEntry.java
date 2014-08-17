package com.orient.lib.xbmc.utils;

public class UrlEntry {
	public String aspect;
	public String cache;
	public String spoof;

	/**
	 * Represents a thumbnail or icon of this entry. This doesn't exist in XBMC
	 * and is a custom implementation.
	 */
	public String thumb;
	public String url;
	public boolean isPost;
	public boolean isGZip;
	public int season;
	public URL_TYPE type;

	public UrlEntry() {
		reset();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof UrlEntry)) {
			return false;
		}

		UrlEntry that = (UrlEntry) other;

		if ((this.spoof == null || that.spoof == null)
				&& (this.spoof != that.spoof))
			return false;

		if ((this.url == null || that.url == null) && (this.url != that.url))
			return false;

		if ((this.cache == null || that.cache == null)
				&& (this.cache != that.cache))
			return false;

		if ((this.aspect == null || that.aspect == null)
				&& (this.aspect != that.aspect))
			return false;

		if ((this.thumb == null || that.thumb == null)
				&& (this.thumb != that.thumb))
			return false;

		// Custom equality check here.
		return this.type == that.type && this.isPost == that.isPost
				&& this.isGZip == that.isGZip && this.season == that.season;
	}

	public void reset() {
		aspect = null;
		cache = null;
		spoof = null;
		thumb = null;
		url = null;
		isPost = false;
		isGZip = false;
		season = -1;
		type = URL_TYPE.GENERAL;
	}
};