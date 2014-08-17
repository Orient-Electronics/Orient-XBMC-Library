package com.orient.lib.xbmc.video.test;

import static junitparams.JUnitParamsRunner.$;
import static org.junit.Assert.assertEquals;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.orient.lib.xbmc.FileItem;
import com.orient.lib.xbmc.Settings;
import com.orient.lib.xbmc.addons.Scraper;
import com.orient.lib.xbmc.video.VideoInfoScanner;
import com.orient.lib.xbmc.video.VideoInfoTag;

@RunWith(JUnitParamsRunner.class)
public class VideoInfoScannerTest {

	private String assetsPath;
	
	public VideoInfoScannerTest() {
		Settings settings = Settings.getInstance();
		assetsPath = settings.getTestAssetsDirPath();
	}
	
	@SuppressWarnings("unused")
	private Object[] nfoSearch() {
		return $(
				$( false, assetsPath + "Testing Data\\Movies - Folders\\Shakespeare in Love (1998)\\somefilename.nfo", assetsPath + "Testing Data\\Movies - Folders\\Shakespeare in Love (1998)cd1\\somefilename.avi" ),			
				$( true, assetsPath + "Testing Data\\Movies - Flat\\movie.nfo", assetsPath + "Testing Data\\Movies - Flat\\Battleship (2012).avi" ),
				$( true, assetsPath + "Testing Data\\Movies - Flat\\movie.nfo", assetsPath + "Testing Data\\Movies - Flat\\The Usual Suspects (1995).nfo" ),
				$( false, assetsPath + "Testing Data\\Movies - Flat\\The Usual Suspects (1995).nfo", assetsPath + "Testing Data\\Movies - Flat\\The Usual Suspects (1995).nfo" ),
				$( false, assetsPath + "Testing Data\\Movies - Flat\\Black Swan (2010) [720p] [R] [voted 0.0] [Thriller].nfo", "stack://" + assetsPath + "Testing Data\\Movies - Flat\\Black Swan (2010) [720p] [R] [voted 0.0] [Thriller]part1.avi , " + assetsPath + "Testing Data\\Movies - Flat\\Black Swan (2010) [720p] [R] [voted 0.0] [Thriller]part2.avi" ),
				$( false, assetsPath + "Testing Data\\Movies - Flat\\Mission Impossible [2011] 720p BRRip [Dual Audio] [English + Hindi] x264 BUZZccd [WBRG]cd1.nfo", "stack://" + assetsPath + "Testing Data\\Movies - Flat\\Mission Impossible [2011] 720p BRRip [Dual Audio] [English + Hindi] x264 BUZZccd [WBRG]cd1.mkv , " + assetsPath + "Testing Data\\Movies - Flat\\Mission Impossible [2011] 720p BRRip [Dual Audio] [English + Hindi] x264 BUZZccd [WBRG]cd2.mkv" ),
				$( false, assetsPath + "Testing Data\\Movies - Folders\\Shakespeare in Love (1998)\\somefilename.nfo", assetsPath + "Testing Data\\Movies - Folders\\Shakespeare in Love (1998)cd1\\somefilename.avi" )
				);
	}
	
	@Test
	@Parameters(method = "nfoSearch")
	public void getNFOFile(boolean grabAny, String expectedResult, String path) {

		FileItem item = new FileItem(path, false);
		
		VideoInfoScanner v = new VideoInfoScanner();
		String actualResult = v.getNFOFile(item, grabAny);
		
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void retrieveInfoForMovie() {
		
		// TODO add more test cases
		
		String filePath = assetsPath + "Testing Data\\Movies - Flat\\Reservoir Dogs (1992).avi";
		FileItem fileItem = new FileItem(filePath, false);
		
		Scraper scraper = new Scraper("metadata.themoviedb.org");
		
		VideoInfoScanner scanner = new VideoInfoScanner();
		scanner.retrieveInfoForMovie(fileItem, false, scraper, true);
		
		fileItem = scanner.getLastProcessedFileItem();
		VideoInfoTag infoTag = fileItem.getVideoInfoTag();
		
		assertEquals("Every dog has his day.", infoTag.tagline);
	}
	
	@Test
	public void retrieveInfoForMusicVideo() {
		
		// TODO add more test cases
		
		String filePath = assetsPath + "Testing Data\\Music Videos - Flat\\Michael Jackson - Beat It.avi";
		FileItem fileItem = new FileItem(filePath, false);
		
		Scraper scraper = new Scraper("metadata.musicvideos.theaudiodb.com");
		
		VideoInfoScanner scanner = new VideoInfoScanner();
		scanner.retrieveInfoForMusicVideo(fileItem, false, scraper, true);
		
		fileItem = scanner.getLastProcessedFileItem();
		VideoInfoTag infoTag = fileItem.getVideoInfoTag();
		
		assertEquals("Thriller", infoTag.album);
	}
	
	@Test
	public void retrieveInfoForTvShow() {
		
		// TODO add more test cases
		
		String filePath = assetsPath + "Testing Data\\TV Shows\\Two And A Half Men\\S01E01.avi";
		FileItem fileItem = new FileItem(filePath, false);
		
		Scraper scraper = new Scraper("metadata.tvdb.com");
		
		VideoInfoScanner scanner = new VideoInfoScanner();
		scanner.retrieveInfoForTvShow(fileItem, false, scraper, true, false);
		
		fileItem = scanner.getLastProcessedFileItem();
		VideoInfoTag infoTag = fileItem.getVideoInfoTag();
		
		assertEquals("Thriller", infoTag.album);
	}
}
