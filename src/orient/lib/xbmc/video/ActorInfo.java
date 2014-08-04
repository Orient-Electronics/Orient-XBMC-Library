package orient.lib.xbmc.video;

import org.w3c.dom.Element;

import orient.lib.xbmc.utils.ScraperUrl;
import orient.lib.xbmc.utils.XMLUtils;

public class ActorInfo {
	public String name;
	public String role;
	public String thumb;
	public ScraperUrl thumbUrl = new ScraperUrl();
	public int order;

	public void parseElement(Element actor) {
		Element nameEl = XMLUtils.getFirstChildElement(actor, "name");
		
		if (nameEl != null)
			name = XMLUtils.getFirstChildValue(nameEl);

		Element roleEl = XMLUtils.getFirstChildElement(actor, "role");
		
		if (roleEl != null)
			role = XMLUtils.getFirstChildValue(roleEl);
		
		Element orderEl = XMLUtils.getFirstChildElement(actor, "order");
		String orderStr = "0";
		
		if (orderEl != null)
			orderStr = XMLUtils.getFirstChildValue(orderEl);
		
//		String order = XMLHelper.getFirstChildValue(actor, "order");

		try {
			this.order = Integer.parseInt(orderStr);
		} catch (NumberFormatException e) {
		}
		
		Element thumb = XMLUtils.getFirstChildElement(actor, "thumb");
		
		while(thumb != null) {
			
			if (thumbUrl == null)
				thumbUrl = new ScraperUrl();
			
			thumbUrl.ParseElement(thumb);
			
			thumb = XMLUtils.getNextSiblingElement(actor, "thumb");
		}
	}
}
