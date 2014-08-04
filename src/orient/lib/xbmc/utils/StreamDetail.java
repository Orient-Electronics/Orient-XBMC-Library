package orient.lib.xbmc.utils;

import org.w3c.dom.Element;

public abstract class StreamDetail {
	public enum StreamType {
		VIDEO, AUDIO, SUBTITLE
	};

	public StreamType type;
	public StreamDetails parent;

	// TODO implement
	public boolean IsWorseThan(StreamDetail that)
	{
		return false;
	}
	
	public abstract void parseElement(Element el);
}
