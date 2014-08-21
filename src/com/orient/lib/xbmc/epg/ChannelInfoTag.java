package com.orient.lib.xbmc.epg;

import org.w3c.dom.Element;

import com.orient.lib.xbmc.InfoTag;
import com.orient.lib.xbmc.utils.ScraperUrl;
import com.orient.lib.xbmc.utils.XMLUtils;

public class ChannelInfoTag extends InfoTag {

	public String id;
	public String name;
	public String logoUrl;
	public ScraperUrl url;
	
	@Override
	protected void initXmlTagMapping() {
		xmlTagMapping.clear();
		
		// Other
		xmlTagMapping.put("display-name", "name");

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void afterParseXML(Element element) {
		// Channel Id
		id = XMLUtils.getAttribute(element, "id");
	}

	@Override
	protected void onParseXMLItemOther(Element el) {
		super.onParseXMLItemOther(el);
		
		String tag = el.getNodeName();
		
		// Icon
		if (tag == "icon") {

			logoUrl = XMLUtils.getAttribute(el, "src");

		}
	}	
}
