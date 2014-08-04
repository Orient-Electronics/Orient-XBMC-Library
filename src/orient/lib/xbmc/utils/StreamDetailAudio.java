package orient.lib.xbmc.utils;

import org.w3c.dom.Element;

public class StreamDetailAudio extends StreamDetail {
	public float channels;
	public String codec;
	public String language;

	public void parseElement(Element el) {
		Element item = XMLUtils.getFirstChildElement(el, "channels");

		if (item != null)
			channels = XMLUtils.getFirstChildValue_float(item);
		
		
		// codec
		item = XMLUtils.getFirstChildElement(el, "codec");
		
		if (item != null)
			codec = XMLUtils.getFirstChildValue(item);

		if (codec != null && codec.length() > 0)
			codec = codec.toLowerCase();
		
		
		// language
		item = XMLUtils.getFirstChildElement(el, "language");

		if (item != null)
			language = XMLUtils.getFirstChildValue(item);
		
		if (language != null && language.length() > 0)
			language = language.toLowerCase();
	}
}
