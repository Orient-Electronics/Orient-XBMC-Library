package orient.lib.xbmc.music;

import java.util.ArrayList;
import java.util.Date;

public class Song {

	public long idSong;
	public int idAlbum;
	public String fileName;
	public String title;
	ArrayList<String> artist;
	// VECARTISTCREDITS artistCredits;
	public String album;
	public ArrayList<String> albumArtist;
	public ArrayList<String> genre;
	public String thumb;
	// MUSIC_INFO::EmbeddedArtInfo embeddedArt;
	public String musicBrainzTrackID;
	public String comment;
	public char rating;
	public int track;
	public int duration;
	public int year;
	public int timesPlayed;
	public Date lastPlayed;
	public int startOffset;
	public int endOffset;
	public boolean compilation;

	// Karaoke-specific information
	public long karaokeNumber; // ! Karaoke song number to "select by number".
								// 0 for non-karaoke
	public String karaokeLyrEncoding; // ! Karaoke song lyrics encoding if
											// known. Empty if unknown.
	public int karaokeDelay; // ! Karaoke song lyrics-music delay in 1/10
								// seconds.
}
