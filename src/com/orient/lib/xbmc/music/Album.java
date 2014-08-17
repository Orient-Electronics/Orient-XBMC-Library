package com.orient.lib.xbmc.music;

import java.util.ArrayList;
import java.util.Map;

public class Album {

	public long id = -1;// idAlbum;
	public String album;
	public String musicBrainzAlbumID;
	public ArrayList<String> artist;
	// VECARTISTCREDITS artistCredits;
	public ArrayList<String> genre;
	// ScraperUrl thumbURL;
	public ArrayList<String> moods;
	public ArrayList<String> styles;
	public ArrayList<String> themes;
	public Map<String, String> art;
	public String review;
	public String label;
	public String type;
	public String path;
	public String dateOfRelease;
	public int rating = -1;
	public int year = -1;
	public boolean compilation;
	public int timesPlayed = 0;
	public ArrayList<Song> songs; // /< Local songs
	public ArrayList<Song> infoSongs; // /< Scraped songs
}
