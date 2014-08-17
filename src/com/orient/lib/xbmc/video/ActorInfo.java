package com.orient.lib.xbmc.video;

import org.w3c.dom.Element;

import com.orient.lib.xbmc.utils.ScraperUrl;
import com.orient.lib.xbmc.utils.XMLUtils;

public class ActorInfo {
	public String name;
	public String role;
	public String thumb;
	public ScraperUrl thumbUrl = new ScraperUrl();
	public int order;

	public void parseElement(Element actor) {
		// Name
		Element nameEl = XMLUtils.getFirstChildElement(actor, "name");
		
		if (nameEl != null)
			name = XMLUtils.getFirstChildValue(nameEl);
		else {
			String nameVal = XMLUtils.getFirstChildValue(actor);
			
			if (nameVal != null && !nameVal.trim().isEmpty())
				name = nameVal.trim();
		}

		
		// Role
		Element roleEl = XMLUtils.getFirstChildElement(actor, "role");
		
		if (roleEl != null)
			role = XMLUtils.getFirstChildValue(roleEl);
		else {
			String roleAttr = XMLUtils.getAttribute(actor, "role");
			
			if (roleAttr != null && !roleAttr.isEmpty())
				role = roleAttr;
		}
		
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
			
			thumbUrl.parseElement(thumb);
			
			thumb = XMLUtils.getNextSiblingElement(actor, "thumb");
		}
	}
}
