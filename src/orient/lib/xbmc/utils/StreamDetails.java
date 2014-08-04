package orient.lib.xbmc.utils;

import java.util.ArrayList;

public class StreamDetails {
	private ArrayList<StreamDetail> items;
	private StreamDetailVideo bestVideo;
	private StreamDetailAudio bestAudio;
	private StreamDetailSubtitle bestSubtitle; 



	public int GetStreamCount(StreamDetail.StreamType type) 
	{
		int retVal = 0;

		for (StreamDetail i : items) {
			if (i.type == type)
				retVal++;
		}
		return retVal;
	}

	int GetVideoStreamCount()
	{
		return GetStreamCount(StreamDetail.StreamType.VIDEO);
	}
	
	int GetAudioStreamCount()
	{
		return GetStreamCount(StreamDetail.StreamType.AUDIO);
	}
	
	int GetSubtitleStreamCount()
	{
		return GetStreamCount(StreamDetail.StreamType.SUBTITLE);
	}

	public void AddStream(StreamDetail item)
	{
		item.parent = this;

		if(items == null)
			items = new ArrayList<StreamDetail>();

		items.add(item);
	}

	public void Reset()
	{
		bestVideo = null;
		bestAudio = null;
		bestSubtitle = null;

		items.clear();
	}

	public StreamDetail GetNthStream(StreamDetail.StreamType type, int idx)
	{
		if (idx == 0)
		{

			if (type == StreamDetail.StreamType.VIDEO) {
				return bestVideo;	    	
			}
			else if (type == StreamDetail.StreamType.AUDIO) {
				return bestAudio;
			}
			else if (type == StreamDetail.StreamType.SUBTITLE) {
				return bestSubtitle;    	
			}
			else
				return null;
		}

		for (StreamDetail i : items)
		{
			if(i.type == type) {
				idx--;

				if (idx < 1)
					return i;
			}
		}

		return null;
	}
	
	public String GetVideoCodec(int idx) 
	{
		StreamDetailVideo item = (StreamDetailVideo) GetNthStream(StreamDetail.StreamType.VIDEO, idx);
		if (item != null)
			return item.codec;
		else
			return "";
	}


	public float GetVideoAspect(int idx) 
	{
		StreamDetailVideo item = (StreamDetailVideo) GetNthStream(StreamDetail.StreamType.VIDEO, idx);
		if (item != null)
			return item.aspect;
		else
			return (float) 0.0;
	}

	public int GetVideoWidth(int idx) 
	{
		StreamDetailVideo item = (StreamDetailVideo) GetNthStream(StreamDetail.StreamType.VIDEO, idx);
		if (item != null)
			return item.width;
		else
			return 0;
	}

	public int GetVideoHeight(int idx) 
	{
		StreamDetailVideo item = (StreamDetailVideo) GetNthStream(StreamDetail.StreamType.VIDEO, idx);
		if (item != null)
			return item.height;
		else
			return 0;
	}


	public int GetVideoDuration(int idx) 
	{
		StreamDetailVideo item = (StreamDetailVideo) GetNthStream(StreamDetail.StreamType.VIDEO, idx);
		if (item != null)
			return item.duration;
		else
			return 0;
	}


	public void SetVideoDuration(int idx, int duration) 
	{
		StreamDetailVideo item = (StreamDetailVideo) GetNthStream(StreamDetail.StreamType.VIDEO, idx);
		if (item != null)
			item.duration = duration;
	}


	public String GetStereoMode(int idx) 
	{
		StreamDetailVideo item = (StreamDetailVideo) GetNthStream(StreamDetail.StreamType.VIDEO, idx);
		if (item != null)
			return item.stereoMode;
		else
			return "";
	}


	public String GetAudioCodec(int idx) 
	{
		StreamDetailAudio item = (StreamDetailAudio) GetNthStream(StreamDetail.StreamType.AUDIO, idx);
		if (item != null)
			return item.codec;
		else
			return "";
	}

	public String GetAudioLanguage(int idx) 
	{
		StreamDetailAudio item = (StreamDetailAudio) GetNthStream(StreamDetail.StreamType.AUDIO, idx);
		if (item != null)
			return item.language;
		else
			return "";
	}

	public float GetAudioChannels(int idx) 
	{
		StreamDetailAudio item = (StreamDetailAudio) GetNthStream(StreamDetail.StreamType.AUDIO, idx);
		if (item !=null)
			return item.channels;
		else
			return -1;
	}

	public String GetSubtitleLanguage(int idx) 
	{
		StreamDetailSubtitle item = (StreamDetailSubtitle) GetNthStream(StreamDetail.StreamType.SUBTITLE, idx);
		if (item != null)
			return item.language;
		else
			return "";
	}

	public void DetermineBestStreams()
	{
		bestVideo = null;
		bestAudio = null;
		bestSubtitle = null;

		for (StreamDetail i : items)
		{

			StreamDetail champion;

			if (i.type == StreamDetail.StreamType.VIDEO) {
				champion = (StreamDetail) bestVideo;	    	
			}
			else if (i.type == StreamDetail.StreamType.AUDIO) {
				champion = (StreamDetail) bestAudio;	    	
			}
			else if (i.type == StreamDetail.StreamType.SUBTITLE) {
				champion = (StreamDetail) bestSubtitle;	    	
			}
			else
				continue;

			if (champion.IsWorseThan(i))
					champion = i;
		}  /* for each */
	}


	public String VideoDimsToResolutionDescription(int iWidth, int iHeight)
	{
		if (iWidth == 0 || iHeight == 0)
			return "";

		else if (iWidth <= 720 && iHeight <= 480)
			return "480";
		// 720x576 (PAL) (768 when rescaled for square pixels)
		else if (iWidth <= 768 && iHeight <= 576)
			return "576";
		// 960x540 (sometimes 544 which is multiple of 16)
		else if (iWidth <= 960 && iHeight <= 544)
			return "540";
		// 1280x720
		else if (iWidth <= 1280 && iHeight <= 720)
			return "720";
		// 1920x1080
		else if (iWidth <= 1920 && iHeight <= 1080)
			return "1080";
		// 4K
		else if (iWidth * iHeight >= 6000000)
			return "4K";
		else
			return "";
	}

	public String VideoAspectToAspectDescription(float fAspect)
	{
		if (fAspect == 0.0f)
			return "";

		// Given that we're never going to be able to handle every single possibility in
		// aspect ratios, particularly when cropping prior to video encoding is taken into account
		// the best we can do is take the "common" aspect ratios, and return the closest one available.
		// The cutoffs are the geometric mean of the two aspect ratios either side.
		if (fAspect < 1.3499f) // sqrt(1.33*1.37)
			return "1.33";
		else if (fAspect < 1.5080f) // sqrt(1.37*1.66)
			return "1.37";
		else if (fAspect < 1.7190f) // sqrt(1.66*1.78)
			return "1.66";
		else if (fAspect < 1.8147f) // sqrt(1.78*1.85)
			return "1.78";
		else if (fAspect < 2.0174f) // sqrt(1.85*2.20)
			return "1.85";
		else if (fAspect < 2.2738f) // sqrt(2.20*2.35)
			return "2.20";
		else if (fAspect < 2.3749f) // sqrt(2.35*2.40)
			return "2.35";
		else if (fAspect < 2.4739f) // sqrt(2.40*2.55)
			return "2.40";
		else if (fAspect < 2.6529f) // sqrt(2.55*2.76)
			return "2.55";
		return "2.76";
	}

}
