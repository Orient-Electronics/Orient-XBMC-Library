package com.orient.lib.xbmc.video;

public class Bookmark {

	enum Type {
		STANDARD, RESUME, EPISODE
	};

	public double timeInSeconds;
	public double totalTimeInSeconds;
	public long partNumber;
	public String thumbNailImage;
	public String playerState;
	public String player;
	public long seasonNumber;
	public long episodeNumber;

	public Type type;

	public Bookmark() {
		reset();
	}

	public void reset() {
		episodeNumber = 0;
		seasonNumber = 0;
		timeInSeconds = 0.0f;
		totalTimeInSeconds = 0.0f;
		partNumber = 0;
		type = Type.STANDARD;
	}

	/**
	 * brief returns true if this bookmark has been set. return true if
	 * totalTimeInSeconds is positive.
	 */
	public boolean isSet() {
		return totalTimeInSeconds > 0.0f;
	}

	/**
	 * brief returns true if this bookmark is part way through the video file
	 * return true if both totalTimeInSeconds and timeInSeconds are positive.
	 */
	public boolean isPartWay() {
		return totalTimeInSeconds > 0.0f && timeInSeconds > 0.0f;
	};

}
