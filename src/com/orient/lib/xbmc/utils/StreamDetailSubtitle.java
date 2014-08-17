package com.orient.lib.xbmc.utils;

import org.w3c.dom.Element;

public class StreamDetailSubtitle extends StreamDetail {
	public String language;

	public void parseElement(Element el) {
		Element langEl = XMLUtils.getFirstChildElement(el, "language");

		if (langEl != null)
			language = XMLUtils.getFirstChildValue(langEl);

		if (language != null && language.length() > 0)
			language = language.toLowerCase();
	}
}
