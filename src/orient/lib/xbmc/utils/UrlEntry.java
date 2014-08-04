package orient.lib.xbmc.utils;

public class UrlEntry {
	public String m_spoof = null;
	public String m_url = null;
	public String m_cache = null;
	public String m_aspect = null;
	public URL_TYPE m_type = URL_TYPE.GENERAL;
	public boolean m_post = false;
	public boolean m_isgz = false;
	public int m_season = -1;

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof UrlEntry)) {
			return false;
		}

		UrlEntry that = (UrlEntry) other;

		if ((this.m_spoof == null || that.m_spoof == null)
				&& (this.m_spoof != that.m_spoof))
			return false;

		if ((this.m_url == null || that.m_url == null)
				&& (this.m_url != that.m_url))
			return false;

		if ((this.m_cache == null || that.m_cache == null)
				&& (this.m_cache != that.m_cache))
			return false;

		if ((this.m_aspect == null || that.m_aspect == null)
				&& (this.m_aspect != that.m_aspect))
			return false;

		// Custom equality check here.
		return this.m_type == that.m_type && this.m_post == that.m_post
				&& this.m_isgz == that.m_isgz && this.m_season == that.m_season;
	}
};