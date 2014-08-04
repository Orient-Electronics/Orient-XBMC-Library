package orient.lib.xbmc.utils;

import android.annotation.SuppressLint;

import org.w3c.dom.Element;

public class StreamDetailVideo extends StreamDetail {
	public int width;
	public int height;
	public float aspect;
	public int duration;
	public String codec;
	public String stereoMode;

	@SuppressLint("DefaultLocale") public void parseElement(Element el) {
		Element item;
		
		// width
		item = XMLUtils.getFirstChildElement(el, "width");

		if (item != null)
			width = XMLUtils.getFirstChildValue_int(item);

		// Height
		item = XMLUtils.getFirstChildElement(el, "height");

		if (item != null)
			height = XMLUtils.getFirstChildValue_int(item);

		// Aspect
		item = XMLUtils.getFirstChildElement(el, "aspect");

		if (item != null)
			aspect = XMLUtils.getFirstChildValue_float(item);

		//Duration
		item = XMLUtils.getFirstChildElement(el, "durationinseconds");

		if (item != null)
			duration = XMLUtils.getFirstChildValue_int(item);

		// Codec
		item = XMLUtils.getFirstChildElement(el, "codec");

		if (item != null)
			codec = XMLUtils.getFirstChildValue(item);
		
		if (codec != null && codec.length() > 0)
			codec = codec.toLowerCase();

		// Stereo Mode
		item = XMLUtils.getFirstChildElement(el, "stereomode");

		if (item != null)
			stereoMode = XMLUtils.getFirstChildValue(item);
		
		if (stereoMode != null && stereoMode.length() > 0)
			stereoMode = stereoMode.toLowerCase();
	}
}