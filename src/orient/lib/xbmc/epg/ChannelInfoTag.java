package orient.lib.xbmc.epg;

import org.w3c.dom.Element;

import orient.lib.xbmc.InfoTag;
import orient.lib.xbmc.utils.ScraperUrl;
import orient.lib.xbmc.utils.XMLUtils;

public class ChannelInfoTag extends InfoTag {

	public String id;
	public String name;
	public ScraperUrl pictureUrl;
	
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

			String url = XMLUtils.getAttribute(el, "src");

			if (url != null && !url.isEmpty()) {

				if (pictureUrl == null)
					pictureUrl = new ScraperUrl();

				pictureUrl.parseString("<url>" + url + "</url>");
			}
		}
	}	
}
